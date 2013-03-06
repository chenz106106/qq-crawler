package com.chen.crawler.login;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.coola.jutil.j4log.Logger;


public class ClientUtil {
	
	private static Logger logger  = Logger.getLogger(ClientUtil.class.toString());
	private static String path;
	static{
		    //获得根目录
			path  = ClientUtil.class.getClassLoader().getResource("").getPath().replace("%20", " ");
	}
	private static Map<String,String> ssid = new HashMap<String, String>();
	/**
	 * 是否还在运行之中
	 */
	private static boolean isRunning = false;
	public static boolean getIsRunning(){
		return isRunning;
	}
	
	public static void init(){
		ssid.clear();
		ssid.put("pgv_pvid", getPvId());
		ssid.put("pgv_info", getSsid());
	}
	/**
	 * 登录获取Cookie
	 */
	public static CrawlData getVerfiy(String qq,String pwd){
		isRunning = true;
		String url  = "http://check.ptlogin2.qq.com/check?uin="+qq+"&appid=549000912&ptlang=2052&js_type=2&js_ver=10009&r=" + Math.random();
		CrawlData crawl  =  getMethod(url,setVerifyHead(null));
	    isRunning  = false;
	    return setVerify(crawl);
		
	}
	
	
	/**
	 * 登录获取Cookie
	 */
	public static CrawlData getCookie(CrawlData crawl){
		 StringBuffer sb =new StringBuffer();
		 String pwd  = setPwd(crawl);
		 sb.append("http://ptlogin2.qq.com/login?ptlang=2052&u="+crawl.getQq()+"&p="+pwd+"&verifycode="+crawl.getVerify());
		 sb.append("&css=http://imgcache.qq.com/ptcss/b2/sjpt/549000912/qzonelogin_ptlogin.css&mibao_css=m_qzone&aid=549000912&u1=http%3A%2F%2Fqzs.qq.com%2Fqzone%2Fv5%2Floginsucc.html%3Fpara%3Dizone");
		 sb.append("&ptredirect=1&h=1&from_ui=1&dumy=&fp=loginerroralert&action=1-0-9775&g=1&t=1&dummy=&js_type=2&js_ver=10009");
		return getMethod(sb.toString(),setVerifyHead(crawl));
	}
	
	/**
	 * 添加微信
	 */
	public static CrawlData addWeixin(CrawlData crawl,String fuser){
		String url  = "http://weixin.qq.com/cgi-bin/friend?muin="+crawl.getQq()+"&fuser=wxid_3uzfiywrld9121&t=weixin_card&s=addfriend&r="+Math.random();
		Map<String,String> heades = new HashMap<String, String>();
		heades.put("referer", "http://weixin.qq.com/cgi-bin/showcard?t=weixin_card");
		heades.put("User-Agent","Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20100101 Firefox/14.0.1");
		if(crawl != null && crawl.getCookies() != null && crawl.getCookies().size() >  0){
			String ck  = "";
			for(Entry<String, String> e : crawl.getCookies().entrySet()){
				if(StringUtils.isNotBlank(e.getKey()) && StringUtils.isNotBlank(e.getValue())){
					ck += e.getKey()+"="+e.getValue()+"; " ;
				}
			}
			heades.put("Cookie", ck);
		}
		return getMethod(url,heades);
	}
	
