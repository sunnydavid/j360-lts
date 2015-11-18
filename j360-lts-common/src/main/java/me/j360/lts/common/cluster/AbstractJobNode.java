package me.j360.lts.common.cluster;

import com.lts.core.Application;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.GenericsUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Constants;
import com.lts.core.extension.ExtensionLoader;
import com.lts.core.factory.JobNodeConfigFactory;
import com.lts.core.factory.NodeFactory;
import com.lts.core.listener.MasterChangeListener;
import com.lts.core.listener.MasterElectionListener;
import com.lts.core.listener.NodeChangeListener;
import com.lts.core.listener.SelfChangeListener;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.command.CommandBodyWrapper;
import com.lts.core.registry.*;
import com.lts.ec.EventCenterFactory;
import com.lts.remoting.RemotingTransporter;
import com.lts.remoting.serialize.AdaptiveSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 8/15/14.
 *         ����ڵ�
 */
public abstract class AbstractJobNode<T extends Node, App extends Application> implements JobNode {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JobNode.class);

    protected Registry registry;
    protected T node;
    protected Config config;
    protected App application;
    private List<NodeChangeListener> nodeChangeListeners;
    private List<MasterChangeListener> masterChangeListeners;
    private EventCenterFactory eventCenterFactory = ExtensionLoader
            .getExtensionLoader(EventCenterFactory.class).getAdaptiveExtension();
    protected RemotingTransporter remotingTransporter = ExtensionLoader
            .getExtensionLoader(RemotingTransporter.class).getAdaptiveExtension();
    private AtomicBoolean started = new AtomicBoolean(false);

    public AbstractJobNode() {
        application = getApplication();
        config = JobNodeConfigFactory.getDefaultConfig();
        application.setConfig(config);
        nodeChangeListeners = new ArrayList<NodeChangeListener>();
        masterChangeListeners = new ArrayList<MasterChangeListener>();
    }

    final public void start() {
        try {
            if (started.compareAndSet(false, true)) {
                // ��ʼ������
                initConfig();

                beforeRemotingStart();

                remotingStart();

                afterRemotingStart();

                initRegistry();

                registry.register(node);

                LOGGER.info("Start success!");
            }
        } catch (Throwable e) {
            if (e.getMessage().contains("Address already in use")) {
                LOGGER.error("Start failed at listen port {}!", config.getListenPort(), e);
            } else {
                LOGGER.error("Start failed!", e);
            }
        }
    }

    final public void stop() {
        try {
            if (started.compareAndSet(true, false)) {

                if (registry != null) {
                    registry.unregister(node);
                }

                beforeRemotingStop();

                remotingStop();

                afterRemotingStop();

                LOGGER.info("Stop success!");
            }
        } catch (Throwable e) {
            LOGGER.error("Stop failed!", e);
        }
    }

    @Override
    public void destroy() {
        try {
            registry.destroy();
            LOGGER.info("Destroy success!");
        } catch (Throwable e) {
            LOGGER.error("Destroy failed!", e);
        }
    }

    protected void initConfig() {
        application.setEventCenter(eventCenterFactory.getEventCenter(config));

        application.setCommandBodyWrapper(new CommandBodyWrapper(config));
        application.setMasterElector(new MasterElector(application));
        application.getMasterElector().addMasterChangeListener(masterChangeListeners);
        application.setRegistryStatMonitor(new RegistryStatMonitor(application));

        node = NodeFactory.create(getNodeClass(), config);
        config.setNodeType(node.getNodeType());

        LOGGER.info("Current Node config :{}", config);

        // ���ĵ�node����
        SubscribedNodeManager subscribedNodeManager = new SubscribedNodeManager(application);
        application.setSubscribedNodeManager(subscribedNodeManager);
        nodeChangeListeners.add(subscribedNodeManager);
        // ����masterѡ�ٵļ�����
        nodeChangeListeners.add(new MasterElectionListener(application));
        // �����Լ��ڵ�仯���磬��ǰ�ڵ㱻�����ˣ�
        nodeChangeListeners.add(new SelfChangeListener(application));

        // ����Ĭ�����л���ʽ
        String defaultSerializable = config.getParameter(Constants.DEFAULT_REMOTING_SERIALIZABLE);
        if (StringUtils.isNotEmpty(defaultSerializable)) {
            AdaptiveSerializable.setDefaultSerializable(defaultSerializable);
        }
    }

    private void initRegistry() {
        registry = RegistryFactory.getRegistry(application);
        if (registry instanceof AbstractRegistry) {
            ((AbstractRegistry) registry).setNode(node);
        }
        registry.subscribe(node, new NotifyListener() {
            private final Logger NOTIFY_LOGGER = LoggerFactory.getLogger(NotifyListener.class);

            @Override
            public void notify(NotifyEvent event, List<Node> nodes) {
                if (CollectionUtils.isEmpty(nodes)) {
                    return;
                }
                switch (event) {
                    case ADD:
                        for (NodeChangeListener listener : nodeChangeListeners) {
                            try {
                                listener.addNodes(nodes);
                            } catch (Throwable t) {
                                NOTIFY_LOGGER.error("{} add nodes failed , cause: {}", listener.getClass().getName(), t.getMessage(), t);
                            }
                        }
                        break;
                    case REMOVE:
                        for (NodeChangeListener listener : nodeChangeListeners) {
                            try {
                                listener.removeNodes(nodes);
                            } catch (Throwable t) {
                                NOTIFY_LOGGER.error("{} remove nodes failed , cause: {}", listener.getClass().getName(), t.getMessage(), t);
                            }
                        }
                        break;
                }
            }
        });
    }

    protected abstract void remotingStart();

    protected abstract void remotingStop();

    protected abstract void beforeRemotingStart();

    protected abstract void afterRemotingStart();

    protected abstract void beforeRemotingStop();

    protected abstract void afterRemotingStop();

    @SuppressWarnings("unchecked")
    private App getApplication() {
        try {
            return ((Class<App>)
                    GenericsUtils.getSuperClassGenericType(this.getClass(), 1))
                    .newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> getNodeClass() {
        return (Class<T>)
                GenericsUtils.getSuperClassGenericType(this.getClass(), 0);
    }


    /**
     * ����zookeeperע�����ĵ�ַ
     */
    public void setRegistryAddress(String registryAddress) {
        config.setRegistryAddress(registryAddress);
    }

    /**
     * ����Զ�̵��ó�ʱʱ��
     */
    public void setInvokeTimeoutMillis(int invokeTimeoutMillis) {
        config.setInvokeTimeoutMillis(invokeTimeoutMillis);
    }

    /**
     * ���ü�Ⱥ����
     */
    public void setClusterName(String clusterName) {
        config.setClusterName(clusterName);
    }

    /**
     * �ڵ��ʶ(����Ҫ��֤�����ʶ��Ψһ�Ĳ������ã����������)
     * ����ǷǱ������õģ�����ʹ��ϵͳĬ������
     */
    public void setIdentity(String identity) {
        config.setIdentity(identity);
    }

    /**
     * ��ӽڵ������
     */
    public void addNodeChangeListener(NodeChangeListener notifyListener) {
        if (notifyListener != null) {
            nodeChangeListeners.add(notifyListener);
        }
    }

    /**
     * ��� master �ڵ�仯������
     */
    public void addMasterChangeListener(MasterChangeListener masterChangeListener) {
        if (masterChangeListener != null) {
            masterChangeListeners.add(masterChangeListener);
        }
    }

    public void setDataPath(String path) {
        if (StringUtils.isNotEmpty(path)) {
            config.setDataPath(path);
        }
    }

    /**
     * ���ö�������ò���
     */
    public void addConfig(String key, String value) {
        config.setParameter(key, value);
    }
}
