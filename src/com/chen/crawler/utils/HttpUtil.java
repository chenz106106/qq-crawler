package com.chen.crawler.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.chen.crawler.Constant;
import com.chen.crawler.QQZoneCrawler;
import com.chen.crawler.entity.QQ;
import com.chen.crawler.utils.MyProxy.GetThread;

/**
 * 
 * @author chenz
 *
 */
public class HttpUtil {

	private static Logger logger = Logger.getAnonymousLogger();
	private static PoolingClientConnectionManager pcm = new PoolingClientConnectionManager();
	
	private static final int MAX = 10;
	
	private static  DefaultHttpClient httpclient = null;
	
	private static List<MyProxy> proxys = new ArrayList<MyProxy>();
	private volatile static int i = 0;
	
	
	static{
		pcm.setMaxTotal(MAX);
		String[] ips = new String[]{
				"206.72.205.251",
				"58.67.147.199",
				"113.98.62.135",
		};
		
		int [] ports = new int[]{
			60583,
			8080,
			8080,
		};
		
		for(int i = 0; i<ips.length; i++){
			MyProxy myProxy = new MyProxy(ips[i],ports[i]);
			proxys.add(myProxy);
		}
		
	}
	
	synchronized private static  MyProxy  GetAProxy(){
		
		if(i == proxys.size()){
			i = 0;
		}
		return proxys.get(i++);
	}
	/**
	 * send get request
	 * @param url
	 * @return
	 */
	public static String sendGetRequest(String url,boolean userProxy,Map<String,String> params){
		
		
		if(url==null || url.equals("")){
			return "";
		}
		
		String content = "";
		
		httpclient = new DefaultHttpClient(pcm);
		if(userProxy){
			MyProxy myProxy = GetAProxy();
			HttpHost proxy = new HttpHost(myProxy.getIP(),myProxy.getPort());
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);
		}
		
