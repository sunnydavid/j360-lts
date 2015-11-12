package me.j360.lts.common.support;

import me.j360.lts.common.constant.EcTopic;
import me.j360.lts.common.extension.ExtensionLoader;
import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.utils.CollectionUtils;
import me.j360.lts.common.utils.GenericsUtils;
import me.j360.lts.common.utils.JSONUtils;
import me.j360.lts.ec.EventInfo;
import me.j360.lts.ec.EventSubscriber;
import me.j360.lts.ec.Observer;
import me.j360.lts.failstore.AbstractFailStore;
import me.j360.lts.failstore.FailStore;
import me.j360.lts.failstore.FailStoreException;
import me.j360.lts.failstore.FailStoreFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 *         ���Զ�ʱ�� (�������� �� �ͻ��˵ķ�����Ϣ��)
 */
public abstract class RetryScheduler<T> {

    public static final Logger LOGGER = LoggerFactory.getLogger(RetryScheduler.class);

    private Class<?> type = GenericsUtils.getSuperClassGenericType(this.getClass());

    // ��ʱ����Ƿ��� ʦ��ķ���������Ϣ(���ͻ��˵�)
    private ScheduledExecutorService RETRY_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService MASTER_RETRY_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> masterScheduledFuture;
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean selfCheckStart = new AtomicBoolean(false);
    private AtomicBoolean masterCheckStart = new AtomicBoolean(false);
    private FailStore failStore;
    // ������Ҫ��������¼��־
    private String name;

    // �������͵���Ϣ��
    private int batchSize = 5;

    private ReentrantLock lock = new ReentrantLock();

    public RetryScheduler(Application application) {
        this(application, application.getConfig().getFailStorePath());
    }

    public RetryScheduler(Application application, String storePath) {
        FailStoreFactory failStoreFactory = ExtensionLoader.getExtensionLoader(FailStoreFactory.class).getAdaptiveExtension();
        failStore = failStoreFactory.getFailStore(application.getConfig(), storePath);
        try {
            failStore.open();
        } catch (FailStoreException e) {
            throw new RuntimeException(e);
        }
        EventSubscriber subscriber = new EventSubscriber(RetryScheduler.class.getSimpleName()
                .concat(application.getConfig().getIdentity()),
                new Observer() {
                    @Override
                    public void onObserved(EventInfo eventInfo) {
                        Boolean isMaster = (Boolean) eventInfo.getParam("isMaster");
                        if (isMaster != null && isMaster) {
                            startMasterCheck();
                        } else {
                            stopMasterCheck();
                        }
                    }
                });
        /*application.getEventCenter().subscribe(subscriber, EcTopic.MASTER_CHANGED);

        if (application.getMasterElector().isCurrentMaster()) {
            startMasterCheck();
        }*/
    }

    public RetryScheduler(Application application, String storePath, int batchSize) {
        this(application, storePath);
        this.batchSize = batchSize;
    }

