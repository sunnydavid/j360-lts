package me.j360.lts.jobtrack.cluster;


import me.j360.lts.common.cluster.Node;
import me.j360.lts.common.cluster.NodeType;
import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.support.ConcurrentHashSet;
import me.j360.lts.jobtrack.JobTrackerApplication;
import me.j360.lts.jobtrack.channel.ChannelWrapper;
import me.j360.lts.jobtrack.domain.TaskTrackerNode;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 *         Task Tracker ������ (�� TaskTracker �ڵ�ļ�¼ �� �����̵߳ļ�¼)
 */
public class TaskTrackerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskTrackerManager.class);
    // ����
    private final ConcurrentHashMap<String/*nodeGroup*/, Set<TaskTrackerNode>> NODE_MAP = new ConcurrentHashMap<String, Set<TaskTrackerNode>>();
    private JobTrackerApplication application;

    public TaskTrackerManager(JobTrackerApplication application) {
        this.application = application;
    }

    /**
     * get all connected node group
     */
    public Set<String> getNodeGroups() {
        return NODE_MAP.keySet();
    }

    /**
     * ��ӽڵ�
     */
    public void addNode(Node node) {
        //  channel ����Ϊ null
        ChannelWrapper channel = application.getChannelManager().getChannel(node.getGroup(),
                node.getNodeType(), node.getIdentity());
        Set<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(node.getGroup());

        if (taskTrackerNodes == null) {
            taskTrackerNodes = new ConcurrentHashSet<TaskTrackerNode>();
            Set<TaskTrackerNode> oldSet = NODE_MAP.putIfAbsent(node.getGroup(), taskTrackerNodes);
            if (oldSet != null) {
                taskTrackerNodes = oldSet;
            }
        }

        TaskTrackerNode taskTrackerNode = new TaskTrackerNode(node.getGroup(),
                node.getThreads(), node.getIdentity(), channel);
        LOGGER.info("Add TaskTracker node:{}", taskTrackerNode);
        taskTrackerNodes.add(taskTrackerNode);

        // create executable queue
        //application.getExecutableJobQueue().createQueue(node.getGroup());
        //application.getNodeGroupStore().addNodeGroup(NodeType.TASK_TRACKER, node.getGroup());
    }

    /**
     * ɾ���ڵ�
     *
     * @param node
     */
    public void removeNode(Node node) {
        Set<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(node.getGroup());
        if (taskTrackerNodes != null && taskTrackerNodes.size() != 0) {
            TaskTrackerNode taskTrackerNode = new TaskTrackerNode(node.getIdentity());
            taskTrackerNode.setNodeGroup(node.getGroup());
            LOGGER.info("Remove TaskTracker node:{}", taskTrackerNode);
            taskTrackerNodes.remove(taskTrackerNode);
        }
    }

    public TaskTrackerNode getTaskTrackerNode(String nodeGroup, String identity) {
        Set<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(nodeGroup);
        if (taskTrackerNodes == null || taskTrackerNodes.size() == 0) {
            return null;
        }

        for (TaskTrackerNode taskTrackerNode : taskTrackerNodes) {
            if (taskTrackerNode.getIdentity().equals(identity)) {
                if (taskTrackerNode.getChannel() == null || taskTrackerNode.getChannel().isClosed()) {
                    // ��� channel �Ѿ��ر�, ����channel, ���û��channel, �Թ�
                    ChannelWrapper channel = application.getChannelManager().getChannel(
                            taskTrackerNode.getNodeGroup(), NodeType.TASK_TRACKER, taskTrackerNode.getIdentity());
                    if (channel != null) {
                        // ����channel
                        taskTrackerNode.setChannel(channel);
                        LOGGER.info("update node channel , taskTackerNode={}", taskTrackerNode);
                        return taskTrackerNode;
                    }
                } else {
                    // ֻ�е�channel������ʱ��ŷ���
                    return taskTrackerNode;
                }
            }
        }
        return null;
    }

    /**
     * ���½ڵ�� �����߳���
     *
     * @param nodeGroup
     * @param identity
     * @param availableThreads
     * @param timestamp        ʱ���, ֻ�е� ʱ��������ϴθ��µ�ʱ�� �Ÿ��¿����߳���
     */
    public void updateTaskTrackerAvailableThreads(
            String nodeGroup,
            String identity,
            Integer availableThreads,
            Long timestamp) {

        Set<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(nodeGroup);

        if (taskTrackerNodes != null && taskTrackerNodes.size() != 0) {
            for (TaskTrackerNode trackerNode : taskTrackerNodes) {
                if (trackerNode.getIdentity().equals(identity) && (trackerNode.getTimestamp() == null || trackerNode.getTimestamp() <= timestamp)) {
                    trackerNode.setAvailableThread(availableThreads);
                    trackerNode.setTimestamp(timestamp);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("���½ڵ��߳���: {}", trackerNode);
                    }
                }
            }
        }
    }
}
