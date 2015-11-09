package me.j360.lts.common.extension;

import java.lang.annotation.*;

/**
 * ��{@link ExtensionLoader}����Extension��Adaptive Instanceʱ��Ϊ{@link ExtensionLoader}�ṩ��Ϣ��
 * @export
 *
 * @see ExtensionLoader
 * @see Config
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {

    /**
     * ��{@link Config}��Key������Ӧ��Value��ΪҪAdapt�ɵ�Extension����
     * <p>
     * ���{@link Config}��ЩKey��û��Value��ʹ�� �� ȱʡ����չ���ڽӿڵ�{@link SPI}���趨��ֵ����<br>
     * ���磬<code>String[] {"key1", "key2"}</code>����ʾ
     * <ol>
     * <li>����URL����key1��Value��ΪҪAdapt�ɵ�Extension����
     * <li>key1û��Value����ʹ��key2��Value��ΪҪAdapt�ɵ�Extension����
     * <li>key2û��Value��ʹ��ȱʡ����չ��
     * <li>���û���趨ȱʡ��չ���򷽷����û��׳�{@link IllegalStateException}��
     * </ol>
     * <p>
     * �����������ȱʡʹ��Extension�ӿ������ĵ�ָ�Сд�ִ���<br>
     * ������Extension�ӿ�{@code com.lts.core.XxxYyyService}��ȱʡֵΪ<code>String[] {"xxx.yyy.service"}</code>
     *
     * @see SPI#value()
     */
    String[] value() default {};

}