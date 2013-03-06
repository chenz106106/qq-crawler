package com.chen.crawler.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chen.crawler.entity.IpAddres;

public class DialUtils {

	public static Map<String,Object> ipCache = new ConcurrentHashMap<String, Object>();
	
	private static final int RETRY_COUNT = 5;
	public static final AtomicInteger retryCount = new AtomicInteger(RETRY_COUNT);

	private static final int hours = 2;

	
    /**
     * 执行CMD命令,并返回String字符串
     */
    public static String executeCmd(String strCmd) throws Exception {
        Process p = Runtime.getRuntime().exec("cmd /c " + strCmd);
        StringBuilder sbCmd = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(p
                .getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            sbCmd.append(line + "\n");
        }
        return sbCmd.toString();
    }

    public static boolean dial(String adslTitle, String adslName, String adslPass){
    	boolean result = false ;
    	try {
    		cutAdsl(adslTitle);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	try {
    		result = connAdsl(adslTitle,adslName,adslPass);
		} catch (Exception e) {
			e.printStackTrace();
		}
		retryCount.set(RETRY_COUNT);
		return result ;
    	
    }
    /**
     * 连接ADSL
     */
    public static boolean connAdsl(String adslTitle, String adslName, String adslPass) throws Exception {
        System.out.println("正在建立连接.");
        String adslCmd = "rasdial " + adslTitle + " " + adslName + " "+ adslPass;
        String tempCmd = executeCmd(adslCmd);
        // 判断是否连接成功
        boolean result = false;
        System.out.println("------------"+ tempCmd +"-------------");
        while(retryCount.getAndDecrement()>0){
        	if (tempCmd.indexOf("已连接") > 0 || tempCmd.indexOf("已经连接") > 0 || tempCmd.indexOf("completed")>0 || tempCmd.indexOf("Successfully")>0) {
                String ip = IPUtils.getIp();
                if(isValidIP(ip)){
                	//判断ip是否在指定的时间范围内重复
                	if(DbUtils.QueryIpEistes(ip, hours)){
                		//继续拨号
                		connAdsl(adslTitle,adslName,adslPass);
                	}else{
                		//save ip info
                		IpAddres ipAddress  = new IpAddres();
                		ipAddress.setIp(ip);
                		ipAddress.setAddress(IPUtils.getFrom());
                		ipAddress.setType(IPUtils.getProvider());
                		DbUtils.InsertIp(ipAddress);
                		
                		System.out.println("已成功建立连接.");
                    	ipCache.put(ip, IPUtils.getFrom());
                    	result = true;
                    	break;
                	}
                }
            } else {
            	Thread.sleep(20000);
                System.err.println(tempCmd);
                System.err.println("建立连接失败");
                connAdsl(adslTitle, adslName,  adslPass);
            }
        }
        
        return result;
    }

    public static boolean isValidIP(String ip){
    	
    	String ipPattern = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";   
        Pattern pattern = Pattern.compile(ipPattern);   
        Matcher matcher = pattern.matcher(ip);   
        return matcher.matches();  
    }
    /**
     * 断开ADSL
     */
    public static boolean cutAdsl(String adslTitle) throws Exception {
        String cutAdsl = "rasdial " + adslTitle + " /disconnect";
        String result = executeCmd(cutAdsl);
       
        if (result.indexOf("没有连接")!=-1){
            System.err.println(adslTitle + "连接不存在!");
            return false;
        } else {
            System.out.println("连接已断开");
            return true;
        }
    }
   
    public static void main(String[] args) throws Exception {
    	
    	dial("宽带连接","XXX.gd","XXXX");
        //再连，分配一个新的IP
        //connAdsl("dial","XXX","XXX");
        
        for(Map.Entry<String,Object> entry : ipCache.entrySet()){
        	String key = entry.getKey();
        	Object value = entry.getValue();
        	System.out.println("key:"+key+" value:"+value);
        }
//        boolean result = isValidIP("127.223.256.1");
//        System.out.println(result);
    }
}
