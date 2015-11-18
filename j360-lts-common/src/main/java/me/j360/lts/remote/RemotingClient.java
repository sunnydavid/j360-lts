package me.j360.lts.remote;


import me.j360.lts.remote.exception.*;
import me.j360.lts.remote.protocol.RemotingCommand;

import java.util.concurrent.ExecutorService;


/**
 * Զ��ͨ�ţ�Client�ӿ�
 */
public interface RemotingClient {

    public void start() throws RemotingException;

    /**
     * ͬ������
     */
    public RemotingCommand invokeSync(final String addr, final RemotingCommand request,
                                      final long timeoutMillis) throws InterruptedException, RemotingConnectException,
            RemotingSendRequestException, RemotingTimeoutException;

    /**
     * �첽����
     */
    public void invokeAsync(final String addr, final RemotingCommand request, final long timeoutMillis,
                            final AsyncCallback asyncCallback) throws InterruptedException, RemotingConnectException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    /**
     * �������
     */
    public void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis)
            throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
            RemotingTimeoutException, RemotingSendRequestException;

    /**
     * ע�ᴦ����
     */
    public void registerProcessor(final int requestCode, final RemotingProcessor processor,
                                  final ExecutorService executor);

    /**
     * ע��Ĭ�ϴ�����
     */
    public void registerDefaultProcessor(final RemotingProcessor processor, final ExecutorService executor);

    public void shutdown();
}
