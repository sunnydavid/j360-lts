package me.j360.lts.biz.logger;


import me.j360.lts.common.constant.Constants;
import me.j360.lts.common.extension.ExtensionLoader;
import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.support.Config;
import me.j360.lts.common.utils.CollectionUtils;
import me.j360.lts.common.utils.NamedThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * �ڲ������û����������Ƿ�����ӳ�����ˢ�̵Ĳ���,�����������
 * ����ˢ�����������:
 * 1. �ڴ����־�����������õķ�ֵ
 * 2. ÿ3S���һ���ڴ����Ƿ�����־,����о���ôˢ��
 *
 * @author Robert HG (254963746@qq.com) on 10/2/15.
 */
public class JobLoggerDelegate implements JobLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobLoggerDelegate.class);

    // 3S �������һ����־
    private int flushPeriod;

    private JobLogger jobLogger;
    private boolean lazyLog = false;
    private ScheduledExecutorService executor;
    private ScheduledFuture scheduledFuture;
    private BlockingQueue<JobLogPo> memoryQueue;
    // ��־����ˢ������
    private int batchFlushSize = 100;
    private int overflowSize = 10000;
    // �ڴ���������־����ֵ
    private int maxMemoryLogSize;
    private AtomicBoolean flushing = new AtomicBoolean(false);

    public JobLoggerDelegate(Config config) {
        JobLoggerFactory jobLoggerFactory = ExtensionLoader
                .getExtensionLoader(JobLoggerFactory.class).getAdaptiveExtension();
        jobLogger = jobLoggerFactory.getJobLogger(config);
        lazyLog = config.getParameter(Constants.LAZY_JOB_LOGGER, false);
        if (lazyLog) {

            // �޽�Queue
            memoryQueue = new LinkedBlockingQueue<JobLogPo>();
            maxMemoryLogSize = config.getParameter(Constants.LAZY_JOB_LOGGER_MEM_SIZE, 1000);
            flushPeriod = config.getParameter(Constants.LAZY_JOB_LOGGER_CHECK_PERIOD, 3);

            executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("LazyJobLogger"));
            scheduledFuture = executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (flushing.compareAndSet(false, true)) {
                            checkAndFlush();
                        }
                    } catch (Throwable t) {
                        LOGGER.error("CheckAndFlush log error", t);
                    }
                }
            }, flushPeriod, flushPeriod, TimeUnit.SECONDS);

        }
    }

    /**
     * ����ڴ����Ƿ�����־,����о�����ˢ��
     */
    private void checkAndFlush() {
        try {
            int nowSize = memoryQueue.size();
            if (nowSize == 0) {
                return;
            }
            List<JobLogPo> batch = new ArrayList<JobLogPo>();
            for (int i = 0; i < nowSize; i++) {
                JobLogPo jobLogPo = memoryQueue.poll();
                batch.add(jobLogPo);

                if (batch.size() >= batchFlushSize) {
                    flush(batch);
                }
            }
            if (batch.size() > 0) {
                flush(batch);
            }

        } finally {
            flushing.compareAndSet(true, false);
        }
    }

    private void checkOverflowSize() {
        if (memoryQueue.size() > overflowSize) {
            throw new JobLogException("Memory Log size is " + memoryQueue.size() + " , please check the JobLogger is available");
        }
    }

    private void flush(List<JobLogPo> batch) {
        boolean flushSuccess = false;
        try {
            jobLogger.log(batch);
            flushSuccess = true;
        } finally {
            if (!flushSuccess) {
                memoryQueue.addAll(batch);
            }
            batch.clear();
        }
    }

    /**
     * ����ڴ��е���־���Ƿ񳬹���ֵ,���������Ҫ����ˢ����־
     */
    private void checkCapacity() {
        if (memoryQueue.size() > maxMemoryLogSize) {
            // ������ֵ,��Ҫ����ˢ��
            if (flushing.compareAndSet(false, true)) {
                // ������Բ���new Thread, ��Ϊ����ֻ��ͬʱnewһ��
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            checkAndFlush();
                        } catch (Throwable t) {
                            LOGGER.error("Capacity full flush error", t);
                        }
                    }
                }).start();
            }
        }
    }

    @Override
    public void log(JobLogPo jobLogPo) {
        if (jobLogPo == null) {
            return;
        }
        if (lazyLog) {
            checkOverflowSize();
            memoryQueue.offer(jobLogPo);
            checkCapacity();
        } else {
            jobLogger.log(jobLogPo);
        }
    }

    @Override
    public void log(List<JobLogPo> jobLogPos) {
        if (CollectionUtils.isEmpty(jobLogPos)) {
            return;
        }
        if (lazyLog) {
            checkOverflowSize();
            for (JobLogPo jobLogPo : jobLogPos) {
                memoryQueue.offer(jobLogPo);
            }
            // checkCapacity
            checkCapacity();
        } else {
            jobLogger.log(jobLogPos);
        }
    }


}
