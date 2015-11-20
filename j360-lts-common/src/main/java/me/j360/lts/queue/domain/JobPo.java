package me.j360.lts.queue.domain;


import me.j360.lts.common.utils.JSONUtils;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 *         �洢��Jod����
 */
public class JobPo {

    /**
     * ��������ɵ�jobId
     */
    private String jobId;
    /**
     * ���ȼ� (��ֵԽ�� ���ȼ�Խ��)
     */
    private Integer priority;
    /**
     * �ͻ��˴�������ID
     */
    private String taskId;
    // ����ʱ��
    private Long gmtCreated;
    // �޸�ʱ��
    private Long gmtModified;
    /**
     * �ύ�ͻ��˵Ľڵ���
     */
    private String submitNodeGroup;
    /**
     * ִ��job ������ڵ�
     */
    private String taskTrackerNodeGroup;
    /**
     * ����Ĳ���, ��Ҫ����taskTracker��
     */
    private Map<String, String> extParams;
    /**
     * �Ƿ�����ִ��
     */
    private boolean isRunning = false;
    /**
     * ִ�е�taskTracker
     * identity
     */
    private String taskTrackerIdentity;

    // �Ƿ���Ҫ�������ͻ���
    private boolean needFeedback;

    /**
     * ִ��ʱ����ʽ (�� quartz ���ʽһ��)
     */
    private String cronExpression;
    /**
     * ��һ��ִ��ʱ��
     */
    private Long triggerTime;

    /**
     * ���Դ���
     */
    private Integer retryTimes = 0;

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Long getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Long triggerTime) {
        this.triggerTime = triggerTime;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public Long getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Long gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Map<String, String> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, String> extParams) {
        this.extParams = extParams;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean isNeedFeedback() {
        return needFeedback;
    }

    public void setNeedFeedback(boolean needFeedback) {
        this.needFeedback = needFeedback;
    }

    public String getSubmitNodeGroup() {
        return submitNodeGroup;
    }

    public void setSubmitNodeGroup(String submitNodeGroup) {
        this.submitNodeGroup = submitNodeGroup;
    }

    public String getTaskTrackerIdentity() {
        return taskTrackerIdentity;
    }

    public void setTaskTrackerIdentity(String taskTrackerIdentity) {
        this.taskTrackerIdentity = taskTrackerIdentity;
    }

    public boolean isSchedule() {
        return this.cronExpression != null && !"".equals(this.cronExpression.trim());
    }

    @Override
    public String toString() {
        return JSONUtils.toJSONString(this);
    }
}
