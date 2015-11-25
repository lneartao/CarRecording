package net.carslink.asynctask;

import java.io.IOException;
import net.carslink.activity.R;
import net.carslink.util.HttpPostUtil;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class GetLoginCodeTask extends AsyncTask<Integer, Integer, String>{
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";
	Context context;
	String phoneNumber;
	private String serverURL = "";
	
	public GetLoginCodeTask(Context context, String phoneNumber){
		this.context = context;
		this.phoneNumber = phoneNumber;
		
		serverURL = context.getResources().getString(R.string.UrlGetQRCode);
	}
	
	public void onPreExecute(){
		
	}
	
	@Override
	protected String doInBackground(Integer... arg0) {
		HttpPostUtil connect;
		
		int state = 404;
		
		try {
			connect = new HttpPostUtil(serverURL);
			connect.addTextParameter("phoneNumber", phoneNumber);
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
			Toast.makeText(context, "成功发送到手机，请注意查收", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(context, "获取验证码失败，请检查网络", Toast.LENGTH_SHORT).show();
		}
	}

}
