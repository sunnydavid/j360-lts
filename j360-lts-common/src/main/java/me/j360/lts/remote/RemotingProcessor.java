package me.j360.lts.remote;


import me.j360.lts.remote.exception.RemotingCommandException;
import me.j360.lts.remote.protocol.RemotingCommand;

/**
 * ����������������������ͻ���ͨ��
 */
public interface RemotingProcessor {
    public RemotingCommand processRequest(Channel channel, RemotingCommand request)
            throws RemotingCommandException;
}
