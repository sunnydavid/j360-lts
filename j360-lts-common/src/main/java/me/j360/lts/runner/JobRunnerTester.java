package me.j360.lts.runner;


import me.j360.lts.common.constant.Environment;
import me.j360.lts.common.constant.Level;
import me.j360.lts.common.support.Job;

/**
 * Ϊ�˷���JobRunner������Ƶ�
 *
 * @author Robert HG (254963746@qq.com) on 9/13/15.
 */
public abstract class JobRunnerTester {

    public Result run(Job job) throws Throwable {
        // 1. ����LTS����Ϊ UNIT_TEST
        //LTSConfig.setEnvironment(Environment.UNIT_TEST);
        // ���� BizLogger
        //LtsLoggerFactory.setLogger(BizLoggerFactory.getLogger(Level.INFO, null, null));
        // 2. load context (Spring Context ����������)
        initContext();
        // 3. new jobRunner
        JobRunner jobRunner = newJobRunner();
        // 4. run job
        return jobRunner.run(job);
    }

    /**
     * ��ʼ�������� (Spring Context��),׼�����л���
     */
    protected abstract void initContext();

    /**
     * ����JobRunner
     */
    protected abstract JobRunner newJobRunner();

}
