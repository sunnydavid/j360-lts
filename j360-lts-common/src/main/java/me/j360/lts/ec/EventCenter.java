package me.j360.lts.ec;

/**
 * �¼����Ľӿ�
 *
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
public interface EventCenter {

    /**
     * ��������
     *
     * @param topics
     * @param subscriber
     */
    public void subscribe(EventSubscriber subscriber, String... topics);

    /**
     * ȡ����������
     *
     * @param topic
     * @param subscriber
     */
    public void unSubscribe(String topic, EventSubscriber subscriber);

    /**
     * ͬ������������Ϣ
     *
     * @param eventInfo
     */
    public void publishSync(EventInfo eventInfo);

    /**
     * �첽����������Ϣ
     *
     * @param eventInfo
     */
    public void publishAsync(EventInfo eventInfo);

}