		HttpGet httpGet = null;
		try {
			URIBuilder uriBuilder = new URIBuilder(url);
			if(params!=null)
				for(Map.Entry<String,String> entry : params.entrySet()){
					uriBuilder.setParameter(entry.getKey(), entry.getValue());
				}
			
			URI uri = uriBuilder.build();
			
			httpGet = new HttpGet(uri);
			//最后一个代理服务器 IP 
			httpGet.setHeader("REMOTE_ADDR","203.98.182.163");
			//代理服务器 IP
			httpGet.setHeader("HTTP_VIA","");
			httpGet.setHeader("X_FORWARDED_FOR", "");
			httpGet.setHeader("Cookie","randomSeed=710127; __Q_w_s_hat_seed=1; __Q_w_s__QZN_TodoMsgCnt=1; hot_feeds_key=015389c550a10ae3|015389c5509cef93; __Q_w_s__appDataSeed=8; ptui_loginuin=158124908; ptisp=ctc; RK=hf53la8eXJ; qv_swfrfh=qun.t.qq.com; qv_swfrfc=v18; pgv_pvid=6040780116; pgv_info=ssid=s1135998944; o_cookie=383541006; zzpaneluin=; zzpanelkey=; qzspeedup=sdch; pt2gguin=o0158124908; uin=o0158124908; skey=@nDvyVTs5R");
			HttpResponse response = httpclient.execute(httpGet);
			//logger.info(response.getStatusLine().toString());
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity httpEntity = response.getEntity();
				content = EntityUtils.toString(httpEntity, Consts.UTF_8);
			
			}	
			
		} catch (Exception e) {
			httpGet.abort();
			logger.info("error:"+e.getMessage());
		}finally{
			//httpclient.getConnectionManager().shutdown();
			httpGet.releaseConnection();
			try {
				Thread.sleep(Constant.crawleSleepTime);
			} catch (InterruptedException e) {
				
			}
		}
		
		return content;
	}
	
	public synchronized static void    sendMultGetRequest(String uris[]){
		
		if(uris==null || uris.length<1){
			return;
		}
		try {
			HttpClient httpClient = new DefaultHttpClient(pcm);
			
			GetThread[] threads = new GetThread[uris.length];
	        for (int i = 0; i < threads.length; i++) {
	            HttpGet httpget = new HttpGet(uris[i]);
	            //设置读数据超时时间(单位毫秒)    	
	            HttpConnectionParams.setConnectionTimeout(httpget.getParams(),30*1000);
	            threads[i] = new GetThread(httpClient, httpget, i + 1);
	        }
	     // start the threads
	        for (int j = 0; j < threads.length; j++) {
	            threads[j].start();
	        }

	        // join the threads
	        for (int j = 0; j < threads.length; j++) {
	            try {
					threads[j].join();
				} catch (InterruptedException e) {
					continue;
				}
	        }
		}finally{
			try {
				Thread.sleep(Constant.crawleSleepTime*getRandNum());
			} catch (InterruptedException e) {
				
			}
		}
		
	}
	
	/**
	 * send post request
	 * @param url
	 * @return
	 */
	public static String sendPostRequest(String url,boolean userProxy,Map<String,String> params){
		if(url==null || url.equals("")){
			return "";
		}
		httpclient = new DefaultHttpClient(pcm);
		if(userProxy){
			MyProxy myProxy = GetAProxy();
			HttpHost proxy = new HttpHost(myProxy.getIP(),myProxy.getPort());
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);
		}
		String content = "";
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			if(params!=null){
				HttpParams HttpParams = new BasicHttpParams();
				for(Map.Entry<String,String> entry : params.entrySet()){
					HttpParams.setParameter(entry.getKey(), entry.getValue());
				}
				httpPost.setParams(HttpParams);
			}
				
			
			
			httpPost.setHeader("REMOTE_ADDR","203.98.182.163");
			//代理服务器 IP
			httpPost.setHeader("HTTP_VIA","");
			httpPost.setHeader("X_FORWARDED_FOR", "");
			
			HttpResponse response = httpclient.execute(httpPost);
			//logger.info(response.getStatusLine().toString());
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				HttpEntity httpEntity = response.getEntity();
				content = EntityUtils.toString(httpEntity, Consts.UTF_8);
			
			}	
			
		} catch (Exception e) {
			httpPost.abort();
			logger.info("error:"+e.getMessage());
		}finally{
			//httpclient.getConnectionManager().shutdown();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			}
		}
		
		return content;
	}
	
	public static int getRandNum(){
		Random random = new Random();
		//return 1-3
		return random.nextInt(3)+1;
	}
}

 class MyProxy{
	private String ip;
	private int port;
	
	public MyProxy(String ip,int port){
		this.ip = ip;
		this.port = port;
	}
	public String getIP(){
		return this.ip;
	}
	
	public int getPort(){
		return this.port;
	}
	
	 /**
     * A thread that performs a GET.
     */
    static class GetThread extends Thread {

        private final HttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpGet;
        private final int id;
        private String gtk;
        private String cookie;
        public GetThread(HttpClient httpClient, HttpGet httpget, int id) {
            this.httpClient = httpClient;
            this.context = new BasicHttpContext();
            this.httpGet = httpget;
            this.id = id;
        }
        
        private void setRequesetHeader(){
        	
        	//httpGet.setHeader("Cookie", "pgv_pvid=1694981883; pgv_info=ssid=s9883248170; pt2gguin=o1401319810; uin=o1401319810; skey=@Kgk2oL1WN; ptisp=ctc; RK=BbxywH2e3Q");
			httpGet.setHeader("Accept","*/*");
			
			//httpGet.setHeader("Accept-Encoding","gzip,deflate,sdch");
			httpGet.setHeader("Accept-Language","zh-CN,zh;q=0.8");
			httpGet.setHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
//			httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
			httpGet.setHeader("Connection", "keep-alive");
			//httpGet.setHeader("Accept-Charset","utf-8,GBK;q=0.7,*;q=0.3");
			
        	//httpGet.setHeader("Referer", "http://ctc.qzs.qq.com/qzone/v6/newlimit/index.html");
			
			//httpGet.setHeader("cookie", "ETK=; ptcz=ef60002e55e898898b8176faf0cbf7b9dcae39a3e35eabf4c2ac6a107e3c7b02; skey=@Ssxav7db0; pt2gguin=o1441927083; uin=o1441927083; ptuserinfo=e69d8ee88ab1e88eb9; RK=DdJ6sKYu2V; ptisp=ctc; ");
			
			//httpGet.setHeader("Cookie", "pgv_pvid=9809962100; pgv_info=ssid=s8154332218; ptisp=ctc; ptui_loginuin=1148025457; pt2gguin=o1148025457; uin=o1148025457; skey=@BHyyQ2abh; RK=hpZGsSPaXY");
			
//			for(Map.Entry<String, String> entry : Constant.currentTokenInfo.entrySet()){
//				cookie = entry.getValue();
//				gtk = entry.getKey();
//				break;
//			}
			
			//http://r.qzone.qq.com/cgi-bin/user/cgi_personal_card?uin=" +
			//count.getAndIncrement()+"&remark=0&fupdate=1&g_tk=
			String requestUrl = httpGet.getURI().toString();
			int flag = requestUrl.lastIndexOf("=");
			gtk = requestUrl.substring(flag+1);
			
			cookie = Constant.tokenCookieInfo.get(gtk);
			httpGet.setHeader("Cookie", cookie);
//			httpGet.setHeader("Cookie", "pgv_pvid=9809962100; pgv_info=ssid=s8154332218; ptisp=ctc; ptui_loginuin=1148025457; pt2gguin=o1148025457; uin=o1148025457; skey=@BHyyQ2abh; RK=hpZGsSPaXY");
			httpGet.setHeader("Host","r.qzone.qq.com");
//			httpGet.setHeader("If-None-Match", "3036166553");	
        	httpGet.setHeader("User-Agent",Constant.getUserAgent());
        }
        /**
         * Executes the GetMethod and prints some status information.
         */
        @Override
        public void run() {
        	
        	setRequesetHeader();
        	int state = 1;
            try {
                // execute the method
                HttpResponse response = httpClient.execute(httpGet, context);
                
                System.out.println(id + " - get executed");
                System.out.println(response.getStatusLine().getReasonPhrase());
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                	 // get the response body as an array of bytes
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                    	String content = EntityUtils.toString(entity, Consts.UTF_8);
                        int startPosition   =  content.indexOf("{");
            			int endPosition = content.lastIndexOf("}");
            			if(startPosition!=-1 && endPosition>1){
            				content = content.substring(startPosition,endPosition+1);
            			}
            			QQZoneCrawler.parseJSONData(content);
                    }
                }
                else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN){
                	System.err.println("forbidden by tencent,please change you ip address!");
                	state = 2;
                }else{
                	System.err.println("crawle tencent' qzone data occus error,please change you ip address!");
                	state = 3;
                	//Thread.currentThread().interrupt(); 在这里会抛异常 Unable to acquire write lock for status
                	//proxool 对数据库连接池管理如果碰到了InterruptException就会直接报错。不会去写数据操作，因为没有获得写的锁
                }
            } catch (Exception e) {
                httpGet.abort();
                System.err.println(id + " - error: " + e);
                try {
					Thread.sleep(5*1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
            }finally{
            	httpGet.releaseConnection();
            	if(state != 1){
            		try {
                		QQ qqObj = DbUtils.queryQQ(gtk);
                    	
                    	for(QQ qq : Constant.qqObjs){
                    		if(qq.getAccessToken()==gtk){
                    			boolean result = Constant.qqObjs.remove(qq);
                    			if(result)
                    				System.out.println(qq.getQq()+" remove success ");
                    			if(qqObj!=null){
                    				qqObj.setState(state);
                        			DbUtils.updateQQ(qqObj);
                    			}
                    			//Constant.reflush();
                    			break;
                    		}
                    	}
        				Thread.sleep(Constant.crawleSleepTime*HttpUtil.getRandNum());
        			} catch (InterruptedException e) {
        				
        			}
            	}
            }
        }

    }
}
