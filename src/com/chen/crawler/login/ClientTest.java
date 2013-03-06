package com.chen.crawler.login;

import java.util.Map.Entry;


public class ClientTest {

	public static void main(String[] args) {
		ClientUtil.init();
		CrawlData crawl  = ClientUtil.getVerfiy("1148025457", "ilrvkz");
		//print(crawl);
		crawl.setQq("1148025457");
		crawl.setPwd("ilrvkz");
		crawl = ClientUtil.getCookie(crawl);
		print(crawl);
//		crawl.setQq("1143942460");
//		crawl = ClientUtil.addWeixin(crawl, "");
//		print(crawl);
	}
	
	public static void print(CrawlData crawl){
		if(crawl.getStatus() == 200){
			System.out.println("----------------------------------------------------------");
			 System.out.println("backBody:  "+crawl.getBackBody());
			 System.out.println("-----------------------------cookie---------------------");
			 if(crawl.getCookies() != null && crawl.getCookies().size() > 0){
				 for(Entry<String, String > e : crawl.getCookies().entrySet()){
					 System.out.println(e.getKey() + "   "+ e.getValue());
				 }
			 }
			 System.out.println("-----------------------------head-------------------------");
			 if(crawl.getHeads() != null && crawl.getHeads().size() > 0){
				 for(Entry<String, String > e : crawl.getHeads().entrySet()){
					 System.out.println(e.getKey() + "   "+ e.getValue());
				 }
			 }
			 
		 }else{
			 System.out.println("网络有问题！");
		 }
	}
}
