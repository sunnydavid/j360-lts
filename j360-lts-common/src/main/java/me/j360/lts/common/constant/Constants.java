package me.j360.lts.common.constant;


import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         һЩ���ó���
 */
public interface Constants {

    // ���õĴ���������
    int AVAILABLE_PROCESSOR = Runtime.getRuntime().availableProcessors();

    String USER_HOME = System.getProperty("user.home");

    int JOB_TRACKER_DEFAULT_LISTEN_PORT = 35001;

    // Ĭ�ϼ�Ⱥ����
    String DEFAULT_CLUSTER_NAME = "defaultCluster";

    String CHARSET = "UTF-8";

    int DEFAULT_TIMEOUT = 1000;

    String TIMEOUT_KEY = "timeout";

    String SESSION_TIMEOUT_KEY = "session";

    int DEFAULT_SESSION_TIMEOUT = 60 * 1000;

    String REGISTER = "register";

    String UNREGISTER = "unregister";

    String SUBSCRIBE = "subscribe";

    String UNSUBSCRIBE = "unsubscribe";
    /**
     * ע������ʧ���¼������¼�
     */
    String REGISTRY_RETRY_PERIOD_KEY = "retry.period";

    /**
     * ��������
     */
    int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;

    Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    /**
     * ע�������Զ�����ʱ��
     */
    String REGISTRY_RECONNECT_PERIOD_KEY = "reconnect.period";

    int DEFAULT_REGISTRY_RECONNECT_PERIOD = 3 * 1000;

    String ZK_CLIENT_KEY = "zk.client";

    String JOB_LOGGER_KEY = "job.logger";

    String JOB_QUEUE_KEY = "job.queue";

    // �ͻ����ύ��������size
    String JOB_SUBMIT_CONCURRENCY_SIZE = "job.submit.concurrency.size";
    int DEFAULT_JOB_SUBMIT_CONCURRENCY_SIZE = 100;

    String PROCESSOR_THREAD = "job.processor.thread";
    int DEFAULT_PROCESSOR_THREAD = 32 + AVAILABLE_PROCESSOR * 5;

    int LATCH_TIMEOUT_MILLIS = 10 * 60 * 1000;      // 10����

    // ����������Դ���
    String JOB_MAX_RETRY_TIMES = "job.max.retry.times";
    int DEFAULT_JOB_MAX_RETRY_TIMES = 10;

    Charset UTF_8 = Charset.forName("UTF-8");

    String MONITOR_DATA_ADD_URL = "/api/monitor/monitor-data-add.do";

    String MONITOR_JVM_INFO_DATA_ADD_URL = "/api/monitor/jvm-info-data-add.do";

    String MONITOR_COMMAND_INFO_ADD_URL = "/api/monitor/command-info-add.do";

    String JOB_PULL_FREQUENCY = "job.pull.frequency";
    int DEFAULT_JOB_PULL_FREQUENCY = 1;

    // TaskTracker ����(�������)ʱ�� 2 ���ӣ����������ӣ��Զ�ֹͣ��ǰִ������
    long TASK_TRACKER_OFFLINE_LIMIT_MILLIS = 2 * 60 * 1000;
    // TaskTracker����һ��ʱ�����JobTracker���Զ�ֹͣ��ǰ����������
    String TASK_TRACKER_STOP_WORKING_SWITCH = "stop.working";

    String ADMIN_ID_PREFIX = "LTS_admin_";

    // �Ƿ��ӳ�����ˢ����־, ������ã����ö��еķ�ʽ��������־ˢ��(��Ӧ�ùرյ�ʱ�򣬿��ܻ������־��ʧ)
    String LAZY_JOB_LOGGER = "lazy.job.logger";
    // �ӳ�����ˢ����־ �ڴ��е������־����ֵ
    String LAZY_JOB_LOGGER_MEM_SIZE = "lazy.job.logger.mem.size";
    // �ӳ�����ˢ����־ ���Ƶ��
    String LAZY_JOB_LOGGER_CHECK_PERIOD = "lazy.job.logger.check.period";

}
