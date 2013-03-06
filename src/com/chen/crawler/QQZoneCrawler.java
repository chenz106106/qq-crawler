package com.chen.crawler;


import static com.chen.crawler.utils.DbUtils.InsertUserinfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;

import com.chen.crawler.entity.QQUser;
import com.chen.crawler.utils.DialUtils;
import com.chen.crawler.utils.HttpUtil;
import com.coola.jutil.j4log.Logger;

import static com.chen.crawler.Constant.*;

/**
 * 抓取qq数据
 * @author chenz
 * 
 */
public class QQZoneCrawler extends WebCrawler {

	private Logger logger = Logger.getLogger(getClass().getName());
	
	//store restaurant url
	private List<URL> urlList = new ArrayList<URL>();
	
	public static Map<QQUser,String> qqUsers = new ConcurrentHashMap<QQUser, String>();
	private static Object mutex = new Object();
	
	public QQZoneCrawler(String startUrl) throws MalformedURLException {
		super(new URL(startUrl));
	}

	public static void main(String[] args) {

		WebCrawler crawler;
		try {
			String startUrl = "http://r.qzone.qq.com/cgi-bin/user/cgi_personal_card?uin=383541006&remark=0&fupdate=1&g_tk=276850213";
			crawler = new QQZoneCrawler(startUrl);
			crawler.init();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 50, 1,
		// TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100),
		// new ThreadPoolExecutor.DiscardOldestPolicy());
		
		ScheduledExecutorService dialTaskService = Executors.newSingleThreadScheduledExecutor();
		dialTaskService.scheduleAtFixedRate(new Runnable(){

			@Override
			public void run() {
				//重新拨号修改ip
				DialUtils.dial("宽带连接","07550055241@163.gd","YMKOUBXY");
			}
			
		}, 60, dialIntervalTime, TimeUnit.SECONDS);
		try {
			// threadPool.execute(new ThreadPoolTask(j));
			while(true){
				//save QQUser into db task
				Map<QQUser, String> temp = new HashMap<QQUser, String>();
				for (Map.Entry<QQUser, String> entry : QQZoneCrawler.qqUsers.entrySet()) {
					temp.put(entry.getKey(), entry.getValue());
					if(temp.size()>saveTaskMaxNumber){
						break;
					}
				}
				for (Map.Entry<QQUser, String> entry : temp.entrySet()) {
					QQUser user = entry.getKey();
					String tableName = entry.getValue();
					QQZoneCrawler.qqUsers.remove(user);
					// start thread to execute task
					SaveThread saveThread = new SaveThread(user, tableName);
					saveThread.start();
				}
				Thread.sleep(saveTaskSleepTime);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected List<URL> processPage(URL url) {
		
		//180000000  5
		long totalRequest = endNum-currentNum;
		long num = totalRequest/crawleThreadNum;
		for(int i=0;i<num;i++){
			String requestURIs[] = getRequestUri(crawleThreadNum);
			//every time send mult get request
			if(!isStarted){
				while(!isStarted){
					//停止了，那么就不发送请求了，让程序停留在这个阶段
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			HttpUtil.sendMultGetRequest(requestURIs);
		}
		
		
		return urlList;
	}
	
	/**
	 * 构造指定数据的请求url
	 * @param num
	 * @return
	 */
	private static String[] getRequestUri(int num){
		if(num <1){
			return null;
		}
		String [] uris = new String[num];
		
		for(int i=0;i<num;i++){
			String baseUri = "http://r.qzone.qq.com/cgi-bin/user/cgi_personal_card?uin=" +
					count.getAndIncrement()+"&remark=0&fupdate=1&g_tk=";
			baseUri += getGTK();
			
			//baseUri += "507868647";
			
			//baseUri += "893970895";
			
			//baseUri = "http://r.qzone.qq.com/cgi-bin/user/cgi_personal_card?uin=100000003&remark=0&fupdate=1&g_tk=893970895";
			
			uris[i] = baseUri;
		}
		return uris;
	}
	
	static class SaveThread extends Thread {
		
		private final QQUser user;
		private final String tableName;
		public SaveThread(QQUser user,String tableName){
			this.user = user;
			this.tableName = tableName;
		}
		 @Override
	     public void run() {
			 InsertUserinfo(user, tableName);
		 }
	}
	public static   List<URL> parseJSONData(String string){
		
		List<URL> uuidList = new ArrayList<URL>();
		if(string==null || "".equals(string)){
			return uuidList ;
		}
		JSONObject jsonObj = JSONObject.fromObject(string);
		System.out.println(string);
		
		
		boolean qzoneExists = false;
		boolean qzoneOpen = false;
		String nickname = "";
		String uin = ""; //qq
		int gender = 1;
		String astro = ""; //星座
		String from = "";  //地址
		
		if(jsonObj.containsKey("error")){
			String msg = JSONObject.fromObject(jsonObj.get("error")).getString("msg");
			//retry request uri
			count.getAndDecrement();
			System.err.println(msg);
			
		}else if(jsonObj.containsKey("data")){
			JSONObject data = jsonObj.getJSONObject("data");
			int qzone = data.getInt("qzone");
			
			//check qzone is existed
			if(qzone==1){
				qzoneExists = true;
				//check have permission to look
				if(data.containsKey("blog")){
					qzoneOpen = true;
				}
				if(data.containsKey("uin") && data.containsKey("nickname") ){
					nickname = data.getString("nickname");
					uin = data.getString("uin");
					if(data.containsKey("gender")){
						gender = data.getInt("gender");
					}
					if(data.containsKey("astro")){
						astro = data.getString("astro");
					}
					if(data.containsKey("from")){
						from = data.getString("from");
					}
					QQUser user = new QQUser(qzoneExists,qzoneOpen,nickname,uin,gender,astro,from);
					//按照 user对象的hashcode 进行模运算 0-99之间的规则来分表
					String tableName = "userinfo_";
					int code = user.hashCode()%100;
					tableName += code;
					//logger.info("insert user:"+user.getNickname()+" to "+tableName);
					qqUsers.put(user, tableName);
				}
				
			}
		}
		
		return uuidList;
	}
}
