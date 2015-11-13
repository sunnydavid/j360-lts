package me.j360.lts.remote.common;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * ʹ�ò���ԭ�ӱ������ź�����ֻ֤�ͷ�һ��
 */
public class SemaphoreReleaseOnlyOnce {
    private final AtomicBoolean released = new AtomicBoolean(false);
    private final Semaphore semaphore;

    public SemaphoreReleaseOnlyOnce(Semaphore semaphore) {
        this.semaphore = semaphore;
    }


    public void release() {
        if (this.semaphore != null) {
            if (this.released.compareAndSet(false, true)) {
                this.semaphore.release();
            }
        }
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }
}
