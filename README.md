SqliteCtrl2Android
==================

android的sqlite控制包的源代码，提供对xml生成新表新库，通过注解类对象进行对数据库的操作

一、xml文件的创建
  1、在res文件夹下创建xml文件夹
  2、创建database.xml文件
  
二、xml文件的内容
  1、根标签
  <database>
  2、head标签（数据库标签，在oncreate时会自动创建该数据库，在当前版本下只支持一个数据库的创建）
    name属性为数据库名称
    
  <head name="Database Name" />
  
  3、table标签，对应表名与注解的ORM类，在oncreate时会根据类注解生成表的列
  
      <table
        name="Table Name"
        ref="Your Table's Class" />

三、类注解与属性注解
  1、类注解：注解主键名称，oncreate时会根据该字段生成主键约束，操作时会根据该名称检查主键约束，可以不填
 	/**
	 * 主键名
	 * 
	 * @return
	 */
	String kyeName() default "";
  2、属性注解
  /**
	 * 列名， 默认为属性名特指需指明，该名称为创建数据库表与ORM映射成对象时的对应名称
	 * 
	 * @return
	 */
	String name() default "";

	/**
	 * 字段属性 默认为属性类型（简单类型）
	 * 
	 * @return
	 */
	String type() default "";

	/**
	 * 字段长度，该字段为必填字段
	 * 
	 * @return
	 */
	int length();

	/**
	 * 是否自增长，可以指定任意列为自增长列 默认为false
	 * 
	 * @return
	 */
	boolean isPlus() default false;

	/**
	 * 是否为空 默认为false
	 * 
	 * @return
	 */
	boolean notNull() default false;

	/**
	 * 在新增时会检查属性是否设置了默认值，若设置了默认值会自动填充值
	 * 
	 * @return
	 */
	String defaultString() default "";
	
四、使用及方法
  1、创建
    在任意activity中通过new DatabaseCtrl来获取数据库对象
  2、方法
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
			throws XmlPullParserException, IOException, ClassNotFoundException

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
			throws IOException, XmlPullParserException, ClassNotFoundException

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
			int version)

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
			throws XmlPullParserException, IOException, ClassNotFoundException
			
	/**
	 * 关闭数据库连接
	 */
	public void close()
	
	
	/**
	 * 获取数据库连接直接操作SQLiteDatabase对象
	 * 
	 * @return
	 */
	public SQLiteDatabase getSqLiteDatabase()

	/**
	 * 插入数据
	 * 
	 * @param object
	 */
	public <T> void insert(T object) 

	/**
	 * 普通增加
	 * 
	 * @param tableName
	 *            表名
	 * @param values
	 *            ContentValues的使用方法与map相同
	 */
	public void insert(String tableName, ContentValues values)

	/**
	 * 根据主键修改对象内所有对应列
	 * 
	 * @param objext
	 */
	public <T> void update2Id(T object)

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
	public void update2Where(String table, ContentValues values,
			String whereClause, String... whereArgs)

	/**
	 * 根据主键删除
	 * 
	 * @param object
	 */
	public <T> void delete2Id(T object)

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
	public void delete2Where(String table, String whereClause,
			String... whereArgs)

	/**
	 * 根据主键返回对象
	 * 
	 * @param object
	 * @return
	 */
	public <T> T query2Id(T object)

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
	
