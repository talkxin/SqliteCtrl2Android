package org.sqlite.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.sqlite.module.TableValue;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

/**
 * 用于将xml注解入的对象变为数据库表的工具类
 * 
 * @author talkliu
 * 
 */
public class Xml2Data extends DatabaseUtil{
	/**
	 * activity的Context
	 */
	private Context actContext;
	/**
	 * xml的字符串
	 */
	private String xmlStr;
	/**
	 * 配置文件中的xml对象
	 */
	// private Integer r2xml;
	/**
	 * xml解析对象
	 */
	private XmlPullParser dataParser;

	//
	// /**
	// * 类与表的对照map
	// */
	// public static HashMap<String, String> ClassForTabelName = new
	// HashMap<String, String>();

	/**
	 * 构造方法，取得context获取配置文件
	 * 
	 * @param context
	 * @param xml
	 *            R文件下配置文件的资源列表
	 */
	public Xml2Data(Context context, XmlPullParser dataParser) {
		this.actContext = context;
		this.dataParser = dataParser;
	}

	/**
	 * 获取数据库名
	 * 
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public String getDatabaseName() throws XmlPullParserException, IOException {
		String databaseName = "";
		while (dataParser.getEventType() != XmlPullParser.END_DOCUMENT) {
			if ("head".equals(dataParser.getName())) {
				databaseName = dataParser.getAttributeValue(null, "name");
				break;
			}
			dataParser.next();
		}
		// dataParser.close();
		return databaseName;
	}

	/**
	 * 获取创建数据库表的集合
	 * 
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws ClassNotFoundException
	 */
	protected List<String> getCreateSql() throws IOException,
			XmlPullParserException, ClassNotFoundException {
		// 表的集合
		List<String> list = new ArrayList<String>();
		// 组建创建表的StringBuffer对象
		while (dataParser.getEventType() != XmlPullParser.END_DOCUMENT) {
			// 判断标签的起始位置
			if (dataParser.getEventType() == XmlPullParser.START_TAG) {
				// 判断如果标签为table的话
				if ("table".equals(dataParser.getName())) {
					list.add(create_table(dataParser));
				}
			}
			dataParser.next();
		}
		// dataParser.close();
		return list;
	}

	/**
	 * 更新数据库
	 * 
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws ClassNotFoundException
	 */
	protected List<String> getUpdateSql(
			HashMap<String, HashMap<String, TableValue>> map) throws ClassNotFoundException,
			XmlPullParserException, IOException {
		// 表的集合
		List<String> list = new ArrayList<String>();
		// 组建创建表的StringBuffer对象
		StringBuffer createSql = null;
		while (dataParser.getEventType() != XmlPullParser.END_DOCUMENT) {
			// 判断标签的起始位置
			if (dataParser.getEventType() == XmlPullParser.START_TAG) {
				// 判断如果标签为table的话
				if ("table".equals(dataParser.getName())) {
					// 获得表名
					String tableName = dataParser.getAttributeValue(null,
							"name");
					//获取表的老字段值
					HashMap<String, TableValue> oldProperty;
					// 判断是否有该表，若没有该表则直接添加新表
					if ((oldProperty=map.get(tableName)) == null) {
						list.add(create_table(dataParser));
					} else {
						// 获得Class
						Class className = Class.forName(dataParser
								.getAttributeValue(null, "ref"));
						// 獲取的表屬性及其表對比值
						Object[] newTables = getTablePorperty(className);
						HashMap<String, TableValue> newProperty = (HashMap<String, TableValue>) newTables[1];
						Iterator ite=newProperty.entrySet().iterator();
						//检查修改与新增
						while(ite.hasNext()){
							Entry entry = (Entry) ite.next();
							StringBuffer alter;
							TableValue ol;
							String name=(String) entry.getKey();
							TableValue ne=(TableValue) entry.getValue();
							if((ol=oldProperty.get(name))==null){
								alter=new StringBuffer();
								String length="";
								if(ne.length!=0){
									length="(" + ne.length + ") ";
								}
								alter.append("ALTER TABLE "+tableName+" ADD ");
								alter.append(ne.type+length);
								//填入老字段中
								oldProperty.put(name, ne);
								list.add(alter.toString());
							}
						}
					}
				}
			}
			dataParser.next();
		}
		return null;
	}
	
	/**
	 * 返回生成表結構
	 * @return
	 * @throws ClassNotFoundException 
	 */
	private String create_table(XmlPullParser dataParser) throws ClassNotFoundException{
		StringBuffer createSql = new StringBuffer();
		// 获得表名
		String tableName = dataParser.getAttributeValue(null,
				"name");
		// 获得Class
		Class className = Class.forName(dataParser
				.getAttributeValue(null, "ref"));
		// 将表名与类名放入map
		// ClassForTabelName.put(className.getName(), tableName);
		createSql.append("create table ");
		createSql.append(tableName + "(");
		// 獲取的表屬性及其表對比值
		Object[] tableObjects = getTablePorperty(className);
		// 获取表内各个项
		List<TableValue> tableProerty = (List<TableValue>) tableObjects[0];
		// 获取主键
		String key = getTableKye(className);
		for (TableValue t : tableProerty) {
			createSql.append(t.name + " ");
			if (t.isPlus) {
				// 如果为自增长则固定为Integer类型
				createSql.append("integer ");
			} else {
				String length=" ";
				if(t.length!=0){
					length="(" + t.length + ") ";
				}
				createSql.append(t.type+length);
			}
			if (!key.equals("") && t.name.equals(key)) {
				// 如果为主键则加上主键约束
				createSql.append("primary key ");
			}
			if (t.notNull) {
				// 不为空约束
				createSql.append("not null ");
			}
			// 一个字段建立完成
			createSql.append(",");
		}
		// 移除最后一个逗号
		createSql.replace(createSql.length() - 1,
				createSql.length(), "");
		// 一张表建立完成
		createSql.append(")");
		return createSql.toString();
	}
}
