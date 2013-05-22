package com.sqliteCtrl.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 属性注解 注解该属性是否被映射进表
 * 
 * @author talkliu
 * 
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TableProperty {
	/**
	 * 是否会被映射入结果集
	 * 
	 * @return
	 */
	boolean toObject() default true;
}
