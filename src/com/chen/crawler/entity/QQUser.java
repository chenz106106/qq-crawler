package com.chen.crawler.entity;

import java.io.Serializable;

/**
 */
public class QQUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2545181084490798102L;
	
	private long userId = 0l;
	boolean qzoneExists = false;
	boolean qzoneOpen = false;
	String nickname = "";
	String uin = ""; //qq
	int gender = 1;
	String astro = ""; //星座
	String address = "";  //地址
	
	
	public QQUser(boolean qzoneExists, boolean qzoneOpen,
			String nickname, String uin, int gender, String astro, String address) {
		super();
		this.qzoneExists = qzoneExists;
		this.qzoneOpen = qzoneOpen;
		this.nickname = nickname;
		this.uin = uin;
		this.gender = gender;
		this.astro = astro;
		this.address = address;
	}
	public boolean isQzoneExists() {
		return qzoneExists;
	}
	public void setQzoneExists(boolean qzoneExists) {
		this.qzoneExists = qzoneExists;
	}
	public boolean isQzoneOpen() {
		return qzoneOpen;
	}
	public void setQzoneOpen(boolean qzoneOpen) {
		this.qzoneOpen = qzoneOpen;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getUin() {
		return uin;
	}
	public void setUin(String uin) {
		this.uin = uin;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public String getAstro() {
		return astro;
	}
	public void setAstro(String astro) {
		this.astro = astro;
	}
	public String getAddress() {
		return address;
	}
	public void setFrom(String address) {
		this.address = address;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	
	
}
