package org.sqlite.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sqlite.annotation.DataType;
import org.sqlite.annotation.Property;
import org.sqlite.annotation.Table;
import org.sqlite.module.TableType;
import org.sqlite.module.TableValue;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;

public class DatabaseUtil {
	/**
	 * 返回结果集
	 * 
	 * @param <T>
	 * 
	 * @param i
	 *            判断是否使用默认值，除在insert时使用其他操作不使用结果集
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	protected <T> TableType getObjectContentValues(boolean i, T object)
			throws IllegalArgumentException, IllegalAccessException,
			IOException {
		// 创建class
		Class cla = object.getClass();
		// 创建一个结果集对象
		TableType tableType = new TableType();
		// 返回类注解
		Table table = (Table) cla
				.getAnnotation(org.sqlite.annotation.Table.class);
		if (table != null && table.kyeName() != null)
			tableType.tableKey = table.kyeName();
		else
			tableType.tableKey = "";
		// 返回cla的所有内容
		Field[] allFields = cla.getDeclaredFields();
		// 创建一个结果集
		ContentValues values = new ContentValues();
		for (Field field : allFields) {
			// 参数值为true，禁用访问控制检查
			field.setAccessible(true);
			// 获取私有属性值
			Object value = field.get(object);
			// 返回属性注解
			Property property = field.getAnnotation(Property.class);
			if (!property.isPlus()) {
				// 判断类型
				switch (property.type()) {
				case BIGINT:
				case INT:
				case TINYINT:
				case SMALLINT:
				case MEDIUMINT:
				case UNSIGNED_BIG_INT:
				case INT2:
				case INT8:
				case CHARACTER:
				case VARCHAR:
				case VARYING_CHARACTER:
				case NCHAR:
				case NATIVE_CHARACTER:
				case NVARCHAR:
				case TEXT:
				case DOUBLE:
				case DOUBLE_PRECISION:
				case FLOAT:
				case NUMERIC:
				case DECIMAL:
				case BOOLEAN:
					String vString = "";
					if (i) {
						// 判断是否使用默认值
						// 若默認使用默認值則判斷該值是否為空
						vString = String.valueOf(value);
						vString = vString.equals("") ? property.defaultString()
								: vString;
					}
					values.put(property.name().equals("") ? field.getName()
							: property.name(), vString);
					break;
				case CLOB:
				case BLOB:
					// 若保存字节流则需要对对象进行序列化
					// 序列化对象不允许有默认值
					values.put(property.name().equals("") ? field.getName()
							: property.name(), value == null ? null
							: serialize(value));
					break;
				case REAL:
					break;
				case DATE:
					break;
				case DATETIME:
					break;
				default:
					values.put(property.name().equals("") ? field.getName()
							: property.name(), String.valueOf(value));
					break;
				}
			}
		}
		tableType.values = values;
		return tableType;
	}

	/**
	 * 序列化對象
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	protected byte[] serialize(Object obj) throws IOException {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		// 序列化
		baos = new ByteArrayOutputStream();
		oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		byte[] bytes = baos.toByteArray();
		return bytes;
	}

	/**
	 * 反序列化
	 * 
	 * @param bytes
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	protected static Object unserialize(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream bais = null;
		// 反序列化
		bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}

	/**
	 * 返回表的所有類型值
	 * @param xml
	 * @return
	 * @throws XmlPullParserException 
	 * @throws ClassNotFoundException 
	 */
	protected HashMap<String, HashMap<String, TableValue>> getTableMap(
			XmlPullParser dataParser) throws XmlPullParserException, ClassNotFoundException {
		// 表更新时使用的hashmap
		HashMap<String, HashMap<String, TableValue>> tableMap = new HashMap<String, HashMap<String, TableValue>>();
		while (dataParser.getEventType() != XmlPullParser.END_DOCUMENT) {
			// 判断标签的起始位置
			if (dataParser.getEventType() == XmlPullParser.START_TAG) {
				// 判断如果标签为table的话
				if ("table".equals(dataParser.getName())) {
					// 获得表名
					String tableName = dataParser.getAttributeValue(null,
							"name");
					// 获得Class
					Class className = Class.forName(dataParser
							.getAttributeValue(null, "ref"));
					// 獲取的表屬性及其表對比值
					Object[] tableObjects = getTablePorperty(className);
					tableMap.put(tableName, (HashMap<String, TableValue>) tableObjects[1]);
				}
				}
			}
		return tableMap;
	}
	
	/**
	 * 私有方法，通过注解类的class来获取创建表的各项元素
	 * 
	 * @param <T>
	 * 
	 * @param cla
	 * @return
	 */
	protected <T> Object[] getTablePorperty(Class<T> cla) {
		// 创建对比map
		HashMap<String, TableValue> map = new HashMap<String, TableValue>();
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
					// 其他类型直接存储为二进制对象並且進行反序列化
					tableValue.type = DataType.BLOB;
				}
			} else {
				tableValue.type = property.type();
			}
			// 加入对比map
			map.put(tableValue.name, tableValue);
			list.add(tableValue);
		}
		return new Object[] { list, map };
	}

	/**
	 * 私有方法 获取主键
	 * 
	 * @param <T>
	 * 
	 * @param cla
	 * @return
	 */
	protected <T> String getTableKye(Class<T> cla) {
		Table table = (Table) cla
				.getAnnotation(org.sqlite.annotation.Table.class);
		if (table != null)
			return table.kyeName();
		else
			return "";

	}
}
