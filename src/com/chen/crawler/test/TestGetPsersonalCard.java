package com.chen.crawler.test;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.chen.crawler.Constant;

public class TestGetPsersonalCard {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		sendGetRequest();
		
	}
	
	public static String sendGetRequest(){
		
		String content = "";
		
		//第一种情况是cookie可以用，但获取不到性别，位置信息
		//第二种情况是cookie不可以用，返回500错误
		//String GTK = "768001044";
		
		String GTK = "1375502183";
		GTK = "129597254";
		GTK = "213880293";
		HttpClient httpClient = new DefaultHttpClient();
		String url = "http://r.qzone.qq.com/cgi-bin/user/cgi_personal_card?uin=100000003&remark=0&fupdate=1&" +
				"g_tk=";
		url += GTK;
		
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Accept-Language","zh-CN,zh;q=0.8");
		httpGet.setHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
		httpGet.setHeader("Connection", "keep-alive");
		//String cookie = "ptcz=fb05959aeee077a5a057dea85a40fef3c4845613f09b102ceed30b45b6851900; skey=@yrN7fcNgv; pt2gguin=o1328719295; uin=o1328719295; ptuserinfo=e891a3e790bce78e89; RK=hdjTdGBu3V; ptisp=ctc";
		//String cookie = "pgv_pvid=1893935227; pgv_info=ssid=s1053933088; ptisp=ctc; verifysession=h00c8083165c33c8be9120a76333f1828fb1d6a70e0b44b9a6307d279a2995fb741f37c119598bbc2651191114ca4fcb3f4; ptui_loginuin=1335128759; pt2gguin=o1335128759; uin=o1335128759; skey=@C6SGqxZf3; RK=aK4TN2XO0R";
		
		//String cookie = "ETK=; ptcz=ce20d06da59c1091c57ed612847a6a25721d293a1a8fc9512bfb6dbc2f508be9; skey=@7utKul5JW; pt2gguin=o1154400385; uin=o1154400385; ptuserinfo=637868; RK=hLTWkKWq3c; ptisp=ctc; ";
		String cookie = "pgv_pvid=1893935227; pgv_info=ssid=s1053933088; ptisp=ctc; verifysession=h00c8083165c33c8be9120a76333f1828fb1d6a70e0b44b9a6307d279a2995fb741f37c119598bbc2651191114ca4fcb3f4; ptui_loginuin=1154400385; pt2gguin=o1154400385; RK=aK4TN2XO0R; uin=o1154400385; skey=@GfLmSq5pr";
		cookie = "pgv_pvid=6830481750; ptcz=2163829505743f5e77d99b2bbb98c6d8e4a4ee1f426462ccbea0a9f3d7a2f06a; skey=@W9kgZwv5B; pt2gguin=o1449095756; pgv_info=ssid=s8925033750; uin=o1449095756; ptuserinfo=e696b9e4ba91e78795; RK=ii5vxCkaXZ; ptisp=ctc; ";
		//String cookie = Constant.tokenCookieInfo.get(GTK);
		httpGet.setHeader("Cookie", cookie);
		httpGet.setHeader("Host","r.qzone.qq.com");
//		httpGet.setHeader("If-None-Match", "3036166553");	
    	httpGet.setHeader("User-Agent",Constant.getUserAgent());
    	
    	try {
			HttpResponse response = httpClient.execute(httpGet);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				HttpEntity entity = response.getEntity();
				content = EntityUtils.toString(entity);
				System.out.println(content);
			}else{
				System.err.println(response.getStatusLine().getReasonPhrase());
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

}
