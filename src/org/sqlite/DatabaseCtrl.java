package org.sqlite;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sqlite.annotation.Property;
import org.sqlite.annotation.Table;
import org.sqlite.util.DataBase;
import org.sqlite.util.Xml2Data;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库增删查改操作类
 * 
 * @author talkliu
 * 
 */
public class DatabaseCtrl {
	/**
	 * 传入的activity
	 */
	private Context context;
	/**
	 * 操作数据库对象
	 */
	private SQLiteDatabase sDatabase;

	/**
	 * 类与表的对照map
	 */
	private HashMap<String, String> ClassForTabelName = new HashMap<String, String>();

	/**
	 * 构造方法，传入context获取默认数据库
	 * 
	 * @param context
	 * @param database
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws ClassNotFoundException
	 */
	public DatabaseCtrl(Context context, Integer database)
			throws XmlPullParserException, IOException, ClassNotFoundException {
		this.context = context;
		// 初始化表类对应
		onCreateTable(database);
		// 连接数据库数据库
		DataBase dbBase = new DataBase(context, database, true);
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
	}

	/**
	 * 构造方法，连接不同的数据库
	 * 
	 * @param context
	 * @param database
	 * @param databaseName
	 * @throws ClassNotFoundException
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public DatabaseCtrl(Context context, Integer database, String databaseName)
			throws IOException, XmlPullParserException, ClassNotFoundException {
		this.context = context;
		// 初始化表类对应
		onCreateTable(database);
		// 连接数据库数据库
		DataBase dbBase = new DataBase(context, databaseName, DataBase.VERSION,
				false);
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
	}

	/**
	 * 构造方法，连接不同数据库的不同版本
	 * 
	 * @param context
	 * @param database
	 * @param databaseName
	 * @param version
	 * @throws ClassNotFoundException
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public DatabaseCtrl(Context context, Integer database, String databaseName,
			int version) throws IOException, XmlPullParserException,
			ClassNotFoundException {
		this.context = context;
		// 初始化表类对应
		onCreateTable(database);
		// 连接数据库数据库
		DataBase dbBase = new DataBase(context, databaseName, version, false);
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
	}

	/**
	 * 构造方法返回默认数据库的不同版本
	 * 
	 * @param context
	 * @param version
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws ClassNotFoundException
	 */
	public DatabaseCtrl(Context context, Integer database, int version)
			throws XmlPullParserException, IOException, ClassNotFoundException {
		this.context = context;
		// 初始化表类对应
		onCreateTable(database);
		// 连接数据库数据库
		DataBase dbBase = new DataBase(context,
				new Xml2Data(context, database).getDatabaseName(), version,
				true);
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
	}

	/**
	 * 关闭数据库连接
	 */
	public void close() {
		sDatabase.close();
	}

	/**
	 * 获取数据库连接直接操作SQLiteDatabase对象
	 * 
	 * @return
	 */
	public SQLiteDatabase getSqLiteDatabase() {
		return sDatabase;
	}

	/**
	 * 插入数据
	 * 
	 * @param object
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public <T> long insert(T object) throws IllegalArgumentException,
			IllegalAccessException {
		return sDatabase.insert(
				ClassForTabelName.get(object.getClass().getName()), null,
				getObjectContentValues(true, object).values);
	}

	/**
	 * 普通增加
	 * 
	 * @param tableName
	 *            表名
	 * @param values
	 *            ContentValues的使用方法与map相同
	 */
	public long insert(String tableName, ContentValues values) throws Exception {
		return sDatabase.insert(tableName, null, values);
	}

	/**
	 * 根据主键修改对象内所有对应列
	 * 
	 * @param objext
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public <T> int update2Id(T object) throws IllegalArgumentException,
			IllegalAccessException {
		TableType tableType = getObjectContentValues(false, object);
		return sDatabase.update(ClassForTabelName.get(object.getClass()
				.getName()), tableType.values, tableType.tableKey + "=?",
				new String[] { String.valueOf(tableType.values
						.get(tableType.tableKey)) });
	}

	/**
	 * 根据条件修改
	 * 
	 * @param table
	 *            表名
	 * @param values
	 *            结果集
	 * @param whereClause
	 *            条件
	 * @param whereArgs
	 *            条件值，可以根据条件进行无限跟参
	 */
	public int update2Where(String table, ContentValues values,
			String whereClause, String... whereArgs) {
		return sDatabase.update(table, values, whereClause, whereArgs);
	}

