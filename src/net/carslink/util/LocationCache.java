/*
 * 类说明：坐标缓存区
 * 时间：2015/03/17 
 */

package net.carslink.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.carslink.asynctask.UploadLocationTask;

public class LocationCache {
	public static List<HashMap<String, String>> locationCache = null;
	public static UploadLocationTask ult;
	public static String isStart = "1";//0、停车;1、开车;
	
	private static long start = System.currentTimeMillis()/1000;
	
	public static void writeLocationCache(HashMap<String, String> locationMsg){
		if(locationCache == null){
			locationCache = new ArrayList<HashMap<String, String>>();
			sendLocationMsg(locationMsg);//第一次立即上传所获取的坐标，此后每十五秒上传一次
		}
		
		int length = locationCache.size();
		
		if(length >= 30){
			locationCache.remove(0);
		}
		
		long end = System.currentTimeMillis()/1000;
		
		if((end-start) >= 15 ){
			sendLocationMsg(locationMsg);
			start = end;
		}
		locationCache.add(locationMsg);
	}
	
	public static void clearAllCache(){
		if(locationCache != null){
			locationCache.removeAll(locationCache);
			locationCache = null;
		}
		
		if (ult != null && !ult.isCancelled()) {
			ult.cancel(true); // 如果Task还在运行，则先取消它
			ult = null;
		}
	}
	
	public static void sendLocationMsg(HashMap<String, String> locationMsg){
		ult = new UploadLocationTask(locationMsg);
		ult.execute();
	}
}
