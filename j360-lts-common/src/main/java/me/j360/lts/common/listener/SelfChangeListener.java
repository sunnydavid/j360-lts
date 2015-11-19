package me.j360.lts.common.listener;

import me.j360.lts.common.cluster.Node;
import me.j360.lts.common.cluster.NodeType;
import me.j360.lts.common.constant.EcTopic;
import me.j360.lts.common.support.Application;
import me.j360.lts.common.support.Config;
import me.j360.lts.common.utils.CollectionUtils;
import me.j360.lts.ec.EventInfo;

import java.util.List;

/**
 * ���������Լ��Ľڵ���Ϣ�仯
 *
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
public class SelfChangeListener implements NodeChangeListener {

    private Config config;
    private Application application;

    public SelfChangeListener(Application application) {
        this.config = application.getConfig();
        this.application = application;
    }


    private void change(Node node) {
        if (node.getIdentity().equals(config.getIdentity())) {
            // �ǵ�ǰ�ڵ�, �����ڵ������Ƿ����仯
            // 1. �� threads ��û�иı� , Ŀǰֻ�� TASK_TRACKER �� threads������
            if (node.getNodeType().equals(NodeType.TASK_TRACKER)
                    && (node.getThreads() != config.getWorkThreads())) {
                config.setWorkThreads(node.getThreads());
                application.getEventCenter().publishAsync(new EventInfo(EcTopic.WORK_THREAD_CHANGE));
            }

            // 2. �� available ��û�иı�
            if (node.isAvailable() != config.isAvailable()) {
                String topic = node.isAvailable() ? EcTopic.NODE_ENABLE : EcTopic.NODE_DISABLE;
                config.setAvailable(node.isAvailable());
                application.getEventCenter().publishAsync(new EventInfo(topic));
            }
        }
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            change(node);
        }
    }

    @Override
    public void removeNodes(List<Node> nodes) {

    }
}