	/**
	 * 根据主键删除
	 * 
	 * @param object
	 */
	public <T> int delete2Id(T object) {
		try {
			TableType tableType = getObjectContentValues(false, object);
			return sDatabase.delete(tableType.tableKey, tableType.tableKey
					+ "=?", new String[] { String.valueOf(tableType.values
					.get(tableType.tableKey)) });
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 根据条件删除
	 * 
	 * @param table
	 *            表名
	 * @param whereClause
	 *            条件
	 * @param whereArgs
	 *            条件值，可以根据条件进行无限跟参
	 */
	public int delete2Where(String table, String whereClause,
			String... whereArgs) throws Exception {
		return sDatabase.delete(table, whereClause, whereArgs);
	}

	/**
	 * 根据主键返回对象
	 * 
	 * @param object
	 * @return
	 */
	public <T> T query2Id(T object) throws Exception {
		TableType tableType = getObjectContentValues(false, object);
		String sqlString = "select * from "
				+ ClassForTabelName.get(object.getClass().getName())
				+ " where " + tableType.tableKey + "=?";
		Cursor cursor = sDatabase.rawQuery(sqlString, new String[] { String
				.valueOf(tableType.values.get(tableType.tableKey)) });
		List<T> list = outObjectList(cursor, object.getClass());
		return list.size() != 0 ? list.get(0) : null;
	}

	/**
	 * 根据条件返回单个对象
	 * 
	 * @param cla
	 *            返回映射对象的class
	 * @param sql
	 *            sql语句
	 * @param selectionArgs
	 *            sql参数可根据语句无限跟参
	 * @return
	 */
	public <T> T query2Where(Class cla, String sql, String... selectionArgs)
			throws Exception {
		Cursor cursor = sDatabase.rawQuery(sql, selectionArgs);
		List<T> list;
		list = outObjectList(cursor, cla);
		return list.size() != 0 ? list.get(0) : null;
	}

	/**
	 * 返回某表所有数据
	 * 
	 * @param <T>
	 * 
	 * @param object
	 * @return
	 */
	public <T> List<T> queryAllObject(Class cla) throws Exception {
		String sqlString = "select * from "
				+ ClassForTabelName.get(cla.getName());
		Cursor cursor = sDatabase.rawQuery(sqlString, null);
		return outObjectList(cursor, cla);
	}

	/**
	 * 对某表所有数据进行分页
	 * 
	 * @param <T>
	 * 
	 * @param object
	 *            module
	 * @param start
	 *            起始页
	 * @param pageNum
	 *            当前页
	 * @param where
	 *            分页条件条件以and分割
	 * @param selectionArgs
	 *            条件跟参
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> queryAllObjectForLimit(Class cla, int start,
			int pageNum, String where, String... selectionArgs)
			throws Exception {
		String sqlString = "select * from "
				+ ClassForTabelName.get(cla.getName()) + " where 1=1 " + where
				+ " limit " + start + "," + pageNum;
		Cursor cursor = sDatabase.rawQuery(sqlString, selectionArgs);
		return outObjectList(cursor, cla);
	}

	/**
	 * 返回某表的总条目数
	 * 
	 * @param class1
	 * @return
	 * @throws Exception
	 */
	public Integer queryCount(Class cla) {
		String table = ClassForTabelName.get(cla.getName());
		String sqlString = "select count(*) as count from " + table;
		Cursor cursor = sDatabase.rawQuery(sqlString, null);
		cursor.moveToFirst();
		return Integer
				.parseInt(cursor.getString(cursor.getColumnIndex("count")));
	}

	/**
	 * 直接使用sql操作数据库(无返回值)
	 * 
	 * @param sql
	 * @param obj
	 */
	public void execForSql(String sql, Object[] obj) {
		sDatabase.execSQL(sql, obj);
	}

	/**
	 * 直接使用sql进行查询返回字符串数组
	 * 
	 * @param sql
	 * @param obj
	 * @return Cursor的操作与map相同
	 */
	public Cursor queryForSql(String sql, String[] obj) {
		return sDatabase.rawQuery(sql, obj);
	}

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
	 */
	private <T> TableType getObjectContentValues(boolean i, T object)
			throws IllegalArgumentException, IllegalAccessException {
		// 创建class
		Class cla = object.getClass();
		// 创建一个结果集对象
		TableType tableType = new TableType();
		// 返回类注解
		Table table = (Table) cla
				.getAnnotation(org.sqlite.annotation.Table.class);
		tableType.tableKey = table.kyeName();
		// 返回cla的所有内容
		Field[] allFields = cla.getDeclaredFields();
		// 创建一个结果集
		ContentValues values = new ContentValues();
		for (Field field : allFields) {
			// 参数值为true，禁用访问控制检查
			field.setAccessible(true);
			// 获取私有属性值
			String vString = String.valueOf(field.get(object));
			// 返回属性注解
			Property property = field.getAnnotation(Property.class);
			if (!property.isPlus() && !i) {
				// 非递增属性并且不是用默认值
				values.put(property.name().equals("") ? field.getName()
						: property.name(), vString);
			} else if (!property.isPlus() && i) {
				// 非递增，并且使用默认值
				values.put(property.name().equals("") ? field.getName()
						: property.name(),
						vString.equals("") ? property.defaultString() : vString);
			}
		}
		tableType.values = values;
		return tableType;
	}

	/**
	 * 将Cursor结果集反射生成对象
	 * 
	 * @param <T>
	 * 
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	public static <T> List<T> outObjectList(Cursor cursor, Class cl)
			throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			InvocationTargetException {
		List<T> objList = new ArrayList<T>();
		// String[] nameStrings = cursor.getColumnNames();
		while (cursor.moveToNext()) {
			Object object = cl.newInstance();
			// 返回cla的所有内容
			Field[] allFields = cl.getDeclaredFields();
			for (Field field : allFields) {
				// 返回属性注解
				Property property = field.getAnnotation(Property.class);
				Constructor con = field.getType().getConstructor(String.class);
				field.setAccessible(true);
				String input = null;
				// 若列名为属性名的则直接转换
				if (property.name().equals("")) {
					input = cursor.getString(cursor.getColumnIndex(field
							.getName()));
				} else {
					// 若列名不是属性名的，获取注解中的列名获取值
					input = cursor.getString(cursor.getColumnIndex(property
							.name()));
				}
				if (input != null) {
					field.set(object, con.newInstance(input));
				}
			}
			objList.add((T) object);
		}
		return objList;
	}

	/**
	 * 存储主键与结果集的对象
	 * 
	 * @author talkliu
	 * 
	 */
	private class TableType {
		/**
		 * ' 主键名
		 */
		public String tableKey;
		/**
		 * 进行操作的结果集
		 */
		public ContentValues values;
	}

	/**
	 * 初始化表名与类的对比map
	 * 
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws ClassNotFoundException
	 */
	protected void onCreateTable(Integer xml) throws IOException,
			XmlPullParserException, ClassNotFoundException {
		XmlResourceParser dataParser = context.getResources().getXml(xml);
		while (dataParser.getEventType() != XmlResourceParser.END_DOCUMENT) {
			// 判断标签的起始位置
			if (dataParser.getEventType() == XmlResourceParser.START_TAG) {
				// 判断如果标签为table的话
				if ("table".equals(dataParser.getName())) {
					// 获得表名
					String tableName = dataParser.getAttributeValue(null,
							"name");
					// 获得Class
					Class className = Class.forName(dataParser
							.getAttributeValue(null, "ref"));
					// 将表名与类名放入map
					ClassForTabelName.put(className.getName(), tableName);
				}
			}
			dataParser.next();
		}
		dataParser.close();
	}
}
