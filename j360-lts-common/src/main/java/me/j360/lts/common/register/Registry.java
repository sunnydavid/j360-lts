package me.j360.lts.common.register;


import me.j360.lts.common.cluster.Node;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 *         �ڵ�ע��ӿ�
 */
public interface Registry {

    /**
     * �ڵ�ע��
     *
     * @param node
     */
    void register(Node node);

    /**
     * �ڵ� ȡ��ע��
     *
     * @param node
     */
    void unregister(Node node);

    /**
     * �����ڵ�
     *
     * @param listener
     */
    void subscribe(Node node, NotifyListener listener);

    /**
     * ȡ�������ڵ�
     *
     * @param node
     * @param listener
     */
    void unsubscribe(Node node, NotifyListener listener);

    /**
     * ����
     */
    void destroy();
}
