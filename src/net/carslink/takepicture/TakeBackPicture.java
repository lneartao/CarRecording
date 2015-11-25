package net.carslink.takepicture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.carslink.asynctask.UploadImgTask;
import net.carslink.util.SharedSetting;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

public class TakeBackPicture {
    private Context context = null;
    private TakePictureType takePictureType = TakePictureType.None;
    private int tryTimes = 0;
    private Camera cameraBack = null;
    private UploadImgTask uploadImgTask = null;
    
    public TakeBackPicture(Context context){
    	this.context = context;
    }
    
    public void shoot(TakePictureType _type, String _user ,HashMap<String, String> locationMsg){
    	if(SharedSetting.cameraLocked() == true){
    		if(_user == null) _user = "null";
    		SharedSetting.takePictureQueueBack.offer(_user);
    		return;
    	}
    	SharedSetting.lockBackCamera = true;
    	shoot(_type);
 
    	//为空则说明自动拍摄照片
    	if(_user != null){
        	uploadImgTask.setUser(_user);
    	    uploadImgTask.setSendLocation(locationMsg);
    	}
    	//Log.i("PUSH_DEBUG", "tackPicture for user: "+uploadImgTask.getUser());
    }
	
    private void shoot(TakePictureType type){
		/*
		if(takePictureType != TakePictureType.None && type == TakePictureType.OnDemand){
			DataToServer.sendMsgToServer("Camera has been occupied. 0");
			return;
		}else if(takePictureType != TakePictureType.None) return;
		*/
		takePictureType = type;
		tryTimes = 0;
		cameraBack = openFacingBackCamera();
    	uploadImgTask = new UploadImgTask(context);
		
        if (cameraBack != null) {
        	cameraBack.startPreview();
        	
        	cameraBack.autoFocus(new AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					// success为true表示对焦成功
					if (success) {
						camera.takePicture(null, null, mBackPicture);
					}else{
						camera.takePicture(null, null, mBackPicture);
					}
				}
			});
        	cameraBack.setErrorCallback(new ErrorCallback(){
				@Override
				public void onError(int err, Camera camera) {
					Log.i("debug", "camera onError");
					if(tryTimes < 3) {
						tryTimes ++;
						shoot(takePictureType);
					}else{
						if(takePictureType == TakePictureType.OnDemand){
							uploadImgTask.sendMsgToServer(TakePictureType.Err_Cam_Occupied, uploadImgTask.getUser());
							uploadImgTask.execute();
							Log.i("PUSH_DEBUG", "sendMsgToServer for user: "+uploadImgTask.getUser());
							takePictureType = TakePictureType.None;
							return;
						}
					}
				}
			});
		}
	}
	
