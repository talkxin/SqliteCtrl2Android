package com.sqliteCtrl.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;

/***
 * 数据库初始化时读取xml表结构文件，进行表结构初始化
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
	private Integer r2xml;
	/**
	 * xml解析对象
	 */
	private XmlResourceParser dataParser;

	/**
	 * 构造方法，若xmlstr为空，则初始化默认xml表结构，否则则按照该xml初始化数据库
	 * 
	 * @param context
	 * @param xmlStr
	 */
	public Xml2Data(Context context, String xmlStr) {
		this.actContext = context;
		this.xmlStr = xmlStr;
	}

	/**
	 * 获取配置文件中不同的xml
	 * 
	 * @param context
	 * @param database
	 */
	public Xml2Data(Context context, Integer database) {
		this.actContext = context;
		this.r2xml = database;
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
		dataParser = actContext.getResources().getXml(r2xml);
			while (dataParser.getEventType() != XmlResourceParser.END_DOCUMENT) {
				if ("head".equals(dataParser.getName())) {
					databaseName = dataParser.getAttributeValue(null, "name");
					break;
				}
				dataParser.next();
			}
			dataParser.close();
		return databaseName;
	}

	/**
	 * 将xml解析成表创建数据
	 * 
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public List<String> getCreateSql() throws IOException,
			XmlPullParserException {
		XmlResourceParser dataParser = null;
		if (xmlStr == null) {
			dataParser = actContext.getResources().getXml(r2xml);
		} else {
			dataParser = actContext.getResources().getAssets()
					.openXmlResourceParser(xmlStr);

		}
		StringBuffer xmlStringBuffer = new StringBuffer();
		List<String> list = new ArrayList<String>();
		StringBuffer sBuffer = null;
		boolean addPro = false;
		while (dataParser.getEventType() != XmlResourceParser.END_DOCUMENT) {
			if (dataParser.getText() != null)
				xmlStringBuffer.append(dataParser.getText());
			addPro = false;
			if (dataParser.getEventType() == XmlResourceParser.START_TAG) {
				if ("table".equals(dataParser.getName())) {
					sBuffer = new StringBuffer();
					sBuffer.append("create table "
							+ dataParser.getAttributeValue(null, "name") + "(");
				} else if ("property".equals(dataParser.getName())) {
					sBuffer.append(dataParser.getAttributeValue(null, "name")
							+ " " + dataParser.getAttributeValue(null, "type"));
				}
			}
			if (dataParser.getEventType() == XmlResourceParser.END_TAG) {
				if ("table".equals(dataParser.getName())) {
					addPro = true;
				} else if ("property".equals(dataParser.getName())) {
					sBuffer.append(",");
				}
			}
			if (sBuffer != null && addPro) {
				String tableString = sBuffer.toString().substring(0,
						sBuffer.toString().length() - 1);
				list.add(tableString + ")");
			}
			dataParser.next();
		}
		dataParser.close();
		return list;
	}

	/**
	 * 测试方法
	 */
	@Deprecated
	public void getDataName() {
		try {
			while (dataParser.getEventType() != XmlResourceParser.END_DOCUMENT) {
				if (dataParser.getEventType() == XmlResourceParser.START_TAG) {
					if ("database".equals(dataParser.getName())) {
						System.out.println("数据库");
						System.out.println(dataParser.getAttributeValue(null,
								"name"));
					} else if ("table".equals(dataParser.getName())) {
						System.out.println("表");
						System.out.println(dataParser.getAttributeValue(null,
								"name"));
					} else if ("property".equals(dataParser.getName())) {
						System.out.println("结构");
						System.out.println(dataParser.getAttributeValue(null,
								"name"));
						System.out.println(dataParser.getAttributeValue(null,
								"type"));

					}
				}
				dataParser.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
