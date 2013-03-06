package com.chen.crawler.utils;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * 查询本机的ip地址
 * @author chenz
 *
 */
public class IPUtils {

	private static final String requestUrl = "http://iframe.ip138.com/ic.asp";
	
	/**
	 * 
	 * @return
	 */
	public static String getIPInfo(){
		
		String ipInfo = sendGetRequest();
		
		try {
			ipInfo = new String(ipInfo.getBytes("iso-8859-1"),"gb2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ipInfo;
	}
	
	public static String sendGetRequest(){
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(requestUrl);
		String content = "";
		try {
			HttpResponse response = httpClient.execute(httpGet);
			response.setHeader("Content-Type", "text/html; charset=utf-8");
			content = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			httpClient.getConnectionManager().shutdown();
		}
		return content;
	}
	
	/**
	 * 获取当前机器的ip地址
	 * @return
	 */
	public static String getIp(){
		String content = getIPInfo();
		String ip = "";
		if(content==null || content.trim().equals("")){
			return "";
		}
		int start = content.indexOf("[");
		int end = content.lastIndexOf("]");
		if(start<=end && start!=-1 &&end>1){
			ip = content.substring(start+1, end);
		}
		return ip;
	}
	
	public static String getFromAndProvider(){
		String content = getIPInfo();
		
		int start = content.indexOf("来自：");
		int end = content.indexOf("</center>");
		if(start<=end && start!=-1 && end>1){
			content = content.substring(start+3, end);
		}
		return content;
	}
	/**
	 * 获取当前ip所属的地区的服务商
	 * @return
	 */
	public static String getFrom(){
		
		String info = getFromAndProvider();
		
		String []result = info.split("\\s");
		if(result!=null && result.length>1){
			info = result[0];
		}else{
			info = "";
		}
		return info;
	}
	
	/**
	 * 获取服务提供商
	 * @return
	 */
	public static String getProvider(){
		
		String info = getFromAndProvider();
		
		String []result = info.split("\\s");
		if(result!=null && result.length>1){
			info = result[1];
		}else{
			info = "";
		}
		return info;
	}
	public static void main(String[] args) {
		String s = getFrom();
		System.out.println(s);
	}
	
}
