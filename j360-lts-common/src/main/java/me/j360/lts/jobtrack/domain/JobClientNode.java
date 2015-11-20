package me.j360.lts.jobtrack.domain;

import me.j360.lts.jobtrack.channel.ChannelWrapper;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 * �ͻ��˽ڵ�
 */
public class JobClientNode {

    // �ڵ�������
    public String nodeGroup;
    // Ψһ��ʶ
    public String identity;
    // �ýڵ��channel
    public ChannelWrapper channel;

    public JobClientNode(String nodeGroup, String identity, ChannelWrapper channel) {
        this.nodeGroup = nodeGroup;
        this.identity = identity;
        this.channel = channel;
    }

    public JobClientNode(String identity) {
        this.identity = identity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobClientNode)) return false;

        JobClientNode that = (JobClientNode) o;

        if (identity != null ? !identity.equals(that.identity) : that.identity != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identity != null ? identity.hashCode() : 0;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public ChannelWrapper getChannel() {
        return channel;
    }

    public void setChannel(ChannelWrapper channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "JobClientNode{" +
                "nodeGroup='" + nodeGroup + '\'' +
                ", identity='" + identity + '\'' +
                ", channel=" + channel +
                '}';
    }
}
