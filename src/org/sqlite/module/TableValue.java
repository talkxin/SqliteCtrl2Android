package org.sqlite.module;

import java.io.Serializable;

import org.sqlite.annotation.SQLType;

/**
 * 私有类，创建类的属性值
 * 
 * @author talkliu
 * 
 */
public class TableValue implements Serializable {
	/**
	 * 属性名
	 */
	public String name;
	/**
	 * 属性类型
	 */
	public SQLType type;
	/**
	 * 长度
	 */
	public Integer length;
	/**
	 *是否递增长
	 */
	public boolean isPlus;
	/**
	 * 是否允许为null
	 */
	public boolean notNull;
}