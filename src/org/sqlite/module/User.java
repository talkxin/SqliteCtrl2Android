package org.sqlite.module;

import org.sqlite.annotation.Property;
import org.sqlite.annotation.Table;

@Table(kyeName = "id")
public class User {
	@Property(isPlus = true, length = 1)
	private Integer id;
	@Property(length = 10)
	private String name;
	@Property(name = "passwd", length = 20, type = "varchar")
	private String psd;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPsd() {
		return psd;
	}

	public void setPsd(String psd) {
		this.psd = psd;
	}

}
