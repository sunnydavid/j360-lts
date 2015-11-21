package me.j360.lts.queue;

import me.j360.lts.queue.domain.JobPo;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
public interface PreLoader {

    public JobPo take(String taskTrackerNodeGroup, String taskTrackerIdentity);

    /**
     * ���taskTrackerNodeGroupΪ�գ���ôload���е�
     */
    public void load(String taskTrackerNodeGroup);
}
