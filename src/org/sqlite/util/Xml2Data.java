package org.sqlite.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sqlite.annotation.Property;
import org.sqlite.annotation.Table;
import org.sqlite.annotation.DataType;
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
public class Xml2Data {
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
	protected Object[] getCreateSql() throws IOException,
			XmlPullParserException, ClassNotFoundException {
		//表更新时使用的hashmap
		HashMap<String, HashMap<String, TableValue>> tableMap=new HashMap<String, HashMap<String,TableValue>>();
		// 表的集合
		List<String> list = new ArrayList<String>();
		// 组建创建表的StringBuffer对象
		StringBuffer createSql = null;
		while (dataParser.getEventType() != XmlPullParser.END_DOCUMENT) {
			// 判断标签的起始位置
			if (dataParser.getEventType() == XmlPullParser.START_TAG) {
				// 判断如果标签为table的话
				if ("table".equals(dataParser.getName())) {
					createSql = new StringBuffer();
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
					//獲取的表屬性及其表對比值
					Object[] tableObjects=getTablePorperty(className);
					tableMap.put(tableName, (HashMap<String, TableValue>) tableObjects[1]);
					// 获取表内各个项
					List<TableValue> tableProerty = (List<TableValue>)tableObjects[0];
					// 获取主键
					String key = getTableKye(className);
					for (TableValue t : tableProerty) {
						createSql.append(t.name + " ");
						if (t.isPlus) {
							// 如果为自增长则固定为Integer类型
							createSql.append("integer ");
						} else {
							createSql.append(t.type + "(" + t.length + ") ");
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
					// 移除最后一个,
					createSql.replace(createSql.length() - 1,
							createSql.length(), "");
					// 一张表建立完成
					createSql.append(")");
					list.add(createSql.toString());
				}
			}
			dataParser.next();
		}
		// dataParser.close();
		return new Object[]{list,tableMap};
	}
	
	/**
	 * 更新数据库
	 * @param map
	 * @return
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 * @throws ClassNotFoundException 
	 */
	protected List<String> getUpdateSql(HashMap<String, HashMap<String, TableValue>> map) throws ClassNotFoundException, XmlPullParserException, IOException{
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
					//判断是否有该表，若没有该表则直接添加新表
					if(map.get(tableName)==null){
					createSql = new StringBuffer();
					// 获得Class
					Class className = Class.forName(dataParser
							.getAttributeValue(null, "ref"));
					// 将表名与类名放入map
					// ClassForTabelName.put(className.getName(), tableName);
					createSql.append("create table ");
					createSql.append(tableName + "(");
					//獲取的表屬性及其表對比值
					Object[] tableObjects=getTablePorperty(className);
					// 获取表内各个项
					List<TableValue> tableProerty = (List<TableValue>)tableObjects[0];
					// 获取主键
					String key = getTableKye(className);
					for (TableValue t : tableProerty) {
						createSql.append(t.name + " ");
						if (t.isPlus) {
							// 如果为自增长则固定为Integer类型
							createSql.append("integer ");
						} else {
							createSql.append(t.type + "(" + t.length + ") ");
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
					// 移除最后一个,
					createSql.replace(createSql.length() - 1,
							createSql.length(), "");
					// 一张表建立完成
					createSql.append(")");
					list.add(createSql.toString());
				}else{
					HashMap<String, TableValue> property=map.get(tableName);
					//如果存在則判斷屬性值是否有修改
					// 获得Class
					Class className = Class.forName(dataParser
							.getAttributeValue(null, "ref"));
					//獲取的表屬性及其表對比值
					Object[] tableObjects=getTablePorperty(className);
					// 获取表内各个项
					List<TableValue> tableProerty = (List<TableValue>)tableObjects[0];
					// 获取主键
					String key = getTableKye(className);
					Iterator iter = property.entrySet().iterator(); 
					while (iter.hasNext()) { 
					    Map.Entry entry = (Map.Entry) iter.next(); 
					} 
					for (TableValue t : tableProerty) {
						createSql = new StringBuffer();
						createSql.append("ALTER TABLE ");
						createSql.append(tableName+" ");
						TableValue ot=null;
						if((ot = property.get(t.name))!=null){
							
						}else{
							createSql.append("ADD "+t.name+" "+t.type+"("+t.length+")");
							list.add(createSql.toString());
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
	 * 私有方法，通过注解类的class来获取创建表的各项元素
	 * 
	 * @param <T>
	 * 
	 * @param cla
	 * @return
	 */
	private <T> Object[] getTablePorperty(Class<T> cla) {
		//创建对比map
		HashMap<String, TableValue> map=new HashMap<String, TableValue>();
		Field[] allFields = cla.getDeclaredFields();
		List<TableValue> list = new ArrayList<TableValue>();
		for (Field field : allFields) {
			Property property = field.getAnnotation(Property.class);
			TableValue tableValue = new TableValue();
			tableValue.isPlus = property.isPlus();
			tableValue.length = property.length();
			tableValue.name = property.name().equals("") ? field.getName()
					: property.name();
			tableValue.notNull = property.notNull();
			if (property.type().equals("")) {
				if (field.getType().toString()
						.equals("class java.lang.Integer")) {
					tableValue.type = DataType.INT;
				} else if (field.getType().toString()
						.equals("class java.lang.Double")) {
					tableValue.type = DataType.DOUBLE;
				} else if (field.getType().toString()
						.equals("class java.lang.Long")) {
					tableValue.type = DataType.BIGINT;
				} else {
					//其他类型直接存储为二进制对象並且進行反序列化
					tableValue.type = DataType.BLOB;
				}
			} else {
				tableValue.type = property.type();
			}
			//加入对比map
			map.put(tableValue.name, tableValue);
			list.add(tableValue);
		}
		return new Object[]{list,map};
	}

	/**
	 * 私有方法 获取主键
	 * 
	 * @param <T>
	 * 
	 * @param cla
	 * @return
	 */
	private <T> String getTableKye(Class<T> cla) {
		Table table = (Table) cla
				.getAnnotation(org.sqlite.annotation.Table.class);
		if (table != null)
			return table.kyeName();
		else
			return "";

	}
}
