package me.j360.lts.queue;


import me.j360.lts.common.cluster.NodeType;
import me.j360.lts.common.support.NodeGroupGetRequest;
import me.j360.lts.common.web.response.PageResponse;
import me.j360.lts.queue.domain.NodeGroupPo;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public interface NodeGroupStore {

    /**
     * ��� NodeGroup
     *
     * @param nodeType
     * @param name
     */
    void addNodeGroup(NodeType nodeType, String name);

    /**
     * �Ƴ� NodeGroup
     *
     * @param nodeType
     * @param name
     */
    void removeNodeGroup(NodeType nodeType, String name);

    /**
     * �õ�ĳ��nodeType ������ nodeGroup
     *
     * @param nodeType
     * @return
     */
    List<NodeGroupPo> getNodeGroup(NodeType nodeType);

    /**
     * ��ҳ��ѯ
     */
    PageResponse<NodeGroupPo> getNodeGroup(NodeGroupGetRequest request);
}
