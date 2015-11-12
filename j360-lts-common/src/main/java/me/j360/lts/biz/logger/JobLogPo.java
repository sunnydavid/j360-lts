package me.j360.lts.biz.logger;


import me.j360.lts.common.constant.Level;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/20/14.
 *         ����ִ�� ��־
 */
public class JobLogPo {

    // ��־��¼ʱ��
    private Long logTime;
    // ��־��¼ʱ��
    private Long gmtCreated;
    // ��־����
    private LogType logType;
    private boolean success;
    private String msg;
    private String taskTrackerIdentity;

    // ��־��¼����
    private Level level;

    protected String jobId;
    protected String taskId;
    /**
     * ���ȼ� (��ֵԽ�� ���ȼ�Խ��)
     */
    protected Integer priority = 100;
    // �ύ�Ľڵ�
    protected String submitNodeGroup;
    // ִ�еĽڵ�
    protected String taskTrackerNodeGroup;

    protected Map<String, String> extParams;
    // �Ƿ�Ҫ�������ͻ���
    protected boolean needFeedback = true;
    /**
     * ִ�б��ʽ �� quartz ��һ��
     * ������Ϊ�գ���ʾ����ִ�е�
     */
    private String cronExpression;
    /**
     * ������������ʱ��
     */
    private Long triggerTime;

    private Integer retryTimes = 0;

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTaskTrackerIdentity() {
        return taskTrackerIdentity;
    }

    public void setTaskTrackerIdentity(String taskTrackerIdentity) {
        this.taskTrackerIdentity = taskTrackerIdentity;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getSubmitNodeGroup() {
        return submitNodeGroup;
    }

    public void setSubmitNodeGroup(String submitNodeGroup) {
        this.submitNodeGroup = submitNodeGroup;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public Map<String, String> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, String> extParams) {
        this.extParams = extParams;
    }

    public boolean isNeedFeedback() {
        return needFeedback;
    }

    public void setNeedFeedback(boolean needFeedback) {
        this.needFeedback = needFeedback;
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

    public Long getLogTime() {
        return logTime;
    }

    public void setLogTime(Long logTime) {
        this.logTime = logTime;
    }
}
