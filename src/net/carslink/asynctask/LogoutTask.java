package net.carslink.asynctask;

import java.io.IOException;

import net.carslink.activity.LoginRegActivity;
import net.carslink.activity.R;
import net.carslink.util.HttpPostUtil;
import net.carslink.util.SharedSetting;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

public class LogoutTask extends AsyncTask<Integer, Integer, String>{
	private static final int SUCCESS = 0;
	private static final int FAIL = 1;
	
	Context context;
	String deviceId;
	private String serverURL = "";
	
	public LogoutTask(Context context, String deviceId){
		this.context = context;
		this.deviceId = deviceId;
		
		serverURL = context.getResources().getString(R.string.UrlLogout);
	}
	
	public void onPreExecute(){
		
	}
	
	@Override
	protected String doInBackground(Integer... arg0) {
		HttpPostUtil connect;
		
		int state = 404;
		
		try {
			connect = new HttpPostUtil(serverURL);
			connect.addTextParameter("phoneNumber", deviceId);
			state = connect.send().getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(state == 200){
			return "SUCCESS";
		}else{
			return "FAIL";
		}
	}
	
	public void onPostExecute(String result){
		if(result.equals("SUCCESS")){
			SharedSetting.setDeviceId("13912341230");
			Intent loginIntent = new Intent(context, LoginRegActivity.class);
			context.startActivity(loginIntent);
		}else{
			Toast.makeText(context, "解绑失败，请检查网络", Toast.LENGTH_LONG).show();
		}
	}

}
