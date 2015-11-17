package me.j360.lts.runner.support;


import me.j360.lts.common.constant.Constants;
import me.j360.lts.common.constant.EcTopic;
import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.ec.EventInfo;
import me.j360.lts.ec.EventSubscriber;
import me.j360.lts.ec.Observer;
import me.j360.lts.runner.domain.TaskTrackerApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ������JobTrackerȥȡ����
 * 1. �ᶩ��JobTracker�Ŀ���,��������Ϣ����Ķ���
 * 2. ֻ�е�JobTracker���õ�ʱ��Ż�ȥPull����
 * 3. Pullֻ�ǻ��JobTracker����һ��֪ͨ
 * Robert HG (254963746@qq.com) on 3/25/15.
 */
public class JobPullMachine {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobPullMachine.class.getSimpleName());

    // ��ʱ���TaskTracker�Ƿ��п��е��̣߳�����У���ô��JobTracker��������pull����
    private final ScheduledExecutorService SCHEDULED_CHECKER = Executors.newScheduledThreadPool(1);
    private ScheduledFuture scheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);
    private TaskTrackerApplication application;
    private Runnable runnable;
    private int jobPullFrequency;

    public JobPullMachine(final TaskTrackerApplication application) {
        this.application = application;
        this.jobPullFrequency = application.getConfig().getParameter(Constants.JOB_PULL_FREQUENCY, Constants.DEFAULT_JOB_PULL_FREQUENCY);

        application.getEventCenter().subscribe(
                new EventSubscriber(JobPullMachine.class.getSimpleName().concat(application.getConfig().getIdentity()),
                        new Observer() {
                            @Override
                            public void onObserved(EventInfo eventInfo) {
                                if (EcTopic.JOB_TRACKER_AVAILABLE.equals(eventInfo.getTopic())) {
                                    // JobTracker ������
                                    start();
                                } else if (EcTopic.NO_JOB_TRACKER_AVAILABLE.equals(eventInfo.getTopic())) {
                                    stop();
                                }
                            }
                        }), EcTopic.JOB_TRACKER_AVAILABLE, EcTopic.NO_JOB_TRACKER_AVAILABLE);
        this.runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (!start.get()) {
                        return;
                    }
                    //sendRequest();
                } catch (Exception e) {
                    LOGGER.error("Job pull machine run error!", e);
                }
            }
        };
    }

    private void start() {
        try {
            if (start.compareAndSet(false, true)) {
                if (scheduledFuture == null) {
                    scheduledFuture = SCHEDULED_CHECKER.scheduleWithFixedDelay(runnable, 1, jobPullFrequency, TimeUnit.SECONDS);
                    // 5s ���һ���Ƿ��п����߳�
                }
                LOGGER.info("Start job pull machine success!");
            }
        } catch (Throwable t) {
            LOGGER.error("Start job pull machine failed!", t);
        }
    }

    private void stop() {
        try {
            if (start.compareAndSet(true, false)) {
//                scheduledFuture.cancel(true);
//                SCHEDULED_CHECKER.shutdown();
                LOGGER.info("Stop job pull machine success!");
            }
        } catch (Throwable t) {
            LOGGER.error("Stop job pull machine failed!", t);
        }
    }

    /**
     * ����Job pull ����
     */
    /*private void sendRequest() throws RemotingCommandFieldCheckException {
        int availableThreads = application.getRunnerPool().getAvailablePoolSize();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("current availableThreads:{}", availableThreads);
        }
        if (availableThreads == 0) {
            return;
        }
        JobPullRequest requestBody = application.getCommandBodyWrapper().wrapper(new JobPullRequest());
        requestBody.setAvailableThreads(availableThreads);
        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_PULL.code(), requestBody);

        try {
            RemotingCommand responseCommand = application.getRemotingClient().invokeSync(request);
            if (responseCommand == null) {
                LOGGER.warn("job pull request failed! response command is null!");
                return;
            }
            if (JobProtos.ResponseCode.JOB_PULL_SUCCESS.code() == responseCommand.getCode()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("job pull request success!");
                }
                return;
            }
            LOGGER.warn("job pull request failed! response command is null!");
        } catch (JobTrackerNotFoundException e) {
            LOGGER.warn("no job tracker available!");
        }
    }*/
}
