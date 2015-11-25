package net.carslink.util;

import java.util.LinkedList;
import java.util.Queue;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;

public class SharedSetting {
    static private SharedPreferences settings = null;
    static private Editor localEditor = null;
    static private boolean isInited = false;
    static public boolean lockBackCamera = false;
    static private boolean lockFrontCamera = false;
    static public boolean hasPushBind = false;

    static public Queue<String> takePictureQueueBack = null;
    static public Queue<String> takePictureQueueFront = null;
    
    static public void initSharedSetting(SharedPreferences sp){
        settings = sp;
        localEditor = settings.edit();
        localEditor.commit();
        
        takePictureQueueBack = new LinkedList<String>();
        takePictureQueueFront = new LinkedList<String>();

        isInited = true;
    }
    
    static public boolean isInited(){
    	return isInited;
    }
	
    static public boolean cameraLocked(){
		boolean result = false;
		Log.i("DEBUG", "lockBackCamera: "+lockBackCamera+" lockFrontCamera: "+lockFrontCamera);
		if(lockBackCamera == true || lockFrontCamera == true)
			result = true;
		return result;
	}
    
    static public void lockFrontCamera(boolean set){
    	Log.i("DEBUG", "lockFrontCamera: "+set);
    	lockFrontCamera = set;
    }
    
    static public boolean isPushBind(){
    	String bindFlag = settings.getString("bind_flag", "not");
    	boolean ret = false;
    	if(bindFlag.equals("ok")) ret = true;
    	return ret;
    }
    
    static public boolean getAutoStart(){
        if(settings == null){
            Log.i("Debug", "settings is null");
        }
        return settings.getBoolean("setting_title_auto_start", false);
    }
    
    static public void setAutoStart(boolean autoStart){
        localEditor.putBoolean("setting_title_auto_start", autoStart);
        localEditor.commit();
    }
    
    static public void setDzwl(boolean enable){
        localEditor.putBoolean("setting_dzwl", enable);
        localEditor.commit();
    }
    
    static public boolean getDzwl(){
        if(settings == null){
            Log.i("Debug", "settings is null");
        }
        return settings.getBoolean("setting_dzwl", false);
    }
    
    static public boolean getAutoBackPicture(){
        if(settings == null){
            Log.i("Debug", "settings is null");
        }
        return settings.getBoolean("setting_auto_take_back_picture", true);
    }
    
    static public boolean getCompareInnerPicture(){
        if(settings == null){
            Log.i("Debug", "settings is null");
        }
        return settings.getBoolean("setting_compare_inner_picture", true);
    }
    
    static public void setAutoBackPicture(boolean autoStart){
        localEditor.putBoolean("setting_auto_take_back_picture", autoStart);
        localEditor.commit();
    }
    
    static public int getAutoFrontPicture(){
        if(settings == null){
            Log.i("Debug", "settings is null");
        }
        return settings.getInt("setting_auto_take_front_picture", -1);
    }
    
    static public void setAutoFrontPicture(int autoStart){
        localEditor.putInt("setting_auto_take_front_picture", autoStart);
        localEditor.commit();
    }
    
    static public int getFenBianLv(){
        return settings.getInt("fenbianlv", 720);
    }
    
    static public void setFenBianLv(int fbl){
        localEditor.putInt("fenbianlv", fbl);
        localEditor.commit();
    }
    
    static public int[] getVideoWidthAndHeight(){
        int fbl = getFenBianLv();
        int ret[] = new int[2];
        switch(fbl){
        case 720:
            ret[0] = 1280;
            ret[1] = 720;
            break;
        case 1080:
        	ret[0] = 1920;
            ret[1] = 1080;
            break;
        }
        return ret;
    }
    
    static private String getSdCardPath(){
        return settings.getString("sd_card_path", Environment
                .getExternalStorageDirectory().getAbsolutePath());
    }
    
    static public int getVideoBitrate(){
         return settings.getInt("video_bitrate", 5000000 * 3);
    }
    
    static public int getVideoDuration(){
         return settings.getInt("video_duration", 300000);
    }
    
    static public void setVideoDuration(int value){
        localEditor.putInt("video_duration", value);
        localEditor.commit();
    }
    
    static public int getTempFolderSize(){
         return settings.getInt("temp_folder_size", 600*1024);
    }
    
    static public void setTempFolderSize(int value){
        localEditor.putInt("temp_folder_size", value);
        localEditor.commit();
    }
    
    static public int getMinFreeSpace(){
         return settings.getInt("min_free_space", 600*1024);
    }
    
    static public void setMinFreeSpace(int value){
        localEditor.putInt("min_free_space", value);
        localEditor.commit();
    }
    
    static public String getDeviceId(){
        return settings.getString("device_id", "13912341230");
    }
    
    static public void setDeviceId(String value){
        localEditor.putString("device_id", value);
        localEditor.commit();
        //Log.i("DEBUG", settings.getString("device_id", "13912341235"));
    }
    
    static public String getVideoTempFolder(){
    	return getSdCardPath() + "/DashCam/临时录像/";
    }
    
    static public String getVideoFavFolder(){
    	return getSdCardPath() + "/DashCam/保留录像/";
    }
    
    static public String getCarfrontPictureFolder(){
    	return getSdCardPath() + "/DashCam/车前照片/";
    }
    
    static public String getInnerPictureFolder(){
    	return getSdCardPath() + "/DashCam/车内照片/";
    }
    
    static public String getAppMainPath(){
    	return getSdCardPath() + "/DashCam";
    }
}
