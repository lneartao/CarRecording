package net.carslink.asynctask;

import java.io.IOException;
import java.util.HashMap;

import net.carslink.util.HttpPostUtil;
import android.os.AsyncTask;

public class UploadLocationTask extends AsyncTask<Integer, Integer, String>{
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";
	HashMap<String,String> locationMsg = null;
	private String serverURL = "http://weixin.carslink.net/camera/in";
	
	public UploadLocationTask(HashMap<String,String> locationMsg){
		this.locationMsg = locationMsg;
	}
	
	public void onPreExecute(){
		
	}
	
	@Override
	protected String doInBackground(Integer... arg0) {
		HttpPostUtil connect;
		
		int state = 404;
		
		try {
			connect = new HttpPostUtil(serverURL);
			connect.addHasMapTextParameter(locationMsg);
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
		}else{
		}
	}

}
