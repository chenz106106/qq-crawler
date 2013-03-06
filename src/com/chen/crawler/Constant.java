package com.chen.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.chen.crawler.entity.QQ;
import com.chen.crawler.utils.DbUtils;
import com.sun.corba.se.spi.extension.CopyObjectPolicy;

/**
 * 
 * @author chenz
 *
 */
public class Constant {
	
	/**
	 * 所有用来发送请求的时候需要的参数信息
	 */
	public static List<QQ> qqObjs = new CopyOnWriteArrayList<QQ>();
	
	public static List<String> userAgent = new ArrayList<String>();
	/**
	 * 间隔拨号时间 单位秒
	 */
	public static final int dialIntervalTime = 1200;
	
	/**
	 * 启动发送请求线程的个数
	 */
	public static final int crawleThreadNum = 10;
	/**
	 * 请求线程之间的时间间隔 单位毫秒
	 */
	public static final int crawleSleepTime = 100;
	/**
	 *  保存对象任务线程的休眠时间 单位毫秒
	 */
	public static int saveTaskSleepTime = 200;
	/**
	 * 保存对象任务线程的数量
	 */
	public static final int saveTaskMaxNumber = 6;
	
	public static long startNum = 100000000;
	
	public static long endNum = 999999999;
	
	public static long  currentNum = 100000001;
	
	public static AtomicLong count = new AtomicLong(100000000);
	
	public static Map<String,String> currentTokenInfo = new ConcurrentHashMap<String, String>();
	
	public static Map<String,String> tokenCookieInfo = new ConcurrentHashMap<String, String>();
	
	public static int tokenCount = 0;
	
	public static int userAgentCount = 0;
	/**
	 * 刷新配置信息
	 */
	public static synchronized void reflush(){
		
		qqObjs = DbUtils.queryAllQQ(2);
		
		userAgent = DbUtils.queryAllUserAgent();
		
		
		if(qqObjs==null || qqObjs.size()<1){
			System.err.println("数据库中没有发送请求的qq信息，请在qq表中添加发送请求需要的信息");
			return;
		}
		for(QQ qq : qqObjs){
			tokenCookieInfo.put(qq.getAccessToken(), qq.getAccessCookie());
		}
		
		String token = qqObjs.get(0).getAccessToken();
		String cookie =  qqObjs.get(0).getAccessCookie();
		currentTokenInfo.put(token,cookie);
		tokenCount = qqObjs.size();
		if(userAgent!=null)
			userAgentCount = userAgent.size();
		
		List<Map<String,Object>> countInfos = DbUtils.queryCountInfo();
		if(countInfos!=null && countInfos.size()>0){
			startNum = ((Number)countInfos.get(0).get("startNum")).longValue();
			endNum = ((Number)countInfos.get(0).get("endNum")).longValue();
			currentNum = ((Number)countInfos.get(0).get("currentNum")).longValue();
			count.set(currentNum);
		}
	}
	/**
	 * 按循序循环获取useragent信息
	 * @return
	 */
	public static String getUserAgent(){
		if(userAgentCount<1){
			userAgentCount = userAgent.size();
		}
		if(userAgent!=null && userAgent.size()>0){
			String ua =  userAgent.get(--userAgentCount);
			return ua;
		}else{
			return "Mozilla/5.0 (Windows NT 5.1; rv:18.0) Gecko/20100101 Firefox/18.0";
		}
		
	}
	
	/**
	 * 从map中循环获取GTK 信息
	 * @return
	 */
	public static  String getGTK(){
		if(tokenCount<1){
			tokenCount = qqObjs.size();
			return "";
		}
		QQ qq = qqObjs.get(--tokenCount);
		String cookie = "";
		String GTK = "";
		if(qq!=null){
			GTK = qq.getAccessToken();
			cookie = qq.getAccessCookie();
		}
		currentTokenInfo.clear();
		currentTokenInfo.put(GTK, cookie);
		return GTK;
	}
}
