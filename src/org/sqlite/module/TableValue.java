package org.sqlite.module;

import java.io.Serializable;

import org.sqlite.annotation.DataType;

/**
 * 私有类，创建类的属性值
 * 
 * @author talkliu
 * 
 */
public class TableValue implements Serializable {
	public String name;
	public DataType type;
	public Integer length;
	public boolean isPlus;
	public boolean notNull;
}