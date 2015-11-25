package net.carslink.asynctask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.carslink.takepicture.TakePictureType;
import net.carslink.util.HttpPostUtil;
import net.carslink.util.SharedSetting;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class UploadImgTask extends AsyncTask<Integer, Integer, String>{
	private Context context = null;
	private String wxUser = "";
	private String lastUser = "";
	String _fileName = "";
	
	private final String SUCCESS = "1";
	private final String FAIL = "0";
	private HashMap<String,String> sendMsg = null;
	private String serverURL = "http://weixin.carslink.net/";
	
	public UploadImgTask(Context context){
		this.context = context;
		
		sendMsg = new HashMap<String, String>();
	}
	
	public void setUser(String _user){
		if(_user != null) lastUser = wxUser;
		wxUser = _user;
	}
	
	public void setImgFile(String _fileName){
		this._fileName = _fileName;
	}
	
	public String getUser(){
		return wxUser;
	}
	
	public void setSendLocation(HashMap<String, String> locationMsg){
		serverURL += "camera";
		sendMsg.put("wxuser", wxUser);
		sendMsg.put("Latitude", locationMsg.get("latitude"));
		sendMsg.put("Longitude", locationMsg.get("longitude"));
	}
	
	public void sendMsgToServer(TakePictureType msgType, String _user){
    	Log.i("PUSH_DEBUG", "----===" + msgType.name());
    	if(_user == null) _user = lastUser;
    	serverURL+="wxio/sendFailureMsg";
    	sendMsg.put("id", _user);
    	sendMsg.put("device", SharedSetting.getDeviceId());
        
        String text = "";
        switch(msgType){
			case Err_Cam_Occupied:
				text = "摄像头被暂时占用, 请重试.";
				break;
			case Err_Cfg_Fbd:
				text = "配置禁止远程拍照.";
				break;
			case Err_Recording:
				text = "摄像头录影中, 请稍后重试.";
				break;
			case Alarm_Security:
				text = "车内可能有人进入,请留意.";
				break;
			case No_GPS:
				text = "无位置信息,设置不成功.";
				break;
			case Trigger_dzwl:
				text = "汽车位置超出电子围栏设置.";
				break;
            case OnNavigation:
                text = "设备正在导航中,请稍后重试.";
                break;
            case OnCalculated:
                text = "路线规划成功，请在设备上点击开始导航.";
                break;
			case Loop:
				break;
			case None:
				break;
			case OnDemand:
				break;
			default:
				break;
        }
        
        sendMsg.put("text", text);
    }
	
	@Override
	protected String doInBackground(Integer... i) {
		HttpPostUtil connect;
		
		int state = 404;
		
		try {
			connect = new HttpPostUtil(serverURL);
			connect.addHasMapTextParameter(sendMsg);
			if( !_fileName.equals("") ){
			
				String rootPath = context.getFilesDir().getPath();
				File file = new File(rootPath+File.separator+_fileName);
				connect.addFileParameter(_fileName, file);
			}
			
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
			
		}else if(result.equals(FAIL)){
			
		}
	}
	
}
