package org.sqlite.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sqlite.DatabaseCtrl;
import org.sqlite.module.DatabaseVersion;
import org.sqlite.module.TableValue;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * db初始化类 初始化该类即可获取数据库连接
 * 
 * @author talkliu
 * 
 */
public class DataBase extends SQLiteOpenHelper {
	public static final int VERSION = 1;
	private List<String> updateDatabasesList;
	private Context context;
	private XmlPullParser xml = null;
	private String xmlString = null;
	private boolean isonCreate = false;

	public DataBase(Context context, String name, CursorFactory factory,
			int version, boolean isonCreate) {
		super(context, name, factory, version);
		this.isonCreate = isonCreate;
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	public DataBase(Context context, String name, int version,
			boolean isonCreate) {
		this(context, name, null, version, isonCreate);
		this.context = context;
	}

	public DataBase(Context context, XmlPullParser database,
			boolean isonCreate, int version) throws XmlPullParserException,
			IOException {
		this(context, new Xml2Data(context, database).getDatabaseName(),
				version, isonCreate);
		this.context = context;
		xml = database;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// 表结构初始化'
		if (isonCreate) {
			try {
				//初始化版本
				new databaseTableVersion().setOnCreate(db);
				for (String sql : new Xml2Data(context, xml).getCreateSql()) {
					db.execSQL(sql);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 表结构更新
		try {
			Object[] table=new databaseTableVersion().setOnUpgrade(db);
			for(String sql:new Xml2Data(context, xml).getUpdateSql(((DatabaseVersion)table[0]).getTableMap(), (List<String>)table[1])){
				db.execSQL(sql);
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// new Xml2Data(context, xml).getUpdateSql(map)
 catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<String> getUpdateDatabasesList() {
		return updateDatabasesList;
	}

	public void setUpdateDatabasesList(List<String> updateDatabasesList) {
		this.updateDatabasesList = updateDatabasesList;
	}
	
	/**
	 * 用于更新表数据的各种方法
	 * @author young
	 *
	 */
	private class databaseTableVersion extends DatabaseUtil{
		/**
		 * 初始化表结构
		 * @param db
		 * @throws ClassNotFoundException
		 * @throws XmlPullParserException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws IOException
		 */
		public void setOnCreate(SQLiteDatabase db) throws ClassNotFoundException, XmlPullParserException, IllegalArgumentException, IllegalAccessException, IOException{
			// 表更新时使用的hashmap
			HashMap<String, HashMap<String, TableValue>> tableMap = getTableMap(xml);
			DatabaseVersion databaseVersion = new DatabaseVersion();
			databaseVersion.setTableMap(tableMap);
			db.execSQL("create table system_sqliteCtrl_databaseVersion(version integer,tableMap BLOB)");
			// 插入最初表结构版本
			db.insert("system_sqliteCtrl_databaseVersion", null,
					getObjectContentValues(true,
							databaseVersion).values);
		}
		/**
		 * 更新表結構用
		 * @param db
		 * @return
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * @throws NoSuchMethodException
		 * @throws ClassNotFoundException
		 * @throws NoSuchFieldException
		 * @throws IllegalArgumentException
		 * @throws InvocationTargetException
		 * @throws IOException
		 * @throws XmlPullParserException 
		 */
		public Object[] setOnUpgrade(SQLiteDatabase db) throws InstantiationException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, InvocationTargetException, IOException, XmlPullParserException{
			//获取前一版的表数据结构
			String sqlString = "select * from system_sqliteCtrl_databaseVersion order by version desc limit 1";
			String getTables="select * from sqlite_master";
			Cursor tabnm=db.rawQuery(getTables, null);
			List<String> tab_name=new ArrayList<String>();
			while (tabnm.moveToNext()) {
				tab_name.add(tabnm.getString(tabnm.getColumnIndex("tbl_name")));
			}
			Cursor cursor = db.rawQuery(sqlString, null);
			DatabaseVersion dv=(DatabaseVersion) DatabaseCtrl.outObjectList(cursor, DatabaseVersion.class).get(0);
			//保存新版本
			// 表更新时使用的hashmap
			HashMap<String, HashMap<String, TableValue>> tableMap = getTableMap(xml);
			DatabaseVersion databaseVersion = new DatabaseVersion();
			databaseVersion.setTableMap(tableMap);
			db.insert("system_sqliteCtrl_databaseVersion", null,
					getObjectContentValues(true,
							databaseVersion).values);
			
			return new Object[]{dv,tab_name};
		}
	}

}
