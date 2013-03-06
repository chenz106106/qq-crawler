package com.chen.crawler.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 
 * @author chenz
 *
 */
public class FileUtils {

	/**
	 * 文件保存的路径
	 */
	private static String PATH = "c:\\jiaofan";
	/**
	 * 保存一个对象到硬盘中
	 * @param path
	 */
	public static void saveObj2Disk(String filename,Object obj){
		
		if(null==filename || "".equals(filename) || null==obj){
			throw new IllegalArgumentException("saveObj2Disk: need not empty arguments");
		}
		File file = new File(filename);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			try {
				file = File.createTempFile(PATH, "data");
				fos = new FileOutputStream(file);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				oos.close();
			} catch (IOException e) {
				System.err.println("close file occus errors"+e.getMessage());
			}
			
		}
	}
	
	/**
	 * 从一个文件中恢复对象
	 * @param filename
	 */
	public static Object readObjFromDish(String filename){
		if(null==filename || "".equals(filename)){
			throw new IllegalArgumentException("readObjFromDish: need not empty argument");
		}
		File file = new File(filename);
		FileInputStream fos = null;
		try {
			fos = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			try {
				file = File.createTempFile(PATH, "data");
				fos = new FileInputStream(file);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		ObjectInputStream ois = null;
		Object obj = null;
		try {
			ois = new ObjectInputStream(fos);
			try {
				obj = ois.readObject();
			} catch (ClassNotFoundException e) {
				System.err.println("close file occus errors"+e.getMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				ois.close();
			} catch (IOException e) {
				System.err.println("close file occus errors"+e.getMessage());
			}
			
		}
		return obj;
	}
	
	
	
	
}
