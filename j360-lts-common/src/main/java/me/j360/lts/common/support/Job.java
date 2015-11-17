package me.j360.lts.common.support;



import me.j360.lts.common.utils.JSONUtils;
import me.j360.lts.common.utils.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class Job implements Serializable{

    private String taskId;
    /**
     * ���ȼ� (��ֵԽ�� ���ȼ�Խ��)
     */
    private Integer priority = 100;
    // �ύ�Ľڵ� �������ֶ�ָ����
    private String submitNodeGroup;
    // ִ�еĽڵ�
    private String taskTrackerNodeGroup;

    private Map<String, String> extParams;
    // �Ƿ�Ҫ�������ͻ���
    private boolean needFeedback = false;
    // ���Դ���
    private int retryTimes = 0;
    /**
     * ִ�б��ʽ �� quartz ��һ��
     * ������Ϊ�գ���ʾ����ִ�е�
     */
    private String cronExpression;

    /**
     * ������������ʱ��
     * ��������� cronExpression�� ��ô����ֶ�û��
     */
    private Long triggerTime;
    /**
     * ����������д�����������ʱ���Ƿ��滻����
     */
    private boolean replaceOnExist = false;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public boolean isNeedFeedback() {
        return needFeedback;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public void setNeedFeedback(boolean needFeedback) {
        this.needFeedback = needFeedback;
    }

    public Map<String, String> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, String> extParams) {
        this.extParams = extParams;
    }

    public String getParam(String key) {
        if (extParams == null) {
            return null;
        }
        return extParams.get(key);
    }

    public void setParam(String key, String value) {
        if (extParams == null) {
            extParams = new HashMap<String, String>();
        }
        extParams.put(key, value);
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isSchedule() {
        return this.cronExpression != null && !"".equals(this.cronExpression.trim());
    }

    public void setTriggerTime(Date date) {
        if (date != null) {
            this.triggerTime = date.getTime();
        }
    }

    public Long getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Long triggerTime) {
        this.triggerTime = triggerTime;
    }

    public boolean isReplaceOnExist() {
        return replaceOnExist;
    }

    public void setReplaceOnExist(boolean replaceOnExist) {
        this.replaceOnExist = replaceOnExist;
    }

    @Override
    public String toString() {
        return JSONUtils.toJSONString(this);
    }

}
