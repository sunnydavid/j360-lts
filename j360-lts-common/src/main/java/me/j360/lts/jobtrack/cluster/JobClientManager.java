package me.j360.lts.jobtrack.cluster;

import me.j360.lts.common.cluster.Node;
import me.j360.lts.common.cluster.NodeType;
import me.j360.lts.common.extension.ExtensionLoader;
import me.j360.lts.common.loadbalance.LoadBalance;
import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.support.ConcurrentHashSet;
import me.j360.lts.common.utils.CollectionUtils;
import me.j360.lts.jobtrack.JobTrackerApplication;
import me.j360.lts.jobtrack.channel.ChannelWrapper;
import me.j360.lts.jobtrack.domain.JobClientNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         �ͻ��˽ڵ����
 */
public class JobClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobClientManager.class);

    private final ConcurrentHashMap<String/*nodeGroup*/, Set<JobClientNode>> NODE_MAP = new ConcurrentHashMap<String, Set<JobClientNode>>();

    private LoadBalance loadBalance;
    private JobTrackerApplication application;

    public JobClientManager(JobTrackerApplication application) {
        this.application = application;
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getAdaptiveExtension();
    }

    /**
     * get all connected node group
     *
     * @return
     */
    public Set<String> getNodeGroups() {
        return NODE_MAP.keySet();
    }

    /**
     * ��ӽڵ�
     *
     * @param node
     */
    public void addNode(Node node) {
        //  channel ����Ϊ null
        ChannelWrapper channel = application.getChannelManager().getChannel(node.getGroup(), node.getNodeType(), node.getIdentity());
        Set<JobClientNode> jobClientNodes = NODE_MAP.get(node.getGroup());

        if (jobClientNodes == null) {
            jobClientNodes = new ConcurrentHashSet<JobClientNode>();
            Set<JobClientNode> oldSet = NODE_MAP.putIfAbsent(node.getGroup(), jobClientNodes);
            if (oldSet != null) {
                jobClientNodes = oldSet;
            }
        }

        JobClientNode jobClientNode = new JobClientNode(node.getGroup(), node.getIdentity(), channel);
        LOGGER.info("add JobClient node:{}", jobClientNode);
        jobClientNodes.add(jobClientNode);

        // create feedback queue
        //application.getJobFeedbackQueue().createQueue(node.getGroup());
        //application.getNodeGroupStore().addNodeGroup(NodeType.JOB_CLIENT, node.getGroup());
    }

    /**
     * ɾ���ڵ�
     *
     * @param node
     */
    public void removeNode(Node node) {
        Set<JobClientNode> jobClientNodes = NODE_MAP.get(node.getGroup());
        if (jobClientNodes != null && jobClientNodes.size() != 0) {
            for (JobClientNode jobClientNode : jobClientNodes) {
                if (node.getIdentity().equals(jobClientNode.getIdentity())) {
                    LOGGER.info("remove JobClient node:{}", jobClientNode);
                    jobClientNodes.remove(jobClientNode);
                }
            }
        }
    }

    /**
     * �õ� ���õ� �ͻ��˽ڵ�
     *
     * @param nodeGroup
     * @return
     */
    public JobClientNode getAvailableJobClient(String nodeGroup) {

        Set<JobClientNode> jobClientNodes = NODE_MAP.get(nodeGroup);

        if (CollectionUtils.isEmpty(jobClientNodes)) {
            return null;
        }

        List<JobClientNode> list = new ArrayList<JobClientNode>(jobClientNodes);

        while (list.size() > 0) {

            JobClientNode jobClientNode = loadBalance.select(application.getConfig(), list, null);

            if (jobClientNode != null && (jobClientNode.getChannel() == null || jobClientNode.getChannel().isClosed())) {
                ChannelWrapper channel = application.getChannelManager().getChannel(jobClientNode.getNodeGroup(), NodeType.JOB_CLIENT, jobClientNode.getIdentity());
                if (channel != null) {
                    // ����channel
                    jobClientNode.setChannel(channel);
                }
            }

            if (jobClientNode != null && jobClientNode.getChannel() != null && !jobClientNode.getChannel().isClosed()) {
                return jobClientNode;
            } else {
                list.remove(jobClientNode);
            }
        }
        return null;
    }

}
