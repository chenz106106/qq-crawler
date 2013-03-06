package com.chen.crawler.test;

public class GetGTK {

	public static String getGTK(String str){
		   int hash = 5381;
		   for(int i = 0, len = str.length(); i < len; ++i)
		   {
			   int codeChar = str.charAt(i);
			   int code = (int) codeChar;
			   hash += (hash << 5) + code;
		   }
		   return (hash & 0x7fffffff)+"";
	}
	
	public static void main(String[] args) {
		
		String cookie = "@YnXXZAJH1";
		String gtk = getGTK(cookie);
		System.out.println(gtk);
	}
}
