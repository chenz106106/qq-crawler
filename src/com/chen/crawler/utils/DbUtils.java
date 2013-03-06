package com.chen.crawler.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chen.crawler.entity.IpAddres;
import com.chen.crawler.entity.QQ;
import com.chen.crawler.entity.QQUser;
import com.coola.jutil.j4log.Logger;
import com.coola.jutil.sql.DBEngine;
import com.coola.jutil.sql.DBFactory;

/**
 * 数据工具类
 * @author chenz
 *
 */
public class DbUtils {

	private static final  String USER_DB = "qquser";
	private static final DBEngine userDBEngine ;
	private static Connection conn = null;
	private static Logger logger = Logger.getLogger(DbUtils.class.getName());
	
	static{
		userDBEngine = DBFactory.getDBEngine(USER_DB);
		try {
			conn = DBEngine.getConnection(USER_DB);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		boolean tableExists = TableExists("userinfo_1");
		
		if(!tableExists){
			CreateMargeTable("userinfo");
			for(int i = 0 ; i < 100; i++){
				CreateTables("userinfo_"+i);
			}
		}
			
			
	}
	
	private DbUtils(){
		
	}
	
	private static boolean TableExists(String  tableName){
		
		String sql = "show tables like '"+tableName+"'";
		
		try {
			conn = DBEngine.getConnection(USER_DB);
			java.sql.PreparedStatement pstmt =  conn.prepareStatement(sql);
			ResultSet resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				tableName = resultSet.getString(1);
				if(tableName!=null && tableName.length()>1){
					return true;
				}else{
					return false;
				}
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return false;
		
	}
	/**
	 * 创建主表，注意分表和主表的ENGINE 要分表设置成 MyISAM 和MRG_MyISAM，不可以是InnoDb的引擎。
	 * 分表和主表的结构基本是一致的，除了userId在主表不是主键，变成索引之外，其他的都相同。这里有100个分表，所以
	 * 要union 100个分表 ，同时在建立主表的时候添加了INSERT_METHOD=NO 来保证主表为只读的，即主表只是保存分表
	 * 的结构，不保存数据的。
	 * @param tableName
	 */
	private static void CreateMargeTable(String tableName){
		String sql = "CREATE TABLE `" + tableName + "` ("+
				"`userId` bigint(15) NOT NULL ,"+
				"`qzoneExists` tinyint(2) NOT NULL COMMENT 'qzone是否存在1代表开通，0代表未开通或不存在qq',"+
				"`qzoneOpen` tinyint(2) NOT NULL COMMENT '1:空间为公开的 0 :没有公开',"+
				"`nickname` varchar(50) NOT NULL COMMENT '用户昵称',"+
				"`qq` varchar(15) NOT NULL COMMENT 'QQ号码',"+
				"`gender` tinyint(2) NOT NULL COMMENT '性别 1 ：男 2：女',"+
				"`astro` varchar(5) NOT NULL COMMENT '星座的编码',"+
				"`address` varchar(50) NOT NULL COMMENT '地址',"+
				" KEY  (`userId`),"+
				" KEY `i_nickname` (`nickname`),"+
				" KEY `i_address` (`address`)"+
				" ) ENGINE=MRG_MyISAM  INSERT_METHOD=NO DEFAULT CHARSET=utf8 union=(";
		StringBuffer sb = new StringBuffer();
		sb.append(sql);
		for(int i =0;i<100 ;i++){
			sb.append("userinfo_"+i).append(",");
		}
		sb.deleteCharAt(sb.length()-1).append(");");
			
			try {
				java.sql.PreparedStatement pstmt =  conn.prepareStatement(sb.toString());
				pstmt.execute();
				
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * 创建userinfo_i 分表 i从0-99变化，根据Userinfo对象的hashcode 取模运算生成
	 * @param tableName
	 */
	private static void CreateTables(String tableName){
		
		String sql = "CREATE TABLE `" + tableName + "` ("+
			"`userId` bigint(15) NOT NULL auto_increment,"+
			"`qzoneExists` tinyint(2) NOT NULL COMMENT 'qzone是否存在1代表开通，0代表未开通或不存在qq',"+
			"`qzoneOpen` tinyint(2) NOT NULL COMMENT '1:空间为公开的 0 :没有公开',"+
			"`nickname` varchar(50) NOT NULL COMMENT '用户昵称',"+
			"`qq` varchar(15) NOT NULL COMMENT 'QQ号码',"+
			"`gender` tinyint(2) NOT NULL COMMENT '性别 1 ：男 2：女',"+
			"`astro` varchar(5) NOT NULL COMMENT '星座的编码',"+
			"`address` varchar(50) NOT NULL COMMENT '地址',"+
			" PRIMARY KEY  (`userId`),"+
			" KEY `i_nickname` (`nickname`),"+
			" KEY `i_address` (`address`)"+
			" ) ENGINE=MyISAM DEFAULT CHARSET=utf8;";
		
		try {
			java.sql.PreparedStatement pstmt =  conn.prepareStatement(sql);
			pstmt.execute();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 插入抓取下来的QQ用户的资料
	 * @param user
	 * @param tableName
	 * @return
	 */
	public  static boolean InsertUserinfo(QQUser user,String tableName) {
		boolean result = false;

		if (user == null || tableName==null) {
			return false;
		}
		try {
			String sql = "insert into " + tableName + "(qzoneExists,qzoneOpen,nickname,qq,gender,astro,address) values(?,?,?,?,?,?,?)";
			logger.info(sql);
			result = userDBEngine.executeUpdate(sql, new Object[] { 
					user.isQzoneExists(),user.isQzoneOpen(),user.getNickname(),user.getUin(),
					user.getGender(),user.getAstro(),user.getAddress()
				 }) > 0 ? true : false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 查询数据中是否存在该QQ
	 * @param ip
	 * @param 间隔多少小时
	 * @return
	 */
	public  static boolean QueryIpEistes(String ip,int hours){
		
		String pastTime = getLateHoursTime(new Date(), -12);
		String sql = "select count(*) as num from ipTables where ip=? and ctime >= ? limit 1 " ;
		logger.info(sql);
		boolean result = false ;
		try {
			List<Map<String,Object>> returnResult = userDBEngine.executeQuery(sql,new Object[]{ip,pastTime});
			if(returnResult!=null && returnResult.size()>0){
				int num =  ((Number) returnResult.get(0).get("num")).intValue();
				if(num>0){
					result = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 插入拨号器生成的IpAddress 对象
	 * @param ip
	 * @return
	 */
	public  static boolean InsertIp(IpAddres ip) {
		boolean result = false;

		if (ip == null) {
			return false;
		}
		try {
			String sql = "insert into ipTables(ip,address,ctime,type) values(?,?,NOW(),?)";
			logger.info(sql);
			result = userDBEngine.executeUpdate(sql, new Object[] { 
					ip.getIp(),ip.getAddress(),ip.getType()
				 }) > 0 ? true : false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 查询所有accessCookie为null的QQ对象
	 * @param type 1 accessToken为null  2 accessToken不为空
	 * @return
	 */
	public  static List<QQ>  queryAllQQ(int type) {
		
		List<Map<String,Object>> result = null;
		try {
			String sql = "";
			if(1==type){
				sql = "select * from qq where  accessCookie IS NULL and state=1";
			}else if(2 == type){
				sql = "select * from qq where  accessCookie IS NOT NULL and state=1";
			}
			logger.info(sql);
			result = userDBEngine.executeQuery(sql,new Object[]{});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		List<QQ> qqObjs = null;
		if(result!=null && result.size()>0){
			qqObjs = new ArrayList<QQ>();
			for(Map<String,Object> map: result){
				QQ qq = new QQ();
				qq.setQq((String) map.get("qq"));
				qq.setPwd((String) map.get("pwd"));
				qq.setAccessCookie((String) map.get("accessCookie"));
				qq.setAccessToken((String) map.get("accessToken"));
				qqObjs.add(qq);
			}
		}
		return qqObjs;
	}
	
	/**
	 * 批更新QQ对象信息
	 * @param qqInfos
	 */
	public static void batchUpdateQQ(List<QQ> qqInfos){
		for(QQ qq : qqInfos){
			updateQQ(qq);
		}
	}
	
	/**
	 * 跟新qq对象信息
	 * @param qq
	 */
	public static synchronized void updateQQ(QQ qq){
		try {
			String sql = "update qq set accessToken=?, accessCookie=?,ctime=NOW(),state=? where qq=? and pwd=?";
			logger.info(sql);
			userDBEngine.executeUpdate(sql, new Object[]{
				qq.getAccessToken(),qq.getAccessCookie(),qq.getState(), qq.getQq(),qq.getPwd()
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 根据gtk查询qq信息
	 * @param gtk
	 * @return
	 */
	public static QQ queryQQ(String gtk){
		try {
			String sql = "select * from qq where accessToken=? limit 1";
			logger.info(sql);
			List<Map<String,Object>> qqInfo = userDBEngine.executeQuery(sql, new Object[]{
				gtk
			});
			if(qqInfo!=null && qqInfo.size()>0){
				QQ qq = new QQ();
				qq.setQq((String) qqInfo.get(0).get("qq"));
				qq.setPwd((String) qqInfo.get(0).get("pwd"));
				qq.setAccessCookie((String) qqInfo.get(0).get("accessCookie"));
				qq.setAccessToken((String) qqInfo.get(0).get("accessToken"));
				return qq;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 查询爬虫上次请求的位置信息
	 * @return 
	 */
	public static List<Map<String,Object>> queryCountInfo(){
		List<Map<String,Object>> result = null;
		try {
			String sql = "select * from countInfo limit 1";
			logger.info(sql);
			result = userDBEngine.executeQuery(sql,new Object[]{});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 保存这次爬虫抓取的请求地址的count信息
	 */
	public static boolean saveCountInfo(long currentNum){
		try {
			String sql = "update countInfo set currentNum=?,updateTime=NOW() limit 1";
			logger.info(sql);
			int num = userDBEngine.executeUpdate(sql, new Object[]{
				currentNum
			});
			if(num>0){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 查询所有的userAgent头信息
	 * @return
	 */
	public static List<String> queryAllUserAgent() {
		try {
			String sql = "select * from useragent where 1=? ";
			logger.info(sql);
			List<Map<String,Object>> userAgentInfo = userDBEngine.executeQuery(sql, new Object[]{
				1
			});
			if(userAgentInfo!=null && userAgentInfo.size()>0){
				List<String> result = new ArrayList<String>();
				for(Map<String,Object> map : userAgentInfo){
					result.add((String) map.get("userAgent"));
				}
				return result;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 获取指定距离当前时间范围后的时间
	 * @param datetime
	 * @param hours
	 * @return
	 */
	public static String getLateHoursTime(Date datetime, int hours) {
		Calendar c = new GregorianCalendar();
		c.clear();
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		c.setTime(datetime);
		c.add(Calendar.HOUR, hours);
		return dateTimeFormat.format(c.getTime());
	}
	
	public static void main(String[] args) {
		String time = getLateHoursTime(new Date(), -2);
		System.out.println(time);
	}

}
