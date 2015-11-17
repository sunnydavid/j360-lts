package me.j360.lts.common.test.runner;


import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.common.support.Job;
import me.j360.lts.runner.Action;
import me.j360.lts.runner.JobRunner;
import me.j360.lts.runner.Result;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class TestJobRunner implements JobRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestJobRunner.class);
    //private final static BizLogger bizLogger = LtsLoggerFactory.getBizLogger();
    //private final static BizLogger bizLogger = LtsLoggerFactory.getBizLogger();

    @Override
    public Result run(Job job) throws Throwable {
        try {
//            Thread.sleep(1000L);
//
//            if (job.getRetryTimes() > 5) {
//                return new Result(Action.EXECUTE_FAILED, "���Դ�������5���ˣ��Ź����!");
//            }
//
//            if (SystemClock.now() % 2 == 1) {
//                return new Result(Action.EXECUTE_LATER, "�Ժ�ִ��");
//            }

            // TODO ҵ���߼�
            LOGGER.info("��Ҫִ�У�" + job);
            // �ᷢ�͵� LTS (JobTracker��)
            //bizLogger.info("���ԣ�ҵ����־����������");

        } catch (Exception e) {
            LOGGER.info("Run job failed!", e);
            return new Result(Action.EXECUTE_FAILED, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "ִ�гɹ��ˣ�����");
    }
}
