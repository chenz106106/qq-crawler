package com.chen.crawler.entity;

import java.io.Serializable;

public class QQ implements Serializable {

	private String qq;
	private String pwd;
	private String accessToken;
	private String accessCookie;
	private String ctime;
	/**
	 * 1:正常 2：禁用 3：未知原因
	 */
	private int state = 1;
	public String getQq() {
		return qq;
	}
	public void setQq(String qq) {
		this.qq = qq;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getAccessCookie() {
		return accessCookie;
	}
	public void setAccessCookie(String accessCookie) {
		this.accessCookie = accessCookie;
	}
	public String getCtime() {
		return ctime;
	}
	public void setCtime(String ctime) {
		this.ctime = ctime;
	}
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public QQ(String qq, String pwd, String accessToken, String accessCookie,
			String ctime, int state) {
		super();
		this.qq = qq;
		this.pwd = pwd;
		this.accessToken = accessToken;
		this.accessCookie = accessCookie;
		this.ctime = ctime;
		this.state = state;
	}
	public QQ(){
		
	}
	
	

}
