package com.chen.crawler.login;

import java.util.Map;


public class CrawlData {
	
	/**
	 * qq号码
	 */
	private String qq;
   
	/**
	 * 密码
	 */
	private String pwd;
	
	/**
	 * 验证的状态
	 */
	private int verifyState = 1 ;
	/**
	 * 登录状态
	 */
	private int loginState = 1;
	/**
	 * 验证码
	 */
	private String verify;
	
	/**
	 * uin
	 */
	private String uin;
	
	/**
	 * 获取的类型
	 */
	private int state ;
	/**
	 * 网络返回状态
	 */
	private int status ;
	
	/**
	 * 返回数据
	 */
	private String backBody;
	
	private String loginBackBody;
	
	
	/**
	 * cookie 数据
	 */
	private Map<String, String>  cookies;
	
	/**
	 * 返回的头
	 */
	private Map<String, String>  heads ;


	public int getState() {
		return state;
	}


	public void setState(int getState) {
		this.state = getState;
	}


	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}


	public String getBackBody() {
		return backBody;
	}


	public void setBackBody(String backBody) {
		this.backBody = backBody;
	}


	public Map<String, String> getCookies() {
		return cookies;
	}


	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}


	public Map<String, String> getHeads() {
		return heads;
	}


	public void setHeads(Map<String, String> heads) {
		this.heads = heads;
	}


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


	public String getVerify() {
		return verify;
	}


	public void setVerify(String verify) {
		this.verify = verify;
	}


	public String getUin() {
		return uin;
	}


	public void setUin(String uin) {
		this.uin = uin;
	}


	public int getVerifyState() {
		return verifyState;
	}


	public void setVerifyState(int verifyState) {
		this.verifyState = verifyState;
	}

	public int getLoginState() {
		return loginState;
	}

	public void setLoginState(int loginState) {
		this.loginState = loginState;
	}

	public String getLoginBackBody() {
		return loginBackBody;
	}

	public void setLoginBackBody(String loginBackBody) {
		this.loginBackBody = loginBackBody;
	}
	
	
	
}
