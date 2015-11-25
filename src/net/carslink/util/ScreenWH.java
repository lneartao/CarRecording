package net.carslink.util;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenWH {
	Context context;
	
	public ScreenWH(Context context){
		this.context = context;
		getStatusBarHeight();
		getScreenResolution();
	}
	
	/**
	 * 获取状态栏高度
	 */
	int barHeight;
    public void getStatusBarHeight(){
    	Class<?> c = null;
    	Object obj = null;
    	Field field = null;
    	int x = 0;
    	try {
    	    c = Class.forName("com.android.internal.R$dimen");
    	    obj = c.newInstance();
    	    field = c.getField("status_bar_height");
    	    x = Integer.parseInt(field.get(obj).toString());
    	    barHeight = context.getResources().getDimensionPixelSize(x);
    	} catch(Exception e1) {
    	    e1.printStackTrace();
    	}
    }
	
	/**
	 * 获取屏幕分辨率；
	 * 单位: px;
	 * 因为获取的是屏幕分辨率，横屏的时候，宽高相反
	 */
	public static int width = 0;
	public static int height = 0;
	public void getScreenResolution(){
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels - barHeight;
	}
}
