package me.j360.lts.common.test.runner;


import me.j360.lts.common.support.Job;
import me.j360.lts.common.utils.JSONUtils;
import me.j360.lts.runner.JobRunner;
import me.j360.lts.runner.JobRunnerTester;
import me.j360.lts.runner.Result;

/**
 * @author Robert HG (254963746@qq.com) on 9/13/15.
 */
public class TestJobRunnerTester extends JobRunnerTester {

    public static void main(String[] args) throws Throwable {
        //  Mock Job ����
        Job job = new Job();
        job.setTaskId("2313213");
        // ���в���
        TestJobRunnerTester tester = new TestJobRunnerTester();
        Result result = tester.run(job);
        System.out.println(JSONUtils.toJSONString(result));
    }

    @Override
    protected void initContext() {
        // TODO ��ʼ��Spring����
    }

    @Override
    protected JobRunner newJobRunner() {
        return new TestJobRunner();
    }
}
