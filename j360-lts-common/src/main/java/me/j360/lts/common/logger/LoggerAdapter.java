package me.j360.lts.common.logger;

import me.j360.lts.common.extension.SPI;

import java.io.File;

/**
 * ��־�����������
 */
@SPI
public interface LoggerAdapter {

	/**
	 * ��ȡ��־�����
	 *
	 * @param key �����
	 * @return ��־�����, ��������: ������null.
	 */
	Logger getLogger(Class<?> key);

	/**
	 * ��ȡ��־�����
	 *
	 * @param key �����
	 * @return ��־�����, ��������: ������null.
	 */
	Logger getLogger(String key);

	/**
	 * ��������ȼ�
	 *
	 * @param level ����ȼ�
	 */
	void setLevel(Level level);

	/**
	 * ��ȡ��ǰ��־�ȼ�
	 *
	 * @return ��ǰ��־�ȼ�
	 */
	Level getLevel();

	/**
	 * ��ȡ��ǰ��־�ļ�
	 *
	 * @return ��ǰ��־�ļ�
	 */
	File getFile();

	/**
	 * ���������־�ļ�
	 *
	 * @param file �����־�ļ�
	 */
	void setFile(File file);

}