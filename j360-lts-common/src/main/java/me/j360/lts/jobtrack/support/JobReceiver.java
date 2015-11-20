package me.j360.lts.jobtrack.support;

import com.lts.core.protocol.command.JobSubmitRequest;
import me.j360.lts.biz.logger.JobLogPo;
import me.j360.lts.biz.logger.LogType;
import me.j360.lts.common.constant.Level;
import me.j360.lts.common.exception.JobReceiveException;
import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.support.Job;
import me.j360.lts.common.support.SystemClock;
import me.j360.lts.common.utils.CollectionUtils;
import me.j360.lts.common.utils.StringUtils;
import me.j360.lts.jobtrack.JobTrackerApplication;
import me.j360.lts.jobtrack.id.IdGenerator;
import me.j360.lts.queue.domain.JobPo;
import me.j360.lts.queue.exception.DuplicateJobException;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14.
 *         ��������
 */
public class JobReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobReceiver.class);

    private JobTrackerApplication application;
    private IdGenerator idGenerator;
    //private JobTrackerMonitor monitor;

    public JobReceiver(JobTrackerApplication application) {
        this.application = application;
        //this.monitor = (JobTrackerMonitor) application.getMonitor();
        //this.idGenerator = ExtensionLoader.getExtensionLoader(IdGenerator.class).getAdaptiveExtension();
    }

    /**
     * jobTracker ��������
     */
    public void receive(JobSubmitRequest request) throws JobReceiveException {

        List<Job> jobs = request.getJobs();
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }
        JobReceiveException exception = null;
        for (Job job : jobs) {
            try {
                addToQueue(job, request);
            } catch (Exception t) {
                if (exception == null) {
                    exception = new JobReceiveException(t);
                }
                exception.addJob(job);
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

    private JobPo addToQueue(Job job, JobSubmitRequest request) {

        JobPo jobPo = null;
        boolean success = false;
        BizLogCode code = null;
        try {
            jobPo = JobDomainConverter.convert(job);
            if (jobPo == null) {
                LOGGER.warn("Job can not be null��{}", job);
                return null;
            }
            if (StringUtils.isEmpty(jobPo.getSubmitNodeGroup())) {
                jobPo.setSubmitNodeGroup(request.getNodeGroup());
            }
            // ���� jobId
            jobPo.setJobId(idGenerator.generate(application.getConfig(), jobPo));

            // �������
            addJob(job, jobPo);

            success = true;
            code = BizLogCode.SUCCESS;

        } catch (DuplicateJobException e) {
            // �Ѿ�����
            if (job.isReplaceOnExist()) {
                //Assert.notNull(jobPo);
                success = replaceOnExist(job, jobPo);
                code = success ? BizLogCode.DUP_REPLACE : BizLogCode.DUP_FAILED;
            } else {
                code = BizLogCode.DUP_IGNORE;
                LOGGER.info("Job already exist. nodeGroup={}, {}", request.getNodeGroup(), job);
            }
        } finally {
            if (success) {
                //monitor.incReceiveJobNum();
            }
        }

        // ��¼��־
        jobBizLog(jobPo, code);

        return jobPo;
    }

    /**
     * �������
     */
    private void addJob(Job job, JobPo jobPo) throws DuplicateJobException {
        if (job.isSchedule()) {
            //addCronJob(jobPo);
            LOGGER.info("Receive Cron Job success. {}", job);
        } else {
            //application.getExecutableJobQueue().add(jobPo);
            LOGGER.info("Receive Job success. {}", job);
        }
    }

    /**
     * ��������
     **/
    private boolean replaceOnExist(Job job, JobPo jobPo) {

        // �õ��ϵ�jobId
        JobPo oldJobPo = null;
        if (job.isSchedule()) {
            //oldJobPo = application.getCronJobQueue().getJob(job.getTaskTrackerNodeGroup(), job.getTaskId());
        } else {
            //oldJobPo = application.getExecutableJobQueue().getJob(job.getTaskTrackerNodeGroup(), job.getTaskId());
        }
        if (oldJobPo != null) {
            String jobId = oldJobPo.getJobId();
            // 1. ɾ������
            //application.getExecutableJobQueue().remove(job.getTaskTrackerNodeGroup(), jobId);
            if (job.isSchedule()) {
                //application.getCronJobQueue().remove(jobId);
            }
            jobPo.setJobId(jobId);
        }

        // 2. �����������
        try {
            addJob(job, jobPo);
        } catch (DuplicateJobException e) {
            // һ�㲻���ߵ�����
            LOGGER.error("Job already exist twice. {}", job);
            return false;
        }
        return true;
    }

    /**
     * ���Cron ����
     */
    /*private void addCronJob(JobPo jobPo) throws DuplicateJobException {
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
        if (nextTriggerTime != null) {
            // 1.add to cron job queue
            application.getCronJobQueue().add(jobPo);

            // 2. add to executable queue
            jobPo.setTriggerTime(nextTriggerTime.getTime());
            application.getExecutableJobQueue().add(jobPo);
        }
    }
*/
    /**
     * ��¼������־
     */
    private void jobBizLog(JobPo jobPo, BizLogCode code) {
        if (jobPo != null) {
            try {
                // ��¼��־
                JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
                jobLogPo.setSuccess(true);
                jobLogPo.setLogType(LogType.RECEIVE);
                jobLogPo.setLogTime(SystemClock.now());

                switch (code) {
                    case SUCCESS:
                        jobLogPo.setLevel(Level.INFO);
                        jobLogPo.setMsg("�������ɹ�.");
                        break;
                    case DUP_IGNORE:
                        jobLogPo.setLevel(Level.WARN);
                        jobLogPo.setMsg("������������Ѿ�����,���Ա����ύ.");
                        break;
                    case DUP_FAILED:
                        jobLogPo.setLevel(Level.ERROR);
                        jobLogPo.setMsg("������������Ѿ�����,����ʱʧ��.");
                        break;
                    case DUP_REPLACE:
                        jobLogPo.setLevel(Level.INFO);
                        jobLogPo.setMsg("������������Ѿ�����,���³ɹ�.");
                        break;
                }

                application.getJobLogger().log(jobLogPo);
            } catch (Throwable t) {     // ��־��¼ʧ�ܲ�Ӱ����������
                LOGGER.error("Receive Job Log error ", t);
            }
        }
    }

    private enum BizLogCode {
        DUP_IGNORE,     // ����ظ�������
        DUP_REPLACE,    // ���ʱ�ظ������Ǹ���
        DUP_FAILED,     // ���ʱ�ظ��ٴ����ʧ��
        SUCCESS,     // ��ӳɹ�
    }

}
