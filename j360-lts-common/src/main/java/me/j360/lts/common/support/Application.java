package me.j360.lts.common.support;


import me.j360.lts.ec.EventCenter;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         �����洢 ���������
 */
public abstract class Application {

    // �ڵ�������Ϣ
    private Config config;


    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public EventCenter getEventCenter() {
        return eventCenter;
    }

    public void setEventCenter(EventCenter eventCenter) {
        this.eventCenter = eventCenter;
    }

    // �¼�����
    private EventCenter eventCenter;

}
