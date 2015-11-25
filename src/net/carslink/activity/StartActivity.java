package net.carslink.activity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

public class StartActivity extends Activity {
	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_layout_start);

		checkData();
	}

	/**
	 * 可以检查数据
	 **/
	public void checkData() {
		handler = new Handler();
		handler.postDelayed(new startHandler(), 3000);

		// 检查网络
		/*
		 * ConnectivityManager connectivityManager = (ConnectivityManager)
		 * this.getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo info
		 * = connectivityManager.getActiveNetworkInfo();
		 * 
		 * if(info == null){ Toast.makeText(this, "无网络状态，请检查网络",
		 * Toast.LENGTH_LONG).show(); }
		 */
		
		String version = getVersionName();
		
		// 创建DashCam 文件夹
		File mFolder = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + "DashCam");
		if (!mFolder.exists()) {
			mFolder.mkdirs();
		}
	}

	public String getVersionName() {
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = null;
		String version = "0.0";
		
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(),0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		if(packInfo != null){
			version = packInfo.versionName;
		}
		
		return version;
	}

	/**
	 * 线程
	 **/
	class startHandler implements Runnable {
		@Override
		public void run() {
			Intent main = new Intent(getApplicationContext(),
					MainActivity.class);
			startActivity(main);
			StartActivity.this.finish();
		}
	}

}
