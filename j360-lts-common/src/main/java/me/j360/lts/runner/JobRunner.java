package me.j360.lts.runner;


import me.j360.lts.common.support.Job;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         ����ִ����Ҫʵ�ֵĽӿ�
 */
public interface JobRunner {

    /**
     * ִ������
     * �׳��쳣������ʧ��, ����null����Ϊ�����ѳɹ�
     */
    public Result run(Job job) throws Throwable;

}