	private static CrawlData  getMethod(String url,Map<String, String> heades){
		CrawlData crawl = new CrawlData();
		DefaultHttpClient httpclientme = null;
		InputStreamReader  in  = null ;
		BufferedReader br = null;
		HttpResponse response = null;
		try {
			//获取Cookie的信息
			 httpclientme = new DefaultHttpClient();
			// 创建一个本地Cookie存储的实例
			CookieStore cookieStore = new BasicCookieStore();
			//cookieStore.addCookie();
			//创建一个本地上下文信息
			HttpContext localContext = new BasicHttpContext();
			//在本地上下问中绑定一个本地存储
			localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			//设置请求的路径
			HttpGet httpGet = new HttpGet(url);
			//请求头存放的东西
			//请求头和cookie
			if(heades != null && heades.size() > 0){
				for(Entry<String, String> e : heades.entrySet()){
					httpGet.setHeader(e.getKey(), e.getValue());
				}
			}
			//传递本地的http上下文给服务器
		    response = httpclientme.execute(httpGet, localContext);
		    //网络访问成功
		    crawl.setStatus(response.getStatusLine().getStatusCode());
		    if(crawl.getStatus() == HttpStatus.SC_OK){
		    	HttpEntity entity = response.getEntity();
		    	if (entity != null) {
				     in = new InputStreamReader(entity.getContent(),"utf-8");
					 br = new BufferedReader(in);
					 crawl.setBackBody(br.readLine());
				}
		    	//cookie
		    	List<Cookie> cookies= cookieStore.getCookies();
		    	if(cookies != null && cookies.size() > 0){
		    		Map<String, String> ck  =  new HashMap<String, String>();
		    		for (Cookie c : cookies) {
		    			if(c.getValue()!=null && !"".equals(c.getValue().trim()))
		    					ck.put(c.getName(), c.getValue());
					}
		    		
		    		ck.putAll(ssid);
		    		
		    		crawl.setCookies(ck);
		    	}
		    	//请求头
		    	Header[] headers = response.getAllHeaders();
		    	if(headers != null && headers.length >  0 ){
		    		Map<String, String> ck  =  new HashMap<String, String>();
		    		for (int i = 0; i<headers.length; i++) {
		    			ck.put(headers[i].getName(), headers[i].getValue());
					}
		    		crawl.setHeads(ck);
		    	}
		    }
		    
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取信息失败！",e);
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
				}
				in  = null;
			}
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
				}
				br  = null;
			}
			if(httpclientme != null){
				httpclientme.getConnectionManager().shutdown();
				httpclientme = null;
			}
		}
		return crawl ;
	} 
	
	private  static  Map<String , String> setVerifyHead(CrawlData crawl){
		Map<String,String> heades = new HashMap<String, String>();
		heades.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		heades.put("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		heades.put("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
		heades.put("Host", "ptlogin2.qq.com");
		heades.put("User-Agent","Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20100101 Firefox/14.0.1");
		
		if(crawl != null && crawl.getCookies() != null && crawl.getCookies().size() >  0){
			String ck  = "";
			
			ck = "pgv_pvid=" + ssid.get("pgv_pvid") + ";pgv_info=" + ssid.get("pgv_info") + ";";
			for(Entry<String, String> e : crawl.getCookies().entrySet()){
				if(e.getValue()!=null || !"".equals(e.getValue().trim()))
					ck += e.getKey()+"="+e.getValue()+"; " ;
			}
			ck  = ck.substring(0,ck.lastIndexOf(";")-1);
			heades.put("Cookie", ck);
		}
		
		return heades;
	}
	/**
	 * 设置验证码
	 */
	private static  CrawlData setVerify(CrawlData crawl){
		//ptui_checkVC('0','!QMH','\x00\x00\x00\x00\x1d\xb8\xb0\xb5')
		if(StringUtils.isNotBlank(crawl.getBackBody()) && crawl.getBackBody().contains("ptui_checkVC")){
			String s  = StringUtils.trim(crawl.getBackBody());
			 s  = s.substring(s.indexOf("(")+1,s.indexOf(")")-1);
			String[] vc = s.split(",");
			if(vc != null && vc.length  == 3){
				crawl.setVerifyState(Integer.parseInt(vc[0].replace("'", "")));
				crawl.setVerify(vc[1].replace("'", ""));
				crawl.setUin(vc[2].replace("'", ""));
			}
		}
		return crawl;
	}
	
	public static CrawlData setLoginState(CrawlData crawl){
		//ptuiCB('0','0','http://qzs.qq.com/qzone/v5/loginsucc.html?para=izone','1','登录成功！', '李一薇');
		//ptuiCB('7','0','','0','很遗憾，网络连接出现异常，请您稍后再试。(2488272398)', '1393628740');
		if(StringUtils.isNotBlank(crawl.getBackBody()) && crawl.getBackBody().contains("ptuiCB")){
			String s  = StringUtils.trim(crawl.getBackBody());
			s  = s.substring(s.indexOf("(")+1,s.indexOf(")")-1);
			String[] vc = s.split(",");
			if(vc != null && vc.length >= 1){
				crawl.setLoginState(Integer.parseInt(vc[0].replace("'", "")));
				crawl.setLoginBackBody(crawl.getBackBody());
			}
		}
		return crawl;
	}
	/**
	 * 获取密码
	 */
	private static String setPwd(CrawlData crawl){
		ScriptEngineManager m = new ScriptEngineManager();  
		ScriptEngine se = m.getEngineByName("javascript"); 
		String pwd  = crawl.getPwd();
		try {
			se.eval(new FileReader(new File(path+File.separator+"util.js")));
			pwd = DigestUtils.md5Hex(pwd).toUpperCase();
			Object t =se.eval("checkACD(\""+pwd+"\",\""+crawl.getUin()+"\");");
			pwd = DigestUtils.md5Hex(t+ crawl.getVerify().toUpperCase()).toUpperCase();
		} catch (Exception e) {
			System.out.println("错误！"+e.getMessage());
			e.printStackTrace();
		}  
		return pwd;
	}
	
	public  static String getPvId(){
		Calendar calendar = Calendar.getInstance();
		Double d = Math.round(Math.random() * 2147483647) * (calendar.getTimeInMillis() % 1000) % 1E10 ;
		String pattern = "#";
		DecimalFormat df  = new DecimalFormat(pattern); 
		String temp = df.format(d);
		return temp;
	}
	
	public static  String getSsid(){
		return "ssid=s"+getPvId();
	}
	public static void main(String[] args) {
		System.out.println("ok");
		ClientUtil c = new ClientUtil();
		for(int i = 0 ; i < 100 ; i++){
			String ssid = c.getSsid();
			System.out.println(ssid);
		}
	}
}
