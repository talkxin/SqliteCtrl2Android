package org.sqlite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sqlite.annotation.Property;
import org.sqlite.annotation.Table;
import org.sqlite.module.TableType;
import org.sqlite.util.DataBase;
import org.sqlite.util.DatabaseUtil;
import org.sqlite.util.Xml2Data;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Xml;

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
	 * 创建xml对象
	 */
	private static XmlPullParser dataParser = Xml.newPullParser();

	/**
	 * 数据库链接
	 */
	private DataBase dbBase;

	public static int VERSION;

	static void init() throws ClassNotFoundException, XmlPullParserException {
		// TODO Auto-generated method stub
		Class.forName("dalvik.system.VMRuntime");
		dataParser
				.setInput(DatabaseCtrl.class
						.getResourceAsStream("/res/xml/database.xml"), "utf-8");
	}

	static {
		try {
			init();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 构造方法，传入context获取默认数据库
	 * 
	 * @param context
	 * @param database
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws ClassNotFoundException
	 */
	public DatabaseCtrl(Context context) throws XmlPullParserException,
			IOException, ClassNotFoundException {
		this.context = context;
		// 初始化表类对应
		onCreateTable();
		// 连接数据库数据库
		dbBase = new DataBase(context, dataParser, true, VERSION);
		// // 初始化连接
		// sDatabase = dbBase.getWritableDatabase();
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
	@Deprecated
	public DatabaseCtrl(Context context, Integer database, String databaseName)
			throws IOException, XmlPullParserException, ClassNotFoundException {
		this.context = context;
		// 初始化表类对应
		onCreateTable();
		// 连接数据库数据库
		dbBase = new DataBase(context, databaseName, DataBase.VERSION, false);
		// // 初始化连接
		// sDatabase = dbBase.getWritableDatabase();
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
	@Deprecated
	public DatabaseCtrl(Context context, String databaseName, int version)
			throws IOException, XmlPullParserException, ClassNotFoundException {
		this.context = context;
		// 初始化表类对应
		onCreateTable();
		// 连接数据库数据库
		dbBase = new DataBase(context, databaseName, version, false);
		// // 初始化连接
		// sDatabase = dbBase.getWritableDatabase();
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
	public DatabaseCtrl(Context context, int version)
			throws XmlPullParserException, IOException, ClassNotFoundException {
		this.context = context;
		// 初始化表类对应
		onCreateTable();
		// 连接数据库数据库
		dbBase = new DataBase(context,
				new Xml2Data(context, dataParser).getDatabaseName(), version,
				true);
		// // 初始化连接
		// sDatabase = dbBase.getWritableDatabase();
	}

	/**
	 * 关闭数据库连接
	 */
	@Deprecated
	public void close() {
		// sDatabase.close();
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
	 * @throws IOException
	 */
	public <T> long insert(T object) throws IllegalArgumentException,
			IllegalAccessException, IOException {
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		long count = sDatabase.insert(
				ClassForTabelName.get(object.getClass().getName()), null,
				DatabaseUtil.getObjectContentValues(true, object).values);
		sDatabase.close();
		return count;
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
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		long count = sDatabase.insert(tableName, null, values);
		sDatabase.close();
		return count;
	}

	/**
	 * 根据主键修改对象内所有对应列
	 * 
	 * @param objext
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public <T> int update2Id(T object) throws IllegalArgumentException,
			IllegalAccessException, IOException {
		TableType tableType = DatabaseUtil
				.getObjectContentValues(false, object);
		if (tableType.tableKey == null || tableType.tableKey.equals(""))
			return 0;
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		int count = sDatabase.update(ClassForTabelName.get(object.getClass()
				.getName()), tableType.values, tableType.tableKey + "=?",
				new String[] { String.valueOf(tableType.values
						.get(tableType.tableKey)) });
		sDatabase.close();
		return count;
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
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		int count = sDatabase.update(table, values, whereClause, whereArgs);
		sDatabase.close();
		return count;
	}

	/**
	 * 根据主键删除
	 * 
	 * @param object
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public <T> int delete2Id(T object) throws IllegalArgumentException,
			IllegalAccessException, IOException {
		TableType tableType = DatabaseUtil
				.getObjectContentValues(false, object);
		if (tableType.tableKey == null || tableType.tableKey.equals(""))
			return 0;
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		int count = sDatabase.delete(tableType.tableKey, tableType.tableKey
				+ "=?", new String[] { String.valueOf(tableType.values
				.get(tableType.tableKey)) });
		sDatabase.close();
		return count;
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
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		int count = sDatabase.delete(table, whereClause, whereArgs);
		sDatabase.close();
		return count;
	}

	/**
	 * 根据主键返回对象
	 * 
	 * @param object
	 * @return
	 */
	public <T> T query2Id(T object) throws Exception {
		TableType tableType = DatabaseUtil
				.getObjectContentValues(false, object);
		if (tableType.tableKey == null || tableType.tableKey.equals(""))
			return null;
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		String sqlString = "select * from "
				+ ClassForTabelName.get(object.getClass().getName())
				+ " where " + tableType.tableKey + "=?";
		Cursor cursor = sDatabase.rawQuery(sqlString, new String[] { String
				.valueOf(tableType.values.get(tableType.tableKey)) });
		List<T> list = outObjectList(cursor, object.getClass());
		sDatabase.close();
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
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		Cursor cursor = sDatabase.rawQuery(sql, selectionArgs);
		List<T> list;
		list = outObjectList(cursor, cla);
		sDatabase.close();
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
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		String sqlString = "select * from "
				+ ClassForTabelName.get(cla.getName());
		Cursor cursor = sDatabase.rawQuery(sqlString, null);
		sDatabase.close();
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
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		String sqlString = "select * from "
				+ ClassForTabelName.get(cla.getName()) + " where 1=1 " + where
				+ " limit " + start + "," + pageNum;
		Cursor cursor = sDatabase.rawQuery(sqlString, selectionArgs);
		sDatabase.close();
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
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		String table = ClassForTabelName.get(cla.getName());
		String sqlString = "select count(*) as count from " + table;
		Cursor cursor = sDatabase.rawQuery(sqlString, null);
		cursor.moveToFirst();
		sDatabase.close();
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
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		sDatabase.execSQL(sql, obj);
		sDatabase.close();
	}

	/**
	 * 直接使用sql进行查询返回字符串数组
	 * 
	 * @param sql
	 * @param obj
	 * @return Cursor的操作与map相同
	 */
	public Cursor queryForSql(String sql, String[] obj) {
		// 初始化连接
		sDatabase = dbBase.getWritableDatabase();
		Cursor cursor = sDatabase.rawQuery(sql, obj);
		sDatabase.close();
		return cursor;
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
	 * @throws IOException
	 */
	public static <T> List<T> outObjectList(Cursor cursor, Class cl)
			throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			InvocationTargetException, IOException {
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
				Object input = null;
				// 若列名为属性名的则直接转换
				int tableName = cursor.getColumnIndex(property.name()
						.equals("") ? field.getName() : property.name());
				// if (property.name().equals("")) {
				// input = cursor.getString(cursor.getColumnIndex(field
				// .getName()));
				// } else {
				// // 若列名不是属性名的，获取注解中的列名获取值
				// input = cursor.getString(cursor.getColumnIndex(property
				// .name()));
				// }
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
				case BOOLEAN:
				case DECIMAL:
					input = cursor.getString(tableName);
					break;
				case CLOB:
				case BLOB:
					// unSerialize
					input = DatabaseUtil.unserialize(cursor.getBlob(tableName));
					break;
				case REAL:
					break;
				case DATE:
					break;
				case DATETIME:
					break;
				default:
					break;
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
	 * 初始化表名与类的对比map
	 * 
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws ClassNotFoundException
	 */
	protected void onCreateTable() throws IOException, XmlPullParserException,
			ClassNotFoundException {
		while (dataParser.getEventType() != XmlPullParser.END_DOCUMENT) {
			// 判断标签的起始位置
			if (dataParser.getEventType() == XmlPullParser.START_TAG) {
				if ("head".equals(dataParser.getName())) {
					VERSION = Integer.parseInt(dataParser.getAttributeValue(
							null, "version"));
					break;
				}
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
		// dataParser.close();
	}
}
