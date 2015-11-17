package me.j360.lts.runner;


import me.j360.lts.common.constant.EcTopic;
import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.support.ConcurrentHashSet;
import me.j360.lts.common.support.JobWrapper;
import me.j360.lts.ec.EventInfo;
import me.j360.lts.ec.EventSubscriber;
import me.j360.lts.ec.Observer;
import me.j360.lts.runner.domain.TaskTrackerApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         �̳߳ع���
 */
public class RunnerPool {

    private final Logger LOGGER = LoggerFactory.getLogger("LTS.RunnerPool");

    private ThreadPoolExecutor threadPoolExecutor = null;

    private RunnerFactory runnerFactory;
    private TaskTrackerApplication application;
    private RunningJobManager runningJobManager;

    public RunnerPool(final TaskTrackerApplication application) {
        this.application = application;
        this.runningJobManager = new RunningJobManager();

        threadPoolExecutor = initThreadPoolExecutor();

        runnerFactory = application.getRunnerFactory();
        if (runnerFactory == null) {
            runnerFactory = new DefaultRunnerFactory(application);
        }
        // ���¼�����ע���¼�, �ı乤���̴߳�С
        application.getEventCenter().subscribe(
                new EventSubscriber(application.getConfig().getIdentity(), new Observer() {
                    @Override
                    public void onObserved(EventInfo eventInfo) {
                        setMaximumPoolSize(application.getConfig().getWorkThreads());
                    }
                }), EcTopic.WORK_THREAD_CHANGE);
    }

    private ThreadPoolExecutor initThreadPoolExecutor() {
        int maxSize = application.getConfig().getWorkThreads();
        int minSize = 4 > maxSize ? maxSize : 4;

        return new ThreadPoolExecutor(minSize, maxSize, 30, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),           // ֱ���ύ���̶߳�����������
                new ThreadPoolExecutor.AbortPolicy());
    }

    public void execute(JobWrapper jobWrapper, RunnerCallback callback) {
        try {
            threadPoolExecutor.execute(
                    new JobRunnerDelegate(application, jobWrapper, callback));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Receive job success ! " + jobWrapper);
            }
        } catch (RejectedExecutionException e) {
            LOGGER.warn("No more thread to run job .");
        }
    }

    /**
     * �õ���ǰ���õ��߳���
     */
    public int getAvailablePoolSize() {
        return threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount();
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize == 0) {
            throw new IllegalArgumentException("maximumPoolSize can not be zero!");
        }

        int corePollSize = threadPoolExecutor.getCorePoolSize();
        if (maximumPoolSize < corePollSize) {
            threadPoolExecutor.setCorePoolSize(maximumPoolSize);
        }
        threadPoolExecutor.setMaximumPoolSize(maximumPoolSize);

        LOGGER.info("maximumPoolSize update to {}", maximumPoolSize);
    }

    /**
     * �õ�����߳���
     */
    public int getMaximumPoolSize() {
        return threadPoolExecutor.getMaximumPoolSize();
    }

    public RunnerFactory getRunnerFactory() {
        return runnerFactory;
    }

    /**
     * ִ�и÷������̳߳ص�״̬���̱��STOP״̬������ͼֹͣ��������ִ�е��̣߳����ٴ����ڳض����еȴ������񣬵�Ȼ�����᷵����Щδִ�е�����
     * ����ͼ��ֹ�̵߳ķ�����ͨ������Thread.interrupt()������ʵ�ֵģ����Ǵ��֪�������ַ������������ޣ�
     * ����߳���û��sleep ��wait��Condition����ʱ����Ӧ��, interrupt()�������޷��жϵ�ǰ���̵߳ġ�
     * ���ԣ�ShutdownNow()���������̳߳ؾ�һ�����������˳��������ܱ���Ҫ�ȴ���������ִ�е�����ִ������˲����˳���
     */
    public void stopWorking() {
        try {
            threadPoolExecutor.shutdownNow();
            Thread.sleep(1000);
            threadPoolExecutor = initThreadPoolExecutor();
            LOGGER.info("stop working succeed ");
        } catch (Throwable t) {
            LOGGER.error("stop working failed ", t);
        }
    }

    /**
     * ������������ִ�е�����
     */
    public class RunningJobManager {

        private final Set<String> JOB_SET = new ConcurrentHashSet<String>();

        public void in(String jobId) {
            JOB_SET.add(jobId);
        }

        public void out(String jobId) {
            JOB_SET.remove(jobId);
        }

        public boolean running(String jobId) {
            return JOB_SET.contains(jobId);
        }

        /**
         * ���ظ���list�в����ڵ�jobId
         */
        public List<String> getNotExists(List<String> jobIds) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Running jobs ��" + JOB_SET);
                LOGGER.debug("Ask jobs:" + jobIds);
            }
            List<String> notExistList = new ArrayList<String>();
            for (String jobId : jobIds) {
                if (!running(jobId)) {
                    notExistList.add(jobId);
                }
            }
            return notExistList;
        }
    }

    public RunningJobManager getRunningJobManager() {
        return runningJobManager;
    }
}
