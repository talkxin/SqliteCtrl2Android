package com.sqliteCtrl.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 类注解
 * 注解该类所对应的表的表名，主键，是否低增长
 * @author talkliu
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {
	/**
	 * 表名
	 * 
	 * @return
	 */
	String name();

	/**
	 * 主键
	 * 
	 * @return
	 */
	String tableKey();

	/**
	 * 是否递增
	 * 
	 * @return
	 */
	boolean nullable();
}
