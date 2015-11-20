package me.j360.lts.common.support;

import me.j360.lts.common.utils.JSONUtils;
import me.j360.lts.runner.Action;

import java.io.Serializable;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 * TaskTracker ����ִ�н��
 */
public class TaskTrackerJobResult implements Serializable{

    private JobWrapper jobWrapper;

    private Action action;

    private String msg;
    // �������ʱ��
    private Long time;

    public JobWrapper getJobWrapper() {
        return jobWrapper;
    }

    public void setJobWrapper(JobWrapper jobWrapper) {
        this.jobWrapper = jobWrapper;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return JSONUtils.toJSONString(this);
    }
}
