package me.j360.lts.common.remoting;

import me.j360.lts.common.cluster.Node;
import me.j360.lts.common.constant.EcTopic;
import me.j360.lts.common.exception.JobTrackerNotFoundException;
import me.j360.lts.common.extension.ExtensionLoader;
import me.j360.lts.common.loadbalance.LoadBalance;
import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.support.Application;
import me.j360.lts.ec.EventInfo;
import me.j360.lts.remote.AsyncCallback;
import me.j360.lts.remote.RemotingClient;
import me.j360.lts.remote.RemotingProcessor;
import me.j360.lts.remote.exception.RemotingException;
import me.j360.lts.remote.protocol.RemotingCommand;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14.
 */
public class RemotingClientDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingClientDelegate.class);

    private RemotingClient remotingClient;
    // ��JobTracker�ĸ��ؾ����㷨
    private LoadBalance loadBalance;
    private Application application;

    // JobTracker �Ƿ����
    private volatile boolean serverEnable = false;
    private List<Node> jobTrackers;

    public RemotingClientDelegate(RemotingClient remotingClient, Application application) {
        this.remotingClient = remotingClient;
        this.application = application;
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getAdaptiveExtension();
        this.jobTrackers = new CopyOnWriteArrayList<Node>();
    }

    private Node getJobTrackerNode() throws JobTrackerNotFoundException {
        try {
            if (jobTrackers.size() == 0) {
                throw new JobTrackerNotFoundException("no available jobTracker!");
            }
            return loadBalance.select(application.getConfig(), jobTrackers,
                    application.getConfig().getIdentity());
        } catch (JobTrackerNotFoundException e) {
            this.serverEnable = false;
            // publish msg
            EventInfo eventInfo = new EventInfo(EcTopic.NO_JOB_TRACKER_AVAILABLE);
            application.getEventCenter().publishAsync(eventInfo);
            throw e;
        }
    }

    public void start() {
        try {
            remotingClient.start();
        } catch (RemotingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean contains(Node jobTracker) {
        return jobTrackers.contains(jobTracker);
    }

    public void addJobTracker(Node jobTracker) {
        if (!contains(jobTracker)) {
            jobTrackers.add(jobTracker);
        }
    }

    public boolean removeJobTracker(Node jobTracker) {
        return jobTrackers.remove(jobTracker);
    }

    /**
     * ͬ������
     */
    public RemotingCommand invokeSync(RemotingCommand request)
            throws JobTrackerNotFoundException {

        Node jobTracker = getJobTrackerNode();

        try {
            RemotingCommand response = remotingClient.invokeSync(jobTracker.getAddress(),
                    request, application.getConfig().getInvokeTimeoutMillis());
            this.serverEnable = true;
            return response;
        } catch (Exception e) {
            // �����JobTracker�Ƴ�
            jobTrackers.remove(jobTracker);
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
            // ֻҪ���ǽڵ� ������, ��ѯ���нڵ�����
            return invokeSync(request);
        }
    }

    /**
     * �첽����
     */
    public void invokeAsync(RemotingCommand request, AsyncCallback asyncCallback)
            throws JobTrackerNotFoundException {

        Node jobTracker = getJobTrackerNode();

        try {
            remotingClient.invokeAsync(jobTracker.getAddress(), request,
                    application.getConfig().getInvokeTimeoutMillis(), asyncCallback);
            this.serverEnable = true;
        } catch (Throwable e) {
            // �����JobTracker�Ƴ�
            jobTrackers.remove(jobTracker);
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
            // ֻҪ���ǽڵ� ������, ��ѯ���нڵ�����
            invokeAsync(request, asyncCallback);
        }
    }

    /**
     * �������
     */
    public void invokeOneway(RemotingCommand request)
            throws JobTrackerNotFoundException {

        Node jobTracker = getJobTrackerNode();

        try {
            remotingClient.invokeOneway(jobTracker.getAddress(), request,
                    application.getConfig().getInvokeTimeoutMillis());
            this.serverEnable = true;
        } catch (Throwable e) {
            // �����JobTracker�Ƴ�
            jobTrackers.remove(jobTracker);
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
            // ֻҪ���ǽڵ� ������, ��ѯ���нڵ�����
            invokeOneway(request);
        }
    }

    public void registerProcessor(int requestCode, RemotingProcessor processor,
                                  ExecutorService executor) {
        remotingClient.registerProcessor(requestCode, processor, executor);
    }

    public void registerDefaultProcessor(RemotingProcessor processor, ExecutorService executor) {
        remotingClient.registerDefaultProcessor(processor, executor);
    }

    public boolean isServerEnable() {
        return serverEnable;
    }

    public void setServerEnable(boolean serverEnable) {
        this.serverEnable = serverEnable;
    }

    public void shutdown() {
        remotingClient.shutdown();
    }

    public RemotingClient getRemotingClient() {
        return remotingClient;
    }
}
