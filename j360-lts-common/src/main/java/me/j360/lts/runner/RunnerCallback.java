package me.j360.lts.runner;


import me.j360.lts.common.support.JobWrapper;
import me.j360.lts.runner.domain.Response;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public interface RunnerCallback {

    /**
     * ִ�����, �����ǳɹ�, Ҳ������ʧ��
     * @param response
     * @return ������µ�����, ��ô�����µ��������
     */
    public JobWrapper runComplete(Response response);

}
