package com.amap.location;

import java.util.HashMap;

import net.carslink.interfaces.GetLocationInterface;
import net.carslink.util.LocationCache;
import net.carslink.util.SharedSetting;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;

public class MyLocationListener implements AMapLocationListener {
	private Context contet;
	private HashMap<String, String> locationMsg = null;
	private LocationManagerProxy mLocationManagerProxy;
	
	public static int AUTO = 15*1000,
			          ONCE = -1;//请求类型那个
	private int requesttype = -1;
	
	public MyLocationListener(Context context) {
		this.contet = context;

		locationMsg = new HashMap<String, String>();
	}

	/**
	 * 初始化高德定位
	 */
	public void initAmapLBS(int requestType) {
		this.requesttype = requestType;
		mLocationManagerProxy = LocationManagerProxy.getInstance(contet);

		// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
		// 注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
		// 在定位结束后，在合适的生命周期调用destroy()方法
		// 其中如果间隔时间为-1，则定位只定一次
		mLocationManagerProxy.requestLocationData(
				LocationProviderProxy.AMapNetwork, requestType, 15, this);

		mLocationManagerProxy.setGpsEnable(true);
	}

	/**
	 * 获取坐标后的回调函数
	 */
	private GetLocationInterface iGetLocation;
	public void getLocationcallback(GetLocationInterface iGetLocation){
		this.iGetLocation = iGetLocation;
	}
	
	@Override
	public void onLocationChanged(Location location) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		locationMsg.clear();
		
		if(locationMsg == null)
			locationMsg = new HashMap<String, String>();

		// 获取位置信息
		if (amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0) {			
			if(amapLocation.getProvider().equals("lbs")){
				locationMsg.put("speed", "0.0");
				locationMsg.put("sat", "0");//高德暂无提供卫星数量获取
			}else{
				locationMsg.put("speed", amapLocation.getSpeed()+ "");
				locationMsg.put("sat", "0");//高德暂无提供卫星数量获取
			}
			
			long time = System.currentTimeMillis()/1000;
			
			locationMsg.put("time", time + "");//时间
			locationMsg.put("device", SharedSetting.getDeviceId());//设备号
			locationMsg.put("isStart", LocationCache.isStart);//0、停车; 1、开车; 2、开车中;
			locationMsg.put("longitude", amapLocation.getLongitude() + "");//经度
			locationMsg.put("latitude", amapLocation.getLatitude() + "");//纬度
			locationMsg.put("radius", amapLocation.getAccuracy() + "");//获取定位精度，单位：m
		}
		
		if(requesttype == MyLocationListener.AUTO){
			LocationCache.writeLocationCache(locationMsg);
		}else{
			iGetLocation.getLocation(locationMsg);
		}
	}
	
    /**
	 * 销毁高德定位 停止录像时调用
	 */
	public void stopAmapLocation() {
		if (mLocationManagerProxy != null) {
			mLocationManagerProxy.removeUpdates((AMapLocationListener) this);
			mLocationManagerProxy.destroy();
		}
		mLocationManagerProxy = null;
	}
}
