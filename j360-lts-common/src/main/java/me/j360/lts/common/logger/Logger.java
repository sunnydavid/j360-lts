package me.j360.lts.common.logger;

/**
 * ��־�ӿ� <p/> ������������commons-logging
 */
public interface Logger {

    /**
     * ���������Ϣ
     *
     * @param msg ��Ϣ����
     */
    public void trace(String msg);

    /**
     * ���������Ϣ
     *
     * @param e �쳣��Ϣ
     */
    public void trace(Throwable e);

    /**
     * ���������Ϣ
     *
     * @param msg ��Ϣ����
     * @param e   �쳣��Ϣ
     */
    public void trace(String msg, Throwable e);

    /**
     * ���������Ϣ
     *
     * @param format    ��Ϣ����
     * @param arguments �����б�
     */
    public void trace(String format, Object... arguments);

    /**
     * ���������Ϣ
     *
     * @param msg ��Ϣ����
     */
    public void debug(String msg);

    /**
     * ���������Ϣ
     *
     * @param e �쳣��Ϣ
     */
    public void debug(Throwable e);

    /**
     * ���������Ϣ
     *
     * @param msg ��Ϣ����
     * @param e   �쳣��Ϣ
     */
    public void debug(String msg, Throwable e);

    /**
     * ���������Ϣ
     *
     * @param format    ��Ϣ����
     * @param arguments �����б�
     */
    public void debug(String format, Object... arguments);

    /**
     * �����ͨ��Ϣ
     *
     * @param msg ��Ϣ����
     */
    public void info(String msg);

    /**
     * �����ͨ��Ϣ
     *
     * @param e �쳣��Ϣ
     */
    public void info(Throwable e);

    /**
     * �����ͨ��Ϣ
     *
     * @param msg ��Ϣ����
     * @param e   �쳣��Ϣ
     */
    public void info(String msg, Throwable e);

    /**
     * �����ͨ��Ϣ
     *
     * @param format    ��Ϣ����
     * @param arguments �����б�
     */
    public void info(String format, Object... arguments);

    /**
     * ���������Ϣ
     *
     * @param msg ��Ϣ����
     */
    public void warn(String msg);

    /**
     * ���������Ϣ
     *
     * @param e �쳣��Ϣ
     */
    public void warn(Throwable e);

    /**
     * ���������Ϣ
     *
     * @param msg ��Ϣ����
     * @param e   �쳣��Ϣ
     */
    public void warn(String msg, Throwable e);

    /**
     * ���������Ϣ
     *
     * @param format    ��Ϣ����
     * @param arguments �����б�
     */
    public void warn(String format, Object... arguments);

    /**
     * ���������Ϣ
     *
     * @param msg ��Ϣ����
     */
    public void error(String msg);

    /**
     * ���������Ϣ
     *
     * @param e �쳣��Ϣ
     */
    public void error(Throwable e);

    /**
     * ���������Ϣ
     *
     * @param msg ��Ϣ����
     * @param e   �쳣��Ϣ
     */
    public void error(String msg, Throwable e);

    /**
     * ���������Ϣ
     *
     * @param format    ��Ϣ����
     * @param arguments �����б�
     */
    public void error(String format, Object... arguments);

    /**
     * ������Ϣ�Ƿ���
     *
     * @return �Ƿ���
     */
    public boolean isTraceEnabled();

    /**
     * ������Ϣ�Ƿ���
     *
     * @return �Ƿ���
     */
    public boolean isDebugEnabled();

    /**
     * ��ͨ��Ϣ�Ƿ���
     *
     * @return �Ƿ���
     */
    public boolean isInfoEnabled();

    /**
     * ������Ϣ�Ƿ���
     *
     * @return �Ƿ���
     */
    public boolean isWarnEnabled();

    /**
     * ������Ϣ�Ƿ���
     *
     * @return �Ƿ���
     */
    public boolean isErrorEnabled();

}