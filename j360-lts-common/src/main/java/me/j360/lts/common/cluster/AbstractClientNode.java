package me.j360.lts.common.cluster;

import com.lts.core.Application;
import com.lts.core.constant.Constants;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.remoting.HeartBeatMonitor;
import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.remoting.RemotingClient;
import com.lts.remoting.RemotingClientConfig;
import com.lts.remoting.RemotingProcessor;

import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         ����ͻ���
 */
public abstract class AbstractClientNode<T extends Node, App extends Application> extends AbstractJobNode<T, App> {

    protected RemotingClientDelegate remotingClient;
    private HeartBeatMonitor heartBeatMonitor;

    protected void remotingStart() {
        remotingClient.start();
        heartBeatMonitor.start();

        RemotingProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {
            int processorSize = config.getParameter(Constants.PROCESSOR_THREAD, Constants.DEFAULT_PROCESSOR_THREAD);
            remotingClient.registerDefaultProcessor(defaultProcessor,
                    Executors.newFixedThreadPool(processorSize,
                            new NamedThreadFactory(AbstractClientNode.class.getSimpleName())));
        }
    }

    /**
     * �õ�Ĭ�ϵĴ�����
     */
    protected abstract RemotingProcessor getDefaultProcessor();

    protected void remotingStop() {
        heartBeatMonitor.stop();
        remotingClient.shutdown();
    }

    /**
     * ���ýڵ�����
     */
    public void setNodeGroup(String nodeGroup) {
        config.setNodeGroup(nodeGroup);
    }

    public boolean isServerEnable() {
        return remotingClient.isServerEnable();
    }

    /**
     * ��������JobTracker�ĸ��ؾ����㷨
     *
     * @param loadBalance �㷨 random, consistenthash
     */
    public void setLoadBalance(String loadBalance) {
        config.setParameter("loadbalance", loadBalance);
    }


    @Override
    protected void beforeRemotingStart() {
        //
        this.remotingClient = new RemotingClientDelegate(getRemotingClient(new RemotingClientConfig()), application);
        this.heartBeatMonitor = new HeartBeatMonitor(remotingClient, application);

        beforeStart();
    }

    private RemotingClient getRemotingClient(RemotingClientConfig remotingClientConfig) {
        return remotingTransporter.getRemotingClient(config, remotingClientConfig);
    }


    @Override
    protected void afterRemotingStart() {
        // ����Ҫ����
        afterStart();
    }

    @Override
    protected void beforeRemotingStop() {
        beforeStop();
    }

    @Override
    protected void afterRemotingStop() {
        afterStop();
    }

    protected abstract void beforeStart();

    protected abstract void afterStart();

    protected abstract void afterStop();

    protected abstract void beforeStop();

}
