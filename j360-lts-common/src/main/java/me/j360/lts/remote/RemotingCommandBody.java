package me.j360.lts.remote;


import me.j360.lts.remote.exception.RemotingCommandFieldCheckException;

import java.io.Serializable;

/**
 * RemotingCommand���Զ����ֶη������Ĺ����ӿ�
 */
public interface RemotingCommandBody extends Serializable {

    public void checkFields() throws RemotingCommandFieldCheckException;
}
