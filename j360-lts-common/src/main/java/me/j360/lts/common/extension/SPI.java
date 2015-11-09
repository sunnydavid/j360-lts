package me.j360.lts.common.extension;

import java.lang.annotation.*;

/**
 * Dubbo����΢�ں�+�����ϵ��ʹ��������ţ���չ��ǿ������ν��΢�ں�+�����ϵ�����ʵ�ֵ��أ�����Ƿ���Ϥspi(service providerinterface)���ƣ�
 * �����Ƕ����˷���ӿڱ�׼���ó���ȥʵ�֣�������˽�spi����ȸ�ٶ��£�, jdkͨ��ServiceLoader��ʵ��spi���Ƶķ�����ҹ��ܡ�
 * */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    /**
     * ȱʡ��չ������
     */
    String value() default "";

}