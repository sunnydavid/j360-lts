package me.j360.lts.common.extension;

import java.lang.annotation.*;

/**
 * Activate
 * <p />
 * ���ڿ��Ա�������Զ����������չ����Annotation����������չ���Զ��������������
 * ���磬������չ���ж��ʵ�֣�ʹ��Activate Annotation����չ���Ը����������Զ����ء�
 * <ol>
 * <li>{@link Activate#group()}��Ч��Group�����������ЩGroupֵ�ɿ��SPI������
 * <li>{@link Activate#value()}��{@link Config}��Key�������У�����Ч��
 * </ol>
 *
 * <p />
 * �ײ���SPI�ṩ��ͨ��{@link ExtensionLoader}��{@link ExtensionLoader#getAdaptiveExtension()}����
 * �����������չ��
 * @export
 * @see SPI
 * @see ExtensionLoader
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Activate {
    /**
     * Group����������
     * <br />
     * ����{@link ExtensionLoader#getAdaptiveExtension()}��group��������ֵ���򷵻���չ��
     * <br />
     * ��û��Group���ã��򲻹��ˡ�
     */
    String[] group() default {};

    /**
     * Key��������������{@link ExtensionLoader#getAdaptiveExtension()}��URL�Ĳ���Key���У��򷵻���չ��
     * <p />
     * ʾ����<br/>
     * ע���ֵ <code>@Activate("cache,validatioin")</code>��
     * ��{@link ExtensionLoader#getAdaptiveExtension()}��URL�Ĳ�����<code>cache</code>Key������<code>validatioin</code>�򷵻���չ��
     * <br/>
     * ��û�����ã��򲻹��ˡ�
     */
    String[] value() default {};

    /**
     * ������Ϣ�����Բ��ṩ��
     */
    String[] before() default {};

    /**
     * ������Ϣ�����Բ��ṩ��
     */
    String[] after() default {};

    /**
     * ������Ϣ�����Բ��ṩ��
     */
    int order() default 0;
}