package org.sqlite.module;

import java.io.Serializable;
import java.util.HashMap;

import org.sqlite.annotation.Property;
import org.sqlite.annotation.SQLType;

public class DatabaseVersion implements Serializable {
	@Property(type = SQLType.INTEGER)
	private Integer version;
	@Property(type = SQLType.BLOB)
	private HashMap<String, HashMap<String, TableValue>> tableMap;

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public HashMap<String, HashMap<String, TableValue>> getTableMap() {
		return tableMap;
	}

	public void setTableMap(
			HashMap<String, HashMap<String, TableValue>> tableMap) {
		this.tableMap = tableMap;
	}

}
