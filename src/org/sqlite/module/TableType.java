package org.sqlite.module;

import android.content.ContentValues;

/**
 * 存储主键与结果集的对象
 * 
 * @author talkliu
 * 
 */
public class TableType {
	/**
	 * ' 主键名
	 */
	public String tableKey;
	/**
	 * 进行操作的结果集
	 */
	public ContentValues values;
}
