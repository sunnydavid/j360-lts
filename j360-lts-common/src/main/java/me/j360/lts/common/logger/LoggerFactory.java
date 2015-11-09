package me.j360.lts.common.logger;



import me.j360.lts.common.extension.ExtensionLoader;
import me.j360.lts.common.logger.jcl.JclLoggerAdapter;
import me.j360.lts.common.logger.jdk.JdkLoggerAdapter;
import me.j360.lts.common.logger.log4j.Log4jLoggerAdapter;
import me.j360.lts.common.logger.slf4j.Slf4jLoggerAdapter;
import me.j360.lts.common.logger.support.FailsafeLogger;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ��־���������
 */
public class LoggerFactory {

	private LoggerFactory() {
	}

	private static volatile LoggerAdapter LOGGER_ADAPTER;

	private static final ConcurrentMap<String, FailsafeLogger> LOGGERS = new ConcurrentHashMap<String, FailsafeLogger>();

	// ���ҳ��õ���־���
	static {
	    String logger = System.getProperty("lts.logger");
	    if ("slf4j".equals(logger)) {
    		setLoggerAdapter(new Slf4jLoggerAdapter());
    	} else if ("jcl".equals(logger)) {
    		setLoggerAdapter(new JclLoggerAdapter());
    	} else if ("log4j".equals(logger)) {
    		setLoggerAdapter(new Log4jLoggerAdapter());
    	} else if ("jdk".equals(logger)) {
    		setLoggerAdapter(new JdkLoggerAdapter());
    	} else {
    		try {
				setLoggerAdapter(new Slf4jLoggerAdapter());
            } catch (Throwable e1) {
                try {
					setLoggerAdapter(new Log4jLoggerAdapter());
				} catch (Throwable e2) {
                    try {
                    	setLoggerAdapter(new JclLoggerAdapter());
                    } catch (Throwable e3) {
                        setLoggerAdapter(new JdkLoggerAdapter());
                    }
                }
            }
    	}
	}

	public static void setLoggerAdapter(String loggerAdapter) {
	    if (loggerAdapter != null && loggerAdapter.length() > 0) {
	        setLoggerAdapter(ExtensionLoader.getExtensionLoader(LoggerAdapter.class).getExtension(loggerAdapter));
	    }
	}

	/**
	 * ������־�����������
	 *
	 * @param loggerAdapter
	 *            ��־�����������
	 */
	public static void setLoggerAdapter(LoggerAdapter loggerAdapter) {
		if (loggerAdapter != null) {
			Logger logger = loggerAdapter.getLogger(LoggerFactory.class.getName());
			logger.info("using logger: " + loggerAdapter.getClass().getName());
			LoggerFactory.LOGGER_ADAPTER = loggerAdapter;
			for (Map.Entry<String, FailsafeLogger> entry : LOGGERS.entrySet()) {
				entry.getValue().setLogger(LOGGER_ADAPTER.getLogger(entry.getKey()));
			}
		}
	}

	/**
	 * ��ȡ��־�����
	 *
	 * @param key
	 *            �����
	 * @return ��־�����, ��������: ������null.
	 */
	public static Logger getLogger(Class<?> key) {
		FailsafeLogger logger = LOGGERS.get(key.getName());
		if (logger == null) {
			LOGGERS.putIfAbsent(key.getName(), new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
			logger = LOGGERS.get(key.getName());
		}
		return logger;
	}

	/**
	 * ��ȡ��־�����
	 *
	 * @param key
	 *            �����
	 * @return ��־�����, ��������: ������null.
	 */
	public static Logger getLogger(String key) {
		FailsafeLogger logger = LOGGERS.get(key);
		if (logger == null) {
			LOGGERS.putIfAbsent(key, new FailsafeLogger(LOGGER_ADAPTER.getLogger(key)));
			logger = LOGGERS.get(key);
		}
		return logger;
	}

	/**
	 * ��̬���������־����
	 *
	 * @param level ��־����
	 */
	public static void setLevel(Level level) {
		LOGGER_ADAPTER.setLevel(level);
	}

	/**
	 * ��ȡ��־����
	 *
	 * @return ��־����
	 */
	public static Level getLevel() {
		return LOGGER_ADAPTER.getLevel();
	}

	/**
	 * ��ȡ��־�ļ�
	 *
	 * @return ��־�ļ�
	 */
	public static File getFile() {
		return LOGGER_ADAPTER.getFile();
	}

}