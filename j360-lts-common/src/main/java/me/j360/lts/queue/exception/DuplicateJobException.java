package me.j360.lts.queue.exception;

/**
 * �������������������ʱ�򣬻���������ظ���������ͻ��׳�����쳣
 * @author Robert HG (254963746@qq.com) on 3/26/15.
 */
public class DuplicateJobException extends RuntimeException {

    public DuplicateJobException() {
        super();
    }

    public DuplicateJobException(String message) {
        super(message);
    }

    public DuplicateJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateJobException(Throwable cause) {
        super(cause);
    }

}
