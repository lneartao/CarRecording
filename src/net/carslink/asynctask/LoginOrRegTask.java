package net.carslink.asynctask;

import java.io.IOException;
import java.util.HashMap;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.push.custom.Utils;

import net.carslink.activity.R;
import net.carslink.util.HttpPostUtil;
import net.carslink.util.SharedSetting;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

public class LoginOrRegTask extends AsyncTask<Integer, Integer, String>{
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";
	Context context;
	HashMap<String, String> sendMsg = new HashMap<String, String>();
	private String serverURL = "";
	
	public LoginOrRegTask(Context context, HashMap<String, String> sendMsg){
		this.context = context;
		
		this.sendMsg = sendMsg;
		serverURL = context.getResources().getString(R.string.UrlRegOrlogin);
	}
	
	public void onPreExecute(){
		
	}
	
	@Override
	protected String doInBackground(Integer... arg0) {
		HttpPostUtil connect;
		
		int state = 404;
		
		try {
			connect = new HttpPostUtil(serverURL);
			connect.addHasMapTextParameter(sendMsg);
			state = connect.send().getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(state == 200){
			return SUCCESS;
		}else{
			return FAIL;
		}
	}
	
	public void onPostExecute(String result){
		if(result.equals(SUCCESS)){
			//Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
			 SharedSetting.setDeviceId(sendMsg.get("phoneNumber"));
             ConnectivityManager connectivityManager = 
                     (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
             NetworkInfo info = connectivityManager.getActiveNetworkInfo();
             
             if(info != null && info.isAvailable()) {
                 PushManager.startWork(context,
                     PushConstants.LOGIN_TYPE_API_KEY,
                     Utils.getMetaValue(context, "api_key"));
             }
             
             ((Activity) context).finish();
		}else{
			Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show();
		}
	}

}
