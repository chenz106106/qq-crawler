package com.chen.crawler.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class ClientFormLogin {

	 public static void main(String[] args) throws Exception {

	        DefaultHttpClient httpclient = new DefaultHttpClient();
	        try {
	        	
	        	String url = "";
	        	String url1 = "http://ptlogin2.qq.com/login" ;
	        	url = "http://xui.ptlogin2.qq.com/cgi-bin/qlogin?domain=qq.com&lang=2052&qtarget=0&jumpname=&ptcss=&param=u1%253Dhttp%25253A%25252F%25252Fimgcache.qq.com%25252Fac%25252Fqzone%25252Flogin%25252Fsucc.html&css=&mibao_css=&low_login=0&ptui_version=10019";
	        	
	        	  
//	            HttpGet httpGet = new HttpGet("http://user.qzone.qq.com/158124933");
//	            HttpResponse response2 = httpclient.execute(httpGet);
//	            HttpEntity entity = response2.getEntity();
//	            System.out.println("Login form get: " + response2.getStatusLine());
//	            List<Cookie> cookies2 = httpclient.getCookieStore().getCookies();
//	            if (cookies2.isEmpty()) {
//	                System.out.println("None");
//	            } else {
//	                for (int i = 0; i < cookies2.size(); i++) {
//	                    System.out.println("- " + cookies2.get(i).toString());
//	                }
//	            }
//	            
//	            if(true)
//	            return ;
	            
	            HttpPost httpPost = new HttpPost(url1);
	            
	            httpPost.setHeader("(Request-Line)","POST /login HTTP/1.1");
	            httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20100101 Firefox/15.0.1");  
	            httpPost.setHeader("Referer", "http://www.zhihu.com/");  
	            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");  
	            httpPost.setHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
	            httpPost.setHeader("Accept-Encoding","gzip, deflate");
	            httpPost.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	            httpPost.setHeader("Connection","keep-alive");
	            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
	            nvps.add(new BasicNameValuePair("u", "1143942460"));
	            nvps.add(new BasicNameValuePair("p", "wliofyrb"));

	            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

	            HttpResponse response = httpclient.execute(httpPost);
	            HttpEntity entity = response.getEntity();

	            System.out.println("Login form get: " + response.getStatusLine());
	            EntityUtils.consume(entity);

	            System.out.println("Post logon cookies:");
	            List<Cookie> cookies = httpclient.getCookieStore().getCookies();
	            if (cookies.isEmpty()) {
	                System.out.println("None");
	            } else {
	                for (int i = 0; i < cookies.size(); i++) {
	                    System.out.println("- " + cookies.get(i).toString());
	                }
	            }

	        } finally {
	            // When HttpClient instance is no longer needed,
	            // shut down the connection manager to ensure
	            // immediate deallocation of all system resources
	            httpclient.getConnectionManager().shutdown();
	        }
	    }
}
