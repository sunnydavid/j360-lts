package me.j360.lts.remote.annotation;

import java.lang.annotation.*;


/**
 * ��ʶ�ֶο��Էǿ�
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface Nullable {
}
