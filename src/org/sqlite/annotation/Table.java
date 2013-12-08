package org.sqlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类注解，声明主键
 * 
 * @author talkliu
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	/**
	 * 主键名
	 * 
	 * @return
	 */
	String kyeName() default "";
}
