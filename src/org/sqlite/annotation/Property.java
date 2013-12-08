package org.sqlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 属性注解类，将属性注解为表字段，创建数据库时可以由类创建表
 * 
 * @author talkliu
 * 
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

	/**
	 * 字段名 默认为属性名特指需指明
	 * 
	 * @return
	 */
	String name() default "";

	/**
	 * 字段属性 默认为属性类型（简单类型）
	 * 
	 * @return
	 */
	String type() default "";

	/**
	 * 字段长度
	 * 
	 * @return
	 */
	int length();

	/**
	 * 是否自增长 默认为false
	 * 
	 * @return
	 */
	boolean isPlus() default false;

	/**
	 * 是否为空 默认为false
	 * 
	 * @return
	 */
	boolean notNull() default false;

	/**
	 * 默认值
	 * 
	 * @return
	 */
	String defaultString() default "";
}
