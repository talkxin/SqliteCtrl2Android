package org.sqlite.util;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
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
	private Integer xml = null;
	private String xmlString = null;
	private boolean isonCreate = false;

	public DataBase(Context context, String name, CursorFactory factory,
			int version,boolean isonCreate) {
		super(context, name, factory, version);
		this.isonCreate = isonCreate;
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	public DataBase(Context context, String name, int version,boolean isonCreate) {
		this(context, name, null, version,isonCreate);
		this.context = context;
	}

	public DataBase(Context context, Integer database,boolean isonCreate)
			throws XmlPullParserException, IOException {
		this(context, new Xml2Data(context, database).getDatabaseName(),
				VERSION,isonCreate);
		this.context = context;
		xml = database;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// 表结构初始化'
		if (isonCreate) {
			try {
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
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 表结构更新
		for (String sql : updateDatabasesList) {
			db.execSQL(sql);
		}
	}

	public List<String> getUpdateDatabasesList() {
		return updateDatabasesList;
	}

	public void setUpdateDatabasesList(List<String> updateDatabasesList) {
		this.updateDatabasesList = updateDatabasesList;
	}

}