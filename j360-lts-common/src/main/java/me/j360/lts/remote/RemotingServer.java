package me.j360.lts.remote;


import me.j360.lts.remote.exception.RemotingException;
import me.j360.lts.remote.exception.RemotingSendRequestException;
import me.j360.lts.remote.exception.RemotingTimeoutException;
import me.j360.lts.remote.exception.RemotingTooMuchRequestException;
import me.j360.lts.remote.protocol.RemotingCommand;

import java.util.concurrent.ExecutorService;

/**
 * 远程通信，Server接口
 */
public interface RemotingServer {

    public void start() throws RemotingException;


    /**
     * 注册请求处理器，ExecutorService必须要对应一个队列大小有限制的阻塞队列，防止OOM
     */
    public void registerProcessor(final int requestCode, final RemotingProcessor processor,
                                  final ExecutorService executor);

    /**
     * 注册默认请求处理器
     */
    public void registerDefaultProcessor(final RemotingProcessor processor, final ExecutorService executor);


    /**
     * 同步调用
     */
    public RemotingCommand invokeSync(final Channel channel, final RemotingCommand request,
                                      final long timeoutMillis) throws InterruptedException, RemotingSendRequestException,
            RemotingTimeoutException;

    /**
     * 异步调用
     */
    public void invokeAsync(final Channel channel, final RemotingCommand request, final long timeoutMillis,
                            final AsyncCallback asyncCallback) throws InterruptedException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    /**
     * 单向调用
     */
    public void invokeOneway(final Channel channel, final RemotingCommand request, final long timeoutMillis)
            throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
            RemotingSendRequestException;


    public void shutdown();

}
