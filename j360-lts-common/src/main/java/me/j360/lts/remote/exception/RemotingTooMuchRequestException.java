package me.j360.lts.remote.exception;

/**
 * �첽���û���Oneway���ã��ѻ������󳬹��ź������ֵ
 */
public class RemotingTooMuchRequestException extends RemotingException {
    private static final long serialVersionUID = 4326919581254519654L;


    public RemotingTooMuchRequestException(String message) {
        super(message);
    }
}
