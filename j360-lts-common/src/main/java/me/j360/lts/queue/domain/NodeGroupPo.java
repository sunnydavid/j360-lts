package me.j360.lts.queue.domain;


import me.j360.lts.common.cluster.NodeType;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public class NodeGroupPo {

    private NodeType nodeType;
    /**
     * ����
     */
    private String name;
    /**
     * ����ʱ��
     */
    private Long gmtCreated;

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }
}
