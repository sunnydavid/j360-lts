package me.j360.lts.common.test.remote.server.channel;

import me.j360.lts.common.cluster.NodeType;
import me.j360.lts.remote.Channel;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 * ��channel�İ�װ
 */
public class ChannelWrapper {

    private Channel channel;
    private NodeType nodeType;
    private String nodeGroup;
    // �ڵ��Ψһ��ʶ
    private String identity;

    public ChannelWrapper(Channel channel, NodeType nodeType, String nodeGroup, String identity) {
        this.channel = channel;
        this.nodeType = nodeType;
        this.nodeGroup = nodeGroup;
        this.identity = identity;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
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

    public boolean isOpen() {
        return channel.isOpen();
    }

    public boolean isClosed() {
        return channel.isClosed();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChannelWrapper)) return false;

        ChannelWrapper that = (ChannelWrapper) o;

        if (channel != null ? !channel.equals(that.channel) : that.channel != null) return false;
        if (identity != null ? !identity.equals(that.identity) : that.identity != null) return false;
        if (nodeGroup != null ? !nodeGroup.equals(that.nodeGroup) : that.nodeGroup != null) return false;
        if (nodeType != that.nodeType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = channel != null ? channel.hashCode() : 0;
        result = 31 * result + (nodeType != null ? nodeType.hashCode() : 0);
        result = 31 * result + (nodeGroup != null ? nodeGroup.hashCode() : 0);
        result = 31 * result + (identity != null ? identity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ChannelWrapper{" +
                "channel=" + channel +
                ", nodeType=" + nodeType +
                ", nodeGroup='" + nodeGroup + '\'' +
                ", identity='" + identity + '\'' +
                '}';
    }
}
