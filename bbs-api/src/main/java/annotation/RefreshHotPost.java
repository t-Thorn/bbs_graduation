package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @param type 类型 0=浏览，1=回复,2=刷新(如果帖子被删除，则需要刷新)
 *             注解标明要计算浏览量的方法
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RefreshHotPost {
    int value();
}
