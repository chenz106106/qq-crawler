package com.chen.crawler;

import java.util.Map;

import com.chen.crawler.entity.QQUser;
import com.chen.crawler.utils.DbUtils;


public class ThreadPoolTask implements Runnable {

	private int index;
	
	public ThreadPoolTask(int index){
		this.index = index;
	}
	@Override
	public void run() {
		
		for(Map.Entry<QQUser, String> entry : QQZoneCrawler.qqUsers.entrySet()){
			QQUser user = entry.getKey();
			String tableName = entry.getValue();
			QQZoneCrawler.qqUsers.remove(user);
			DbUtils.InsertUserinfo(user, tableName);
		}
	}
	
}
