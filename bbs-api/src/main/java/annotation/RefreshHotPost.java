package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @param value 类型 view=浏览，reply=回复,delReply=删除回复，delPost=删除帖子(如果帖子被删除，则需要刷新)
 *             注解标明要计算浏览量的方法
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RefreshHotPost {
    String value() default "view";
}
