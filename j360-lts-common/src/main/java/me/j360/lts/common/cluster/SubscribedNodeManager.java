package me.j360.lts.common.cluster;


import me.j360.lts.common.constant.EcTopic;
import me.j360.lts.common.listener.NodeChangeListener;
import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.support.Application;
import me.j360.lts.common.support.ConcurrentHashSet;
import me.j360.lts.common.utils.CollectionUtils;
import me.j360.lts.common.utils.ListUtils;
import me.j360.lts.ec.EventInfo;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 *         �ڵ���� (��Ҫ���ڹ����Լ���ע�Ľڵ�)
 */
public class SubscribedNodeManager implements NodeChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribedNodeManager.class);
    private final ConcurrentHashMap<NodeType, Set<Node>> NODES = new ConcurrentHashMap<NodeType, Set<Node>>();

    private Application application;

    public SubscribedNodeManager(Application application) {
        this.application = application;
    }

    /**
     * ��Ӽ����Ľڵ�
     */
    private void addNode(Node node) {
        if ((NodeType.JOB_TRACKER.equals(node.getNodeType()))) {
            // ������ӵ�JobTracker�ڵ㣬��ôֱ����ӣ���Ϊ���ֽڵ㶼��Ҫ����
            _addNode(node);
        } else if (NodeType.JOB_TRACKER.equals(application.getConfig().getNodeType())) {
            // �������ڵ���JobTracker�ڵ㣬��ôֱ����ӣ���ΪJobTracker�ڵ�Ҫ�������ֽڵ�
            _addNode(node);
        } else if (application.getConfig().getNodeType().equals(node.getNodeType())
                && application.getConfig().getNodeGroup().equals(node.getGroup())) {
            // ʣ�����������JobClient��TaskTracker��ֻ�������Լ�ͬһ��group�Ľڵ�
            _addNode(node);
        }
    }

    private void _addNode(Node node) {
        Set<Node> nodeSet = NODES.get(node.getNodeType());
        if (CollectionUtils.isEmpty(nodeSet)) {
            nodeSet = new ConcurrentHashSet<Node>();
            Set<Node> oldNodeList = NODES.putIfAbsent(node.getNodeType(), nodeSet);
            if (oldNodeList != null) {
                nodeSet = oldNodeList;
            }
        }
        nodeSet.add(node);
        EventInfo eventInfo = new EventInfo(EcTopic.NODE_ADD);
        eventInfo.setParam("node", node);
        application.getEventCenter().publishSync(eventInfo);
        LOGGER.info("Add {}", node);
    }

    public List<Node> getNodeList(final NodeType nodeType, final String nodeGroup) {

        Set<Node> nodes = NODES.get(nodeType);

        return ListUtils.filter(CollectionUtils.setToList(nodes), new ListUtils.Filter<Node>() {
            @Override
            public boolean filter(Node node) {
                return node.getGroup().equals(nodeGroup);
            }
        });
    }

    public List<Node> getNodeList(NodeType nodeType) {
        return CollectionUtils.setToList(NODES.get(nodeType));
    }

    private void removeNode(Node delNode) {
        Set<Node> nodeSet = NODES.get(delNode.getNodeType());

        if (CollectionUtils.isNotEmpty(nodeSet)) {
            for (Node node : nodeSet) {
                if (node.getIdentity().equals(delNode.getIdentity())) {
                    nodeSet.remove(node);
                    EventInfo eventInfo = new EventInfo(EcTopic.NODE_REMOVE);
                    eventInfo.setParam("node", node);
                    application.getEventCenter().publishSync(eventInfo);
                    LOGGER.info("Remove {}", node);
                }
            }
        }
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            addNode(node);
        }
    }

    @Override
    public void removeNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            removeNode(node);
        }
    }
}
