package me.j360.lts.common.exception;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14.
 * ��û�� �ҵ� JobTracker �ڵ��ʱ���׳�����쳣
 */
public class JobTrackerNotFoundException extends Exception{

    public JobTrackerNotFoundException() {
    }

    public JobTrackerNotFoundException(String message) {
        super(message);
    }

    public JobTrackerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobTrackerNotFoundException(Throwable cause) {
        super(cause);
    }

    public JobTrackerNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
