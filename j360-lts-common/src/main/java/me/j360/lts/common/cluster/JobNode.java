package me.j360.lts.common.cluster;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 * �ڵ�ӿ�
 */
public interface JobNode {

    /**
     * �����ڵ�
     */
    public void start();

    /**
     * ֹͣ�ڵ�
     */
    public void stop();

    /**
     * destroy
     */
    public void destroy();
}