/*	private ShutterCallback mShutter = new ShutterCallback() {  
        @Override  
        public void onShutter() {
        }
    };*/
    
	private Camera openFacingBackCamera() {
		//Toast.makeText(getApplicationContext(), "开始占用摄像头", Toast.LENGTH_LONG).show();
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
			//if (cameraInfo.facing == 1) {
				try {
					cam = Camera.open(camIdx);
					// return cam;   // function ends here
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(cam == null){
			
		}

		int picWidth = 1024,
			picHeight = 768;
		
		SurfaceView dummy = new SurfaceView(context);
		
		/*
		FrameLayout frameLayout = new FrameLayout(context);
		frameLayout.addView(dummy);*/
		try {
			cam.setPreviewDisplay(dummy.getHolder());
			
			Camera.Parameters params = cam.getParameters();  
	        // JPEG质量设置到最好  
	        //params.setJpegQuality(80);
	        
	        //最大兼容，获取系统所支持的分辨率
	        List<Size> picSize = params.getSupportedPictureSizes();
	        if (picSize.size() > 1) {  
			    Iterator<Camera.Size> itor = picSize.iterator();  
			    while (itor.hasNext()) {  
			         Camera.Size cur = itor.next();  
			         if (cur.width <= picWidth && cur.height <= picHeight) {  
			        	 picWidth = cur.width;  
			        	 picHeight = cur.height;  
			             break;
			         }  
			     }  
			} 
	        
	        // 散光灯模式设置为自动调节  
	        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
	        //params.setPictureSize(480, 320);
	        params.setPictureSize(picWidth, picHeight);
	        
	        cam.setParameters(params);
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return cam;
	}
	
	private void releaseBackCamera(){
		cameraBack.stopPreview();
		cameraBack.release();
		cameraBack=null;
		SharedSetting.lockBackCamera = false;
		context.sendBroadcast(new Intent("net.carslink.dashcam.ReleaseBackCamera"));
	}
	
	// PictureCallback回调函数实现  
    private PictureCallback mBackPicture = new PictureCallback() {
        @Override  
        public void onPictureTaken(byte[] data, Camera camera) {
        	try {
            	//Toast.makeText(context, "照片获取结束", Toast.LENGTH_LONG).show();
            	Log.i("PUSH_DEBUG", "照片获取结束 data.length:"+data.length);
            	Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
    					data.length);
            	
            	switch(takePictureType){
					case Loop:
						Long date = new Date().getTime();
						String folder = DateFormat.format("yyyy-MM-dd", date).toString();
						String ts = DateFormat.format("kk-mm-ss", date).toString();
						String loopPhotoFileName = SharedSetting.getCarfrontPictureFolder();
						loopPhotoFileName += folder;
						
						File isFolder = new File(loopPhotoFileName);
						if(!isFolder.isDirectory()) {
							isFolder.mkdirs();
							Log.i("PUSH_DEBUG", "mkdir "+loopPhotoFileName);
						}else{
							Log.i("PUSH_DEBUG", "folder exists");
						}
						
						
						loopPhotoFileName += "/"+ts+".jpg";
						
						Log.i("PUSH_DEBUG", loopPhotoFileName);
						FileOutputStream fop = new FileOutputStream(loopPhotoFileName);
						bitmap.compress(CompressFormat.JPEG, 80, fop);
						fop.close();
						break;
						
					case None:
						break;
						
					case OnVideo:
		        		iRestartRecorder.heyRestart();
		        		String fileName = SharedSetting.getDeviceId() + "_back.jpg";
	            	    //photoFileName = photoFileName.replaceAll(":", "_");
	            	
	            	    Log.i("PUSH_DEBUG", "before upload new");
	            	
	            	    OutputStream outputStream = context.openFileOutput(fileName, Activity.MODE_PRIVATE);
	    			    bitmap.compress(CompressFormat.JPEG, 80, outputStream);
	    			    outputStream.flush();
	    			    outputStream.close();
	    			    uploadImgTask.setImgFile(fileName);
	    			    uploadImgTask.execute();
		        		break;
		        		
					case OnDemand:
	            	    String photoFileName = SharedSetting.getDeviceId() + "_back.jpg";
	            	    //photoFileName = photoFileName.replaceAll(":", "_");
	            	
	            	    Log.i("PUSH_DEBUG", "before upload new");
	            	
	            	    OutputStream os = context.openFileOutput(photoFileName, Activity.MODE_PRIVATE);
	    			    bitmap.compress(CompressFormat.JPEG, 80, os);
	    			    os.flush();
	    			    os.close();
	    			    uploadImgTask.setImgFile(photoFileName);
	    			    uploadImgTask.execute();
						break;
						
					default:
						break;
            	}
        		takePictureType = TakePictureType.None;
        		releaseBackCamera();
            } catch (Exception error) {
            	error.printStackTrace();
                Toast.makeText(context, "Image could not be saved.",
                        Toast.LENGTH_LONG).show();
                Log.i("PUSH_DEBUG", error.toString());
                releaseBackCamera();
            }
        }
    };
    
    public interface RestartRecorder{
    	public void heyRestart();
    }
    
    RestartRecorder iRestartRecorder;
    public void setRetartCallback(RestartRecorder iRestartRecorder){
    	this.iRestartRecorder = iRestartRecorder;
    }
}
