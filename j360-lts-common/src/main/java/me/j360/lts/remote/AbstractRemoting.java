package me.j360.lts.remote;


import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.support.SystemClock;
import me.j360.lts.common.utils.CommonUtils;
import me.j360.lts.remote.codec.Codec;
import me.j360.lts.remote.codec.DefaultCodec;
import me.j360.lts.remote.common.Pair;
import me.j360.lts.remote.common.RemotingHelper;
import me.j360.lts.remote.common.SemaphoreReleaseOnlyOnce;
import me.j360.lts.remote.common.ServiceThread;
import me.j360.lts.remote.exception.RemotingSendRequestException;
import me.j360.lts.remote.exception.RemotingTimeoutException;
import me.j360.lts.remote.exception.RemotingTooMuchRequestException;
import me.j360.lts.remote.protocol.RemotingCommand;
import me.j360.lts.remote.protocol.RemotingCommandHelper;
import me.j360.lts.remote.protocol.RemotingProtos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.*;


/**
 * Server��Client���ó�����
 */
public abstract class AbstractRemoting {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);

    // �ź�����Oneway�����ʹ�ã���ֹ���ػ����������
    protected final Semaphore semaphoreOneway;

    // �ź������첽���������ʹ�ã���ֹ���ػ����������
    protected final Semaphore semaphoreAsync;

    // �������ж�������
    protected final ConcurrentHashMap<Integer /* opaque */, ResponseFuture> responseTable =
            new ConcurrentHashMap<Integer, ResponseFuture>(256);
    // ע��ĸ���RPC������
    protected final HashMap<Integer/* request code */, Pair<RemotingProcessor, ExecutorService>> processorTable =
            new HashMap<Integer, Pair<RemotingProcessor, ExecutorService>>(64);
    protected final RemotingEventExecutor remotingEventExecutor = new RemotingEventExecutor();
    // Ĭ��������봦����
    protected Pair<RemotingProcessor, ExecutorService> defaultRequestProcessor;
    protected final ChannelEventListener channelEventListener;

    public AbstractRemoting(final int permitsOneway, final int permitsAsync, ChannelEventListener channelEventListener) {
        this.semaphoreOneway = new Semaphore(permitsOneway, true);
        this.semaphoreAsync = new Semaphore(permitsAsync, true);
        this.channelEventListener = channelEventListener;
    }

    public ChannelEventListener getChannelEventListener() {
        return this.channelEventListener;
    }

    public void putRemotingEvent(final RemotingEvent event) {
        this.remotingEventExecutor.putRemotingEvent(event);
    }

    public void processRequestCommand(final Channel channel, final RemotingCommand cmd) {
        final Pair<RemotingProcessor, ExecutorService> matched = this.processorTable.get(cmd.getCode());
        final Pair<RemotingProcessor, ExecutorService> pair =
                null == matched ? this.defaultRequestProcessor : matched;

        if (pair != null) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        final RemotingCommand response = pair.getObject1().processRequest(channel, cmd);
                        // Oneway��ʽ����Ӧ����
                        if (!RemotingCommandHelper.isOnewayRPC(cmd)) {
                            if (response != null) {
                                response.setOpaque(cmd.getOpaque());
                                RemotingCommandHelper.markResponseType(cmd);
                                try {
                                    channel.writeAndFlush(response).addListener(new ChannelHandlerListener() {
                                        @Override
                                        public void operationComplete(Future future) throws Exception {
                                            if (!future.isSuccess()) {
                                                LOGGER.error("response to " + RemotingHelper.parseChannelRemoteAddr(channel) + " failed", future.cause());
                                                LOGGER.error(cmd.toString());
                                                LOGGER.error(response.toString());
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    LOGGER.error("process request over, but response failed", e);
                                    LOGGER.error(cmd.toString());
                                    LOGGER.error(response.toString());
                                }
                            } else {
                                // �յ����󣬵���û�з���Ӧ�𣬿�����processRequest�н�����Ӧ�𣬺����������
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("process request exception", e);
                        LOGGER.error(cmd.toString());

                        if (!RemotingCommandHelper.isOnewayRPC(cmd)) {
                            final RemotingCommand response =
                                    RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SYSTEM_ERROR.code(),//
                                            CommonUtils.exceptionSimpleDesc(e));
                            response.setOpaque(cmd.getOpaque());
                            channel.writeAndFlush(response);
                        }
                    }
                }
            };

            try {
                // ������Ҫ�����أ�Ҫ���̳߳ض�Ӧ�Ķ��б������д�С���Ƶ�
                pair.getObject2().submit(run);
            } catch (RejectedExecutionException e) {
                LOGGER.warn(RemotingHelper.parseChannelRemoteAddr(channel) //
                        + ", too many requests and system thread pool busy, RejectedExecutionException " //
                        + pair.getObject2().toString() //
                        + " request code: " + cmd.getCode());
                if (!RemotingCommandHelper.isOnewayRPC(cmd)) {
                    final RemotingCommand response =
                            RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SYSTEM_BUSY.code(),
                                    "too many requests and system thread pool busy, please try another server");
                    response.setOpaque(cmd.getOpaque());
                    channel.writeAndFlush(response);
                }
            }
        } else {
            String error = " request type " + cmd.getCode() + " not supported";
            final RemotingCommand response =
                    RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(),
                            error);
            response.setOpaque(cmd.getOpaque());
            channel.writeAndFlush(response);
            LOGGER.error(RemotingHelper.parseChannelRemoteAddr(channel) + error);
        }
    }

    public void processResponseCommand(Channel channel, RemotingCommand cmd) {
        final ResponseFuture responseFuture = responseTable.get(cmd.getOpaque());
        if (responseFuture != null) {
            responseFuture.setResponseCommand(cmd);

            responseFuture.release();

            // �첽����
            if (responseFuture.getAsyncCallback() != null) {
                boolean runInThisThread = false;
                ExecutorService executor = this.getCallbackExecutor();
                if (executor != null) {
                    try {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    responseFuture.executeInvokeCallback();
                                } catch (Exception e) {
                                    LOGGER.warn("excute callback in executor exception, and callback throw", e);
                                }
                            }
                        });
                    } catch (Exception e) {
                        runInThisThread = true;
                        LOGGER.warn("excute callback in executor exception, maybe executor busy", e);
                    }
                } else {
                    runInThisThread = true;
                }

                if (runInThisThread) {
                    try {
                        responseFuture.executeInvokeCallback();
                    } catch (Exception e) {
                        LOGGER.warn("", e);
                    }
                }
            }
            // ͬ������
            else {
                responseFuture.putResponse(cmd);
            }
        } else {
            LOGGER.warn("receive response, but not matched any request, "
                    + RemotingHelper.parseChannelRemoteAddr(channel));
            LOGGER.warn(cmd.toString());
        }

        responseTable.remove(cmd.getOpaque());
    }

    public void processMessageReceived(Channel channel, final RemotingCommand cmd) throws Exception {
        if (cmd != null) {
            switch (RemotingCommandHelper.getRemotingCommandType(cmd)) {
                case REQUEST_COMMAND:
                    processRequestCommand(channel, cmd);
                    break;
                case RESPONSE_COMMAND:
                    processResponseCommand(channel, cmd);
                    break;
                default:
                    break;
            }
        }
    }

    protected abstract ExecutorService getCallbackExecutor();

    public void scanResponseTable() {
        Iterator<Entry<Integer, ResponseFuture>> it = this.responseTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, ResponseFuture> next = it.next();
            ResponseFuture rep = next.getValue();

            if ((rep.getBeginTimestamp() + rep.getTimeoutMillis() + 1000) <= SystemClock.now()) {
                it.remove();
                rep.release();
                try {
                    rep.executeInvokeCallback();
                } catch (Exception e) {
                    LOGGER.error("scanResponseTable, operationComplete exception", e);
                }

                LOGGER.warn("remove timeout request, " + rep);
            }
        }
    }

    public RemotingCommand invokeSyncImpl(final Channel channel, final RemotingCommand request,
                                          final long timeoutMillis) throws InterruptedException, RemotingSendRequestException,
            RemotingTimeoutException {
        try {
            final ResponseFuture responseFuture =
                    new ResponseFuture(request.getOpaque(), timeoutMillis, null, null);
            this.responseTable.put(request.getOpaque(), responseFuture);

            //�˴�����Ҫ����responseFuture������ʧ��
            channel.writeAndFlush(request).addListener(new ChannelHandlerListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    LOGGER.debug("game over");
                    if (future.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    } else {
                        responseFuture.setSendRequestOK(false);
                    }

                    responseTable.remove(request.getOpaque());
                    responseFuture.setCause(future.cause());
                    responseFuture.putResponse(null);
                    LOGGER.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                    LOGGER.warn(request.toString());
                }


            });

            RemotingCommand responseCommand = responseFuture.waitResponse(timeoutMillis);
            if (null == responseCommand) {
                // ��������ɹ�����ȡӦ��ʱ
                if (responseFuture.isSendRequestOK()) {
                    throw new RemotingTimeoutException(RemotingHelper.parseChannelRemoteAddr(channel),
                            timeoutMillis, responseFuture.getCause());
                }
                // ��������ʧ��
                else {
                    throw new RemotingSendRequestException(RemotingHelper.parseChannelRemoteAddr(channel),
                            responseFuture.getCause());
                }
            }

            return responseCommand;
        } finally {
            this.responseTable.remove(request.getOpaque());
        }
    }

    public void invokeAsyncImpl(final Channel channel, final RemotingCommand request,
                                final long timeoutMillis, final AsyncCallback asyncCallback) throws InterruptedException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException {
        boolean acquired = this.semaphoreAsync.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreAsync);

            final ResponseFuture responseFuture =
                    new ResponseFuture(request.getOpaque(), timeoutMillis, asyncCallback, once);
            this.responseTable.put(request.getOpaque(), responseFuture);
            try {
                channel.writeAndFlush(request).addListener(new ChannelHandlerListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        if (future.isSuccess()) {
                            responseFuture.setSendRequestOK(true);
                            return;
                        } else {
                            responseFuture.setSendRequestOK(false);
                        }

                        responseFuture.putResponse(null);
                        responseFuture.executeInvokeCallback();

                        responseTable.remove(request.getOpaque());
                        LOGGER.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                        LOGGER.warn(request.toString());
                    }
                });
            } catch (Exception e) {
                once.release();
                LOGGER.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new RemotingSendRequestException(RemotingHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new RemotingTooMuchRequestException("invokeAsyncImpl invoke too fast");
            } else {
                LOGGER.warn("invokeAsyncImpl tryAcquire semaphore timeout, " + timeoutMillis
                        + " waiting thread nums: " + this.semaphoreAsync.getQueueLength());
                LOGGER.warn(request.toString());

                throw new RemotingTimeoutException("tryAcquire timeout(ms) " + timeoutMillis);
            }
        }
    }

    public void invokeOnewayImpl(final Channel channel, final RemotingCommand request,
                                 final long timeoutMillis) throws InterruptedException, RemotingTooMuchRequestException,
            RemotingTimeoutException, RemotingSendRequestException {
        RemotingCommandHelper.markOnewayRPC(request);
        boolean acquired = this.semaphoreOneway.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreOneway);
            try {
                channel.writeAndFlush(request).addListener(new ChannelHandlerListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        once.release();
                        if (!future.isSuccess()) {
                            LOGGER.warn("send a request command to channel <" + channel.remoteAddress()
                                    + "> failed.");
                            LOGGER.warn(request.toString());
                        }
                    }
                });
            } catch (Exception e) {
                once.release();
                LOGGER.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new RemotingSendRequestException(RemotingHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new RemotingTooMuchRequestException("invokeOnewayImpl invoke too fast");
            } else {
                LOGGER.warn("invokeOnewayImpl tryAcquire semaphore timeout, " + timeoutMillis
                        + " waiting thread nums: " + this.semaphoreOneway.getQueueLength());
                LOGGER.warn(request.toString());

                throw new RemotingTimeoutException("tryAcquire timeout(ms) " + timeoutMillis);
            }
        }
    }

    class RemotingEventExecutor extends ServiceThread {
        private final LinkedBlockingQueue<RemotingEvent> eventQueue = new LinkedBlockingQueue<RemotingEvent>();
        private final int MaxSize = 10000;

        public void putRemotingEvent(final RemotingEvent event) {
            if (this.eventQueue.size() <= MaxSize) {
                this.eventQueue.add(event);
            } else {
                LOGGER.warn("event queue size[{}] enough, so drop this event {}", this.eventQueue.size(),
                        event.toString());
            }
        }

        @Override
        public void run() {


            LOGGER.info(this.getServiceName() + " service started");

            final ChannelEventListener listener = AbstractRemoting.this.getChannelEventListener();

            while (!this.isStoped()) {
                try {
                    RemotingEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        switch (event.getType()) {
                            case ALL_IDLE:
                                listener.onChannelIdle(IdleState.ALL_IDLE, event.getRemoteAddr(), event.getChannel());
                                break;
                            case WRITER_IDLE:
                                listener.onChannelIdle(IdleState.WRITER_IDLE, event.getRemoteAddr(), event.getChannel());
                                break;
                            case READER_IDLE:
                                listener.onChannelIdle(IdleState.READER_IDLE, event.getRemoteAddr(), event.getChannel());
                                break;
                            case CLOSE:
                                listener.onChannelClose(event.getRemoteAddr(), event.getChannel());
                                break;
                            case CONNECT:
                                listener.onChannelConnect(event.getRemoteAddr(), event.getChannel());
                                break;
                            case EXCEPTION:
                                listener.onChannelException(event.getRemoteAddr(), event.getChannel());
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn(this.getServiceName() + " service has exception. ", e);
                }
            }

            LOGGER.info(this.getServiceName() + " service end");
        }

        @Override
        public String getServiceName() {
            return RemotingEventExecutor.class.getSimpleName();
        }
    }

    protected Codec getCodec() {
        // TODO ��ΪSPI
        return new DefaultCodec();
    }

}
