package org.sqlite.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

import org.sqlite.annotation.Property;
import org.sqlite.annotation.Table;
import org.sqlite.module.TableType;

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
	public static <T> TableType getObjectContentValues(boolean i, T object)
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
	public static byte[] serialize(Object obj) throws IOException {
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
	public static Object unserialize(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream bais = null;
		// 反序列化
		bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}
}
