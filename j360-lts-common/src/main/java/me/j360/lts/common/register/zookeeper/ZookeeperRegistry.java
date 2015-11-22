package me.j360.lts.common.register.zookeeper;


import me.j360.lts.common.cluster.Node;
import me.j360.lts.common.cluster.NodeType;
import me.j360.lts.common.extension.ExtensionLoader;
import me.j360.lts.common.register.FailbackRegistry;
import me.j360.lts.common.register.NodeRegistryUtils;
import me.j360.lts.common.register.NotifyEvent;
import me.j360.lts.common.register.NotifyListener;
import me.j360.lts.common.support.Application;
import me.j360.lts.common.utils.CollectionUtils;
import me.j360.lts.common.zookeeper.ChildListener;
import me.j360.lts.common.zookeeper.StateListener;
import me.j360.lts.common.zookeeper.ZookeeperClient;
import me.j360.lts.common.zookeeper.ZookeeperTransporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 *         �ڵ�ע�������������Լ���ע�Ľڵ�
 */
public class ZookeeperRegistry extends FailbackRegistry {

    private ZookeeperClient zkClient;
    // ������¼���ڵ��µ��ӽڵ�ı仯
    private final ConcurrentHashMap<String/*parentPath*/, List<String/*children*/>> cachedChildrenNodeMap;

    private final ConcurrentMap<Node, ConcurrentMap<NotifyListener, ChildListener>> zkListeners;

    private String clusterName;

    public ZookeeperRegistry(final Application application) {
        super(application);
        this.clusterName = application.getConfig().getClusterName();
        this.cachedChildrenNodeMap = new ConcurrentHashMap<String, List<String>>();
        ZookeeperTransporter zookeeperTransporter = ExtensionLoader.getExtensionLoader(ZookeeperTransporter.class).getAdaptiveExtension();
        this.zkClient = zookeeperTransporter.connect(application.getConfig());
        this.zkListeners = new ConcurrentHashMap<Node, ConcurrentMap<NotifyListener, ChildListener>>();
        // Ĭ�������ɹ���(��zkclientʱ�򣬵�һ�β�����state changedʱ�䱩¶���û���
        // ����Ȼ��new ZkClient��ʱ���ֱ�������ˣ������ṩlistener�Ĺ��캯�����߰�������Ϊstart������okѽ������)
        application.getRegistryStatMonitor().setAvailable(true);

        zkClient.addStateListener(new StateListener() {
            @Override
            public void stateChanged(int state) {
                if (state == DISCONNECTED) {
                    application.getRegistryStatMonitor().setAvailable(false);
                } else if (state == CONNECTED) {
                    application.getRegistryStatMonitor().setAvailable(true);
                } else if (state == RECONNECTED) {
                    try {
                        application.getRegistryStatMonitor().setAvailable(true);
                        recover();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

    @Override
    protected void doRegister(Node node) {
        if (zkClient.exists(node.toFullString())) {
            return;
        }
        zkClient.create(node.toFullString(), true, false);
    }

    @Override
    protected void doUnRegister(Node node) {
        zkClient.delete(node.toFullString());
    }

    @Override
    protected void doSubscribe(Node node, NotifyListener listener) {
        List<NodeType> listenNodeTypes = node.getListenNodeTypes();
        if (CollectionUtils.isEmpty(listenNodeTypes)) {
            return;
        }
        for (NodeType listenNodeType : listenNodeTypes) {
            String listenNodePath = NodeRegistryUtils.getNodeTypePath(clusterName, listenNodeType);

            ChildListener zkListener = addZkListener(node, listener);

            // Ϊ�Լ���ע�� �ڵ� ��Ӽ���
            List<String> children = zkClient.addChildListener(listenNodePath, zkListener);

            if (CollectionUtils.isNotEmpty(children)) {
                List<Node> listenedNodes = new ArrayList<Node>();
                for (String child : children) {
                    Node listenedNode = NodeRegistryUtils.parse(listenNodePath + "/" + child);
                    listenedNodes.add(listenedNode);
                }
                notify(NotifyEvent.ADD, listenedNodes, listener);
                cachedChildrenNodeMap.put(listenNodePath, children);
            }
        }
    }

    @Override
    protected void doUnsubscribe(Node node, NotifyListener listener) {
        ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(node);
        if (listeners != null) {
            ChildListener zkListener = listeners.get(listener);
            if (zkListener != null) {
                List<NodeType> listenNodeTypes = node.getListenNodeTypes();
                if (CollectionUtils.isEmpty(listenNodeTypes)) {
                    return;
                }
                for (NodeType listenNodeType : listenNodeTypes) {
                    String listenNodePath = NodeRegistryUtils.getNodeTypePath(clusterName, listenNodeType);
                    zkClient.removeChildListener(listenNodePath, zkListener);
                }
            }
        }
    }


    private ChildListener addZkListener(Node node, final NotifyListener listener) {

        ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(node);
        if (listeners == null) {
            zkListeners.putIfAbsent(node, new ConcurrentHashMap<NotifyListener, ChildListener>());
            listeners = zkListeners.get(node);
        }
        ChildListener zkListener = listeners.get(listener);
        if (zkListener == null) {

            listeners.putIfAbsent(listener, new ChildListener() {

                public void childChanged(String parentPath, List<String> currentChildren) {

                    if (CollectionUtils.isEmpty(currentChildren)) {
                        currentChildren = new ArrayList<String>(0);
                    }

                    List<String> oldChildren = cachedChildrenNodeMap.get(parentPath);
                    // 1. �ҳ����ӵ� �ڵ�
                    List<String> addChildren = CollectionUtils.getLeftDiff(currentChildren, oldChildren);
                    // 2. �ҳ����ٵ� �ڵ�
                    List<String> decChildren = CollectionUtils.getLeftDiff(oldChildren, currentChildren);

                    if (CollectionUtils.isNotEmpty(addChildren)) {

                        List<Node> nodes = new ArrayList<Node>(addChildren.size());
                        for (String child : addChildren) {
                            Node node = NodeRegistryUtils.parse(parentPath + "/" + child);
                            nodes.add(node);
                        }
                        ZookeeperRegistry.this.notify(NotifyEvent.ADD, nodes, listener);
                    }

                    if (CollectionUtils.isNotEmpty(decChildren)) {
                        List<Node> nodes = new ArrayList<Node>(addChildren.size());
                        for (String child : decChildren) {
                            Node node = NodeRegistryUtils.parse(parentPath + "/" + child);
                            nodes.add(node);
                        }
                        ZookeeperRegistry.this.notify(NotifyEvent.REMOVE, nodes, listener);
                    }
                    cachedChildrenNodeMap.put(parentPath, currentChildren);
                }
            });
            zkListener = listeners.get(listener);
        }
        return zkListener;
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            zkClient.close();
        } catch (Exception e) {
            LOGGER.warn("Failed to close zookeeper client " + getNode() + ", cause: " + e.getMessage(), e);
        }
    }
}