    protected RetryScheduler(Application application, int batchSize) {
        this(application);
        this.batchSize = batchSize;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void start() {
        try {
            if (selfCheckStart.compareAndSet(false, true)) {
                // ���ʱ�������ȥ�Ż�
                scheduledFuture = RETRY_EXECUTOR_SERVICE.scheduleWithFixedDelay
                        (new CheckSelfRunner(), 10, 30, TimeUnit.SECONDS);
                LOGGER.info("Start {} RetryScheduler success", name);
            }
        } catch (Throwable t) {
            LOGGER.error("Start {} RetryScheduler failed", name, t);
        }
    }

    private void startMasterCheck() {
        try {
            if (masterCheckStart.compareAndSet(false, true)) {
                // ���ʱ�������ȥ�Ż�
                masterScheduledFuture = MASTER_RETRY_EXECUTOR_SERVICE.
                        scheduleWithFixedDelay(new CheckDeadFailStoreRunner(), 30, 60, TimeUnit.SECONDS);
                LOGGER.info("Start {} master RetryScheduler success", name);
            }
        } catch (Throwable t) {
            LOGGER.error("Start {} master RetryScheduler failed.", name, t);
        }
    }

    private void stopMasterCheck() {
        try {
            if (masterCheckStart.compareAndSet(true, false)) {
                masterScheduledFuture.cancel(true);
                MASTER_RETRY_EXECUTOR_SERVICE.shutdown();
                LOGGER.info("Stop {} master RetryScheduler success", name);
            }
        } catch (Throwable t) {
            LOGGER.error("Stop {} master RetryScheduler failed", name, t);
        }
    }

    public void stop() {
        try {
            if (selfCheckStart.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                failStore.close();
                RETRY_EXECUTOR_SERVICE.shutdown();
                LOGGER.info("Stop {} RetryScheduler success", name);
            }
        } catch (Throwable t) {
            LOGGER.error("Stop {} RetryScheduler failed", name, t);
        }
    }

    public void destroy() {
        try {
            stop();
            failStore.destroy();
        } catch (FailStoreException e) {
            LOGGER.error("destroy {} RetryScheduler failed", name, e);
        }
    }

    /**
     * ��ʱ��� �ύʧ�������Runnable
     */
    private class CheckSelfRunner implements Runnable {

        @Override
        public void run() {
            try {
                // 1. ��� Զ������ �Ƿ����
                if (!isRemotingEnable()) {
                    return;
                }

                List<KVPair<String, T>> kvPairs = null;
                do {
                    try {
                        lock.tryLock(1000, TimeUnit.MILLISECONDS);
                        kvPairs = failStore.fetchTop(batchSize, type);

                        if (CollectionUtils.isEmpty(kvPairs)) {
                            break;
                        }

                        List<T> values = new ArrayList<T>(kvPairs.size());
                        List<String> keys = new ArrayList<String>(kvPairs.size());
                        for (KVPair<String, T> kvPair : kvPairs) {
                            keys.add(kvPair.getKey());
                            values.add(kvPair.getValue());
                        }
                        if (retry(values)) {
                            LOGGER.info("{} RetryScheduler, local files send success, size: {}, {}", name, values.size(), JSONUtils.toJSONString(values));
                            failStore.delete(keys);
                        } else {
                            break;
                        }
                    }finally {
                        if(lock.isHeldByCurrentThread()){
                            lock.unlock();
                        }
                    }
                } while (CollectionUtils.isNotEmpty(kvPairs));

            } catch (Throwable e) {
                LOGGER.error("Run {} RetryScheduler error ", name, e);
            }
        }
    }

    /**
     * ��ʱ��� �Ѿ�down���Ļ�����FailStoreĿ¼
     */
    private class CheckDeadFailStoreRunner implements Runnable {

        @Override
        public void run() {
            try {
                // 1. ��� Զ������ �Ƿ����
                if (!isRemotingEnable()) {
                    return;
                }
                List<FailStore> failStores = null;
                if (failStore instanceof AbstractFailStore) {
                    failStores = ((AbstractFailStore) failStore).getDeadFailStores();
                }
                if (CollectionUtils.isNotEmpty(failStores)) {
                    for (FailStore store : failStores) {
                        store.open();

                        while (true) {
                            List<KVPair<String, T>> kvPairs = store.fetchTop(batchSize, type);
                            if (CollectionUtils.isEmpty(kvPairs)) {
                                store.destroy();
                                LOGGER.info("{} RetryScheduler, delete store dir[{}] success.", name, store.getPath());
                                break;
                            }
                            List<T> values = new ArrayList<T>(kvPairs.size());
                            List<String> keys = new ArrayList<String>(kvPairs.size());
                            for (KVPair<String, T> kvPair : kvPairs) {
                                keys.add(kvPair.getKey());
                                values.add(kvPair.getValue());
                            }
                            if (retry(values)) {
                                LOGGER.info("{} RetryScheduler, dead local files send success, size: {}, {}", name, values.size(), JSONUtils.toJSONString(values));
                                store.delete(keys);
                            } else {
                                store.close();
                                break;
                            }
                            try {
                                Thread.sleep(500);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("Run {} master RetryScheduler error.", name, e);
            }
        }
    }

    public void inSchedule(String key, T value) {
        try {
            lock.tryLock();
            failStore.put(key, value);
            LOGGER.info("{} RetryScheduler, local files save success, {}", name, JSONUtils.toJSONString(value));
        } catch (FailStoreException e) {
            LOGGER.error("{} RetryScheduler in schedule error. ", name, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    /**
     * Զ�������Ƿ����
     *
     * @return
     */
    protected abstract boolean isRemotingEnable();

    /**
     * ����
     *
     * @param list
     * @return
     */
    protected abstract boolean retry(List<T> list);

}
