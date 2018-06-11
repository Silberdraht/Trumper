package de.hska.lkit.demo.redis.model.Impl;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private String u_id;
	private String online;
	private String name;
	private String password;


	public User() {

	}

	public String getId() {
		return u_id;
	}

	public void setId(String u_id) {
		this.u_id = u_id;
	}

	public String getUsername() {
		return name;
	}

	public void setUsername(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOnline() {
		return online;
	}

	public void setOnline(String firstname) {
		this.online = online;
	}


}
