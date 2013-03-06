package com.chen.crawler.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.chen.crawler.entity.QQ;
import com.chen.crawler.login.ClientUtil;
import com.chen.crawler.login.CrawlData;
import com.chen.crawler.utils.DbUtils;
import com.chen.crawler.utils.DialUtils;

/**
 * 
 * @author chenz
 *
 */
public class GetCookieAndGTK {

	private static List<QQ> qqObjs = new ArrayList<QQ>();
	
	static{
		qqObjs = DbUtils.queryAllQQ(1);
	}
	
	public static void getAllCookie(){
		
		final ScheduledExecutorService loginTaskService = Executors.newSingleThreadScheduledExecutor();
		loginTaskService.scheduleAtFixedRate(new Runnable(){
			@Override
			public void run() {
				//重新拨号修改ip
				//boolean isScucess = DialUtils.dial("宽带连接","07550055241@163.gd","YMKOUBXY");
					if(qqObjs!=null && qqObjs.size()>0){
						QQ qqObj = qqObjs.remove(qqObjs.size()-1);
						ClientUtil.init();
						CrawlData crawl  = ClientUtil.getVerfiy(qqObj.getQq(), qqObj.getPwd());
						if(crawl.getVerifyState()  == 1){//需要验证码，
							if(qqObjs.size() % 5 == 0){
								DialUtils.dial("宽带连接","07550055241@163.gd","YMKOUBXY");
							}
							for(int i = 0 ;i <5 ; i++){
								qqObj = qqObjs.remove(qqObjs.size()-1);
								ClientUtil.init();
								crawl  = ClientUtil.getVerfiy(qqObj.getQq(), qqObj.getPwd());
								if(crawl.getVerifyState()  == 0){
									break;
								}
							}
							
						}
						crawl.setQq(qqObj.getQq());
						crawl.setPwd(qqObj.getPwd());
						crawl = ClientUtil.getCookie(crawl);
						//是否为 0 ，是否登录成功，登录，3，密码错误，19，账户禁用
						 //设置登录的状态
						crawl = ClientUtil.setLoginState(crawl);
						if(crawl.getLoginState()==0){
							System.out.println("登陆成功！"+crawl.getQq());
							//
							Map<String, String>  cookies = crawl.getCookies();
							if(cookies!=null && cookies.size()>0){
								//获取当前qq登陆的cookie成功，那么就更新这个QQ对象
								String skey = cookies.get("skey");
								String accessCookie = "";
								String GTK = "";
								for(Map.Entry<String, String> entry : cookies.entrySet()){
									accessCookie += entry.getKey() + "=" + entry.getValue() + "; ";
								}
								if(accessCookie.lastIndexOf(";")!= -1){
									accessCookie = accessCookie.substring(0, accessCookie.length());
								}
								GTK = GetGTK.getGTK(skey);
								qqObj.setAccessCookie(accessCookie);
								qqObj.setAccessToken(GTK);
								qqObj.setState(1);
								DbUtils.updateQQ(qqObj);
							}else{
								DialUtils.dial("宽带连接","07550055241@163.gd","YMKOUBXY");
								System.err.println(crawl.getBackBody());
							}
						}else if(crawl.getLoginState() == 3){
							System.err.println("密码错误！");
						}else if(crawl.getLoginState() == 19){
							System.out.println("账号被禁用！");
						}else{
							System.err.println("登录失败，错误代码为："+crawl.getLoginState());
							System.err.println(crawl.getBackBody());
//							DialUtils.dial("宽带连接","07550055241@163.gd","YMKOUBXY");
//							return;
						}
						
				}else{
					System.err.println("宽带连接出现问题，请检查你的网络");
					loginTaskService.shutdown();
				}
			}
		}, 1, 20, TimeUnit.SECONDS);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GetCookieAndGTK.getAllCookie();
	}
	
	
}
