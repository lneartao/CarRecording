package net.carslink.service;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.amap.location.MyLocationListener;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.push.custom.Utils;

import net.carslink.activity.MainActivity;
import net.carslink.asynctask.UploadImgTask;
import net.carslink.enums.DashCamStat;
import net.carslink.interfaces.ButtonChangeInterface;
import net.carslink.interfaces.GetLocationInterface;
import net.carslink.takepicture.TakeBackPicture;
import net.carslink.takepicture.TakeBackPicture.RestartRecorder;
import net.carslink.takepicture.TakePictureType;
import net.carslink.util.LocationCache;
import net.carslink.util.ScreenWH;
import net.carslink.util.SharedSetting;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

public class BackgroundService extends Service implements
		MediaRecorder.OnInfoListener, SurfaceHolder.Callback {
	Context context;
	private FrameLayout right_videoview_layout_priview;

	private final IBinder mBinder = new LocalBinder();
	private UploadImgTask uploadImgTask;
	private TakeBackPicture takeBackPicture = null;
	private WindowManager windowManager = null;
	private PowerManager.WakeLock mWakeLock = null;
	private MediaRecorder mediaRecorder = null;
	private SurfaceHolder mSurfaceHolder = null;
	private SurfaceView mSurfaceView = null;
	private Camera camera = null;
	private UpdateReceiver broardCastReceiver = null;
	private MyLocationListener amapListener = null;

	private boolean AUTOSTART = false, AUTOBACKPICTURE = false;
	private boolean lastChargingStatus = false;
	private boolean startRecordingInQueue = false;
	private boolean isrecording = false;
	private int isfavorite = 0; // 0=不保存 1=永久保留 2=临时保存

	private int VIDEO_WIDTH = 1280;// 1920;
	private int VIDEO_HEIGHT = 720;// 1080;
	private int MAX_VIDEO_BIT_RATE = 5 * 1024 * 1024;
	private String VIDEO_FILE_EXT = ".mp4";
	private int MAX_VIDEO_DURATION = 5 * 60 * 1000;// 5分钟
	private long MAX_TEMP_FOLDER_SIZE = 10000000;
	private long MIN_FREE_SPACE = 100*1024;

	private String fileName = null;
	private String recordStartBy = "self";
	private String recordStopBy = "self";

	public static DashCamStat STAT = DashCamStat.MINI_STOP;

	public void setVideoPlayLayout(FrameLayout right_videoview_layout_priview) {
		this.right_videoview_layout_priview = right_videoview_layout_priview;
		
		initPlayVideoView();
	}

	public void setContext() {
		this.context = getApplicationContext();
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		super.onCreate();
		setContext();

		if (!SharedSetting.isInited())
			SharedSetting.initSharedSetting(getSharedPreferences("SettingXML",
					0));

		AUTOSTART = SharedSetting.getAutoStart();

		Intent myIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				myIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		/*Notification notification = new Notification.Builder(this)
				.setContentTitle("录像服务已启动").setContentText("")
				.setSmallIcon(R.drawable.miwalk_ico)
				.setContentIntent(pendingIntent).build();
		startForeground(1, notification);*/

		takeBackPicture = new TakeBackPicture(context);
		windowManager = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"DashCamWakeLock");

		// 注册广播
		broardCastReceiver = new UpdateReceiver();
		IntentFilter broardCastFilter = new IntentFilter();
		broardCastFilter.addAction(Intent.ACTION_ALL_APPS);
		broardCastFilter.addAction("com.baidu.push.TakePicture");
		broardCastFilter.addAction("com.baidu.push.InnerCar");
		broardCastFilter.addAction("com.baidu.push.Setting");
		broardCastFilter.addAction("net.carslink.dashcam.ReleaseBackCamera");
		broardCastFilter.addAction("net.carslink.dashcam.ReleaseFrontCamera");
		broardCastFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		broardCastFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		broardCastFilter.setPriority(Integer.MAX_VALUE);
		this.registerReceiver(broardCastReceiver, broardCastFilter);

		//handlerMinute.postDelayed(runnableMinute, 60 * 1000);
		handlerMinute.postDelayed(runnableMinute, 60*1000);
		//connectBaiduPushServer();
	}

	@Override
	public void onDestroy() {
		this.unregisterReceiver(broardCastReceiver);
		/*Intent intent = new Intent(context, BackgroundService.class);
		context.startService(intent);*/
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public BackgroundService getService() {
			return BackgroundService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	Handler handlerMinute = new Handler();
	Runnable runnableMinute = new Runnable() {
		@Override
		public void run() {
			Log.i("DEBUG", "1 minute run()");
			AUTOBACKPICTURE = SharedSetting.getAutoBackPicture();
			if (AUTOBACKPICTURE == true) {
				if (!isRecording())
					takeBackPicture.shoot(TakePictureType.Loop, null, null);
			}

			handlerMinute.postDelayed(this, 60*1000);

			if (SharedSetting.hasPushBind == false) {
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					String name = info.getTypeName();
					Log.d("DEBUG", "当前网络名称：" + name);
					PushManager.startWork(getApplicationContext(),
							PushConstants.LOGIN_TYPE_API_KEY, Utils
									.getMetaValue(getApplicationContext(),
											"api_key"));
				} else {
					Log.d("DEBUG", "没有可用网络");
				}
			}
		}
	};

	/*
	 * 判断是否已经开始录像
	 */
	public boolean isRecording() {
		return isrecording;
	}

	public int isFavorite() {
		return isfavorite;
	}

	public void setIsfavorite(int isfavorite) {
		this.isfavorite = isfavorite;
	}

	public void toggleFavorite() {
		isfavorite = (isfavorite + 1) % 3;
	}

	public void initPlayVideoView() {
		mSurfaceView = new SurfaceView(this);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);

		right_videoview_layout_priview.addView(mSurfaceView);
		hideSurfaceView();
	}

	public void hideSurfaceView() {
		ViewGroup.LayoutParams params = mSurfaceView.getLayoutParams();
		params.height = 1;
		params.width = 1;
		mSurfaceView.setLayoutParams(params);
	}

	public void showSurfaceView() {
		ViewGroup.LayoutParams params = mSurfaceView.getLayoutParams();
		params.height = ScreenWH.height;
		params.width = ScreenWH.width;
		mSurfaceView.setLayoutParams(params);
	}

	/*
	 * 开始录像
	 */
	public void StartRecording() {
		StartRecording("self");
	}

	public void StartRecording(String by) {
		freeSpace();
		
		if (SharedSetting.cameraLocked() == true) {
			startRecordingInQueue = true;
			return;
		}
		
		if(!by.equals("ScreenShot")){
			showSurfaceView();
		}
		
		if (amapListener == null) {
			LocationCache.isStart = "1";
			amapListener = new MyLocationListener(context);
			amapListener.initAmapLBS(MyLocationListener.AUTO);
		}

		SharedSetting.lockBackCamera = true;
		startRecordingInQueue = false;

		MAX_VIDEO_BIT_RATE = SharedSetting.getVideoBitrate();
		VIDEO_WIDTH = SharedSetting.getVideoWidthAndHeight()[0];
		VIDEO_HEIGHT = SharedSetting.getVideoWidthAndHeight()[1];
		MAX_VIDEO_DURATION = SharedSetting.getVideoDuration();
		MAX_TEMP_FOLDER_SIZE = SharedSetting.getTempFolderSize();
		MIN_FREE_SPACE = SharedSetting.getMinFreeSpace();

		// create temp and fav folders
		Long date = new Date().getTime();
		String folder = DateFormat.format("yyyy-MM-dd", date).toString();
		String tempPath = SharedSetting.getVideoTempFolder()+folder+File.separator;
		String favPath = SharedSetting.getVideoFavFolder() + folder + File.separator;
		
		File mFolder = new File(tempPath);
		if (!mFolder.exists()) {
			mFolder.mkdirs();
		}
		mFolder = new File(favPath);
		if (!mFolder.exists()) {
			mFolder.mkdirs();
		}

		OpenUnlockPrepareStart();

		recordStartBy = by;
	}

	/**
	 * 单位：Mb
	 */
	public void freeSpace() {
		
		File dir = new File(SharedSetting.getVideoTempFolder());
		

		 /*long storageFreeSpace = dir.getFreeSpace() / 1024;
		long minFreeSpace = SharedSetting.getMinFreeSpace();
		String msg = "当前设备内存空余不足\n"
				+ "当前设备剩余内存："+storageFreeSpace + "\n"
				+ "您所设置的最大所需空余内存：" + minFreeSpace;
		
		if(storageFreeSpace<minFreeSpace){
			new AlertDialog.Builder(getApplicationContext())
			.setTitle("内存信息提醒")
			.setMessage(msg)
			.setPositiveButton("确定",null)
			.create()
			.show();
			
			return;
		}*/
		
		File[] filelist = dir.listFiles();
		Arrays.sort(filelist, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f2.lastModified()).compareTo(
						f1.lastModified());
			}
		});
		long totalSize = 0; // in kB
		int i;
		for (i = 0; i < filelist.length; i++) {
			totalSize += getFolderSize(filelist[i])/ 1024;
		}
		
		i = filelist.length - 1;
		long tempFreeSpace = dir.getFreeSpace() / 1024;

		while (i > 0 && (totalSize > this.MAX_TEMP_FOLDER_SIZE || tempFreeSpace < this.MIN_FREE_SPACE)) {
			totalSize -= filelist[i].length()  / 1024;
			filelist[i].delete();
		}
	}

	/**
	 * 获取文件夹大小
	 * 
	 * @param file
	 *            File实例
	 * @return long
	 */
	public long getFolderSize(File file) {
		long size = 0;
		
		try {
			File[] fileList = file.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isDirectory()) {
					size = size + getFolderSize(fileList[i]);

				} else {
					size = size + fileList[i].length();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return size;
	}
	
	@SuppressLint("NewApi")
	private void OpenUnlockPrepareStart() {
		if (!isrecording) {
		int previewWidth = ScreenWH.width;  
	    int previewHeight = ScreenWH.height;
			
			mWakeLock.acquire();
			
			try {
				camera = Camera.open();
				Parameters params = camera.getParameters();
				
				/*params.setPreviewFpsRange(24000, 29000);
				params.setRecordingHint(true);*/
				
				List<Camera.Size> sizeVideo = params.getSupportedVideoSizes();  
				//List<Camera.Size> sizePreview = params.getSupportedPreviewSizes();  
				/*Camera.Size cameraSize = params.getPreviewSize();
				previewWidth = cameraSize.height;
				previewHeight = cameraSize.width;*/
				
				if (sizeVideo.size() > 1) {  
				    Iterator<Camera.Size> itor = sizeVideo.iterator();  
				    while (itor.hasNext()) {  
				         Camera.Size cur = itor.next();  
				         if (cur.width >= previewWidth && cur.height >= previewHeight) {  
				        	 previewWidth = cur.width;  
				        	 previewHeight = cur.height;  
				             break;
				         }  
				     }  
				}
				
				params.setPreviewSize(previewWidth,previewHeight);
				
				Calendar c = Calendar.getInstance();
				int hour = c.get(Calendar.HOUR_OF_DAY);
				if ((hour > 18 || hour < 7)
						&& params.isAutoExposureLockSupported()) {
					params.setAutoExposureLock(true);
				}
				camera.setParameters(params);
				
				mediaRecorder = new MediaRecorder();
				camera.unlock();

				mediaRecorder.setCamera(camera);

				// Step 2: Set sources

				mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
				mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				mediaRecorder.setAudioEncoder(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH).audioCodec);// MediaRecorder.AudioEncoder.HE_AAC
				mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
				// mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

				mediaRecorder.setVideoEncodingBitRate(this.MAX_VIDEO_BIT_RATE);
				//mediaRecorder.setVideoSize(this.VIDEO_WIDTH, this.VIDEO_HEIGHT);// 640x480,800x480
				mediaRecorder.setVideoSize(previewWidth, previewHeight);//为了最大兼容，必须使用系统支持的size
				
				Log.i("PUSH_DEBUG", "----> setVideoSize(" + VIDEO_WIDTH + ", "+ VIDEO_HEIGHT + ")");

				mediaRecorder.setVideoFrameRate(29);

				fileName = DateFormat.format("yyyy-MM-dd_kk-mm-ss",
						new Date().getTime()).toString();

				Long date = new Date().getTime();
				String folder = DateFormat.format("yyyy-MM-dd", date).toString();
				String tempPath = SharedSetting.getVideoTempFolder()+folder+File.separator;
				mediaRecorder.setOutputFile(tempPath + fileName + VIDEO_FILE_EXT);

				mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());// 标识

				mediaRecorder.setMaxDuration(this.MAX_VIDEO_DURATION);
				mediaRecorder.setMaxFileSize(0); // 0 - no limit
				mediaRecorder.setOnInfoListener(this);

				mediaRecorder.prepare();
				mediaRecorder.start();
				isrecording = true;
			} catch (Exception e) {
				e.printStackTrace();
				isrecording = true;
				ResetReleaseLock();
			}
		}
	}

	/*
	 * 结束录像
	 */
	public void StopRecording() {
		StopRecording("self");
	}

	public void StopRecording(String by) {
		startRecordingInQueue = false;
		if (isrecording) {
			Stop();
			ResetReleaseLock();
			
			if (amapListener != null) {
				amapListener.stopAmapLocation();
				amapListener = null;
			}
			
			if(!by.equals("ScreenShot")){
				hideSurfaceView();
				
				LocationCache.isStart = "0";
				MyLocationListener myLocationListener = new MyLocationListener(context);
				myLocationListener.initAmapLBS(MyLocationListener.ONCE);
				myLocationListener.stopAmapLocation();
			}

			if (fileName != null && isfavorite != 0) {
				Long date = new Date().getTime();
				String folder = DateFormat.format("yyyy-MM-dd", date).toString();
				String tempPath = SharedSetting.getVideoTempFolder()+folder+File.separator;
				String favPath = SharedSetting.getVideoFavFolder() + folder + File.separator;
				
				File tmpfile = new File(tempPath + fileName + VIDEO_FILE_EXT);
				File favfile = new File(favPath + fileName + VIDEO_FILE_EXT);
				tmpfile.renameTo(favfile);
				if (isfavorite == 2) {
					isfavorite = 0;
					Intent intent = new Intent();
					intent.setAction("com.example.DashCam.updateinterface");
					sendBroadcast(intent);
				}
			}
			isrecording = false;
			recordStopBy = by;
		}
	}

	private void Stop() {
		if (isrecording) {
			mediaRecorder.stop();
		}
	}

	/*
	 * 释放设备
	 */
	private void ResetReleaseLock() {
		if (isrecording) {
			mediaRecorder.reset();
			mediaRecorder.release();

			camera.lock();
			camera.release();
			mWakeLock.release();

			SharedSetting.lockBackCamera = false;

			sendBroadcast(new Intent("net.carslink.dashcam.ReleaseBackCamera"));
		}
	}

	public void RestartRecording() {
		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		manager.setStreamSolo(AudioManager.STREAM_SYSTEM, true);
		manager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
		StopRecording();
		StartRecording();
		manager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
		manager.setStreamSolo(AudioManager.STREAM_SYSTEM, false);
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
			this.RestartRecording();
		}
	}

	/************************************/

	/*
	 * 暂不知道干嘛的
	 */
	public void setSurface(DashCamStat dcs) {
		STAT = dcs;
		Log.i("debug", "--- setSurface " + dcs.name());
		refreshSurface();
	}

	private void refreshSurface() {
		Log.i("debug", "--- refreshSurface");
		switch (STAT) {
		case BACKGROUND_START:
			ChangeSurface(35, 51, 1, 1);
			break;
		case BACKGROUND_STOP:
			break;
		case MINI_START:
			// for first device
			// ChangeSurface(12, 191, 92, 79);
			// for HM3
			ChangeSurface(35, 51, 130, 110);
			break;
		case MINI_STOP:
			ChangeSurface(35, 51, 1, 1);
			break;
		case NORMAL_START:
			// for first device
			// ChangeSurface(0, 0, mWidth, mHeight);
			// for HM3
			ChangeSurface(20, 0, 680, 400);
			break;
		case NORMAL_STOP:
			ChangeSurface(35, 51, 1, 1);
			break;
		default:
			break;
		}
	}

	private void ChangeSurface(int marginLeft, int marginTop, int width,
			int height) {
		Log.i("debug", "--- ChangeSurface");
		if (this.VIDEO_WIDTH / this.VIDEO_HEIGHT > width / height) {
			height = (int) (width * this.VIDEO_HEIGHT / this.VIDEO_WIDTH);
		} else {
			width = (int) (height * this.VIDEO_WIDTH / this.VIDEO_HEIGHT);
		}
		LayoutParams layoutParams = new WindowManager.LayoutParams(
				// WindowManager.LayoutParams.WRAP_CONTENT,
				// WindowManager.LayoutParams.WRAP_CONTENT,
				width, height, marginLeft, marginTop,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);
		if (width == 1) {
			layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		} else {
			// layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
			layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		}
		windowManager.updateViewLayout(mSurfaceView, layoutParams);
		if (width > 1) {
			if (STAT == DashCamStat.NORMAL_START) {
				enableInfoText();
			} else {
				disableInfoText();
			}
		} else {
			disableInfoText();
		}
	}

	private void disableInfoText() {
	}

	private void enableInfoText() {
	}

	/**
	 * 接收广播
	 * 
	 * @author NULL
	 *
	 */
	private class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			uploadImgTask = new UploadImgTask(context);
			String msg = intent.getAction();
			Log.i("PUSH_DEBUG", "msg--" + msg);

			if (msg.equals("net.carslink.dashcam.ReleaseBackCamera")|| msg.equals("net.carslink.dashcam.ReleaseFrontCamera")) {
				if (SharedSetting.takePictureQueueBack.size() > 0) {
					final String resendForUser = SharedSetting.takePictureQueueBack.poll();
					if (resendForUser.equals("null")) {
						takeBackPicture.shoot(TakePictureType.Loop, null, null);
					} else {// 用户多长请求拍照队列
						MyLocationListener myLocationListener = new MyLocationListener(context);
						myLocationListener.initAmapLBS(MyLocationListener.ONCE);
						myLocationListener.getLocationcallback(new GetLocationInterface() {

							@Override
							public void getLocation(HashMap<String, String> locationMsg) {
								takeBackPicture.shoot(TakePictureType.OnDemand,resendForUser, locationMsg);
							}
						});
					}
					return;
				} else if (startRecordingInQueue == true) {
					StartRecording();
					return;
				}
			} else if (msg.equals("com.baidu.push.TakePicture")) {
				final String forUser = intent.getStringExtra("fromUser");

				if (isrecording) {
					StopRecording("ScreenShot");
					//ProgressDialog();
					MyLocationListener myLocationListener = new MyLocationListener(context);
					myLocationListener.initAmapLBS(MyLocationListener.ONCE);
					myLocationListener.getLocationcallback(new GetLocationInterface() {

						@Override
						public void getLocation(HashMap<String, String> locationMsg) {
							takeBackPicture.shoot(TakePictureType.OnVideo,forUser, locationMsg);
							takeBackPicture.setRetartCallback(new RestartRecorder() {
								
								@Override
								public void heyRestart() {
									StartRecording("ScreenShot");
								}
							});
						}
					});
					return;
				} else if (SharedSetting.cameraLocked() == true) {
					SharedSetting.takePictureQueueBack.offer(forUser);
					return; 
				}
				MyLocationListener myLocationListener = new MyLocationListener(
						context);
				myLocationListener.initAmapLBS(MyLocationListener.ONCE);
				myLocationListener.getLocationcallback(new GetLocationInterface() {

					@Override
					public void getLocation(HashMap<String, String> locationMsg) {
						takeBackPicture.shoot(TakePictureType.OnDemand,forUser, locationMsg);
					}
				});

				return;
			} else if (msg.equals("com.baidu.push.InnerCar")) {
				String forUser = intent.getStringExtra("fromUser");
				if (isrecording) {
					uploadImgTask.sendMsgToServer(
							TakePictureType.Err_Recording, forUser);
					uploadImgTask.execute();
					return;
				} else if (SharedSetting.cameraLocked() == true) {
					SharedSetting.takePictureQueueFront.offer(forUser);
					return;
				}
				return;
			} else if (msg.equals("com.baidu.push.Setting")) {
				String extra = intent.getStringExtra("extra");
				Log.i("DEBUG", "GET Setting broadcast " + extra);

				if (extra.equals("dzwl_enable")) {
					/*
					 * try { mLocation = mLocationManager
					 * .getLastKnownLocation(LocationManager.GPS_PROVIDER); }
					 * catch (Exception e) { mLocation = null; } //TODO
					 * if(mLocation != null){ lastLat = mLocation.getLatitude();
					 * lastLon = mLocation.getLongitude(); }
					 * 
					 * if(mLocation == null || lastLat == 0 || lastLon == 0){
					 * dataToServer.sendMsgToServer(TakePictureType.No_GPS,
					 * "all"); }else{ SharedSetting.setDzwl(true); }
					 */
				} else if (extra.equals("dzwl_disable")) {
					SharedSetting.setDzwl(false);
				}

				return;
			} else if (msg.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				Log.d("DEBUG", "网络状态已经改变");
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					String name = info.getTypeName();
					Log.d("DEBUG", "当前网络名称：" + name);
					PushManager.startWork(getApplicationContext(),
							PushConstants.LOGIN_TYPE_API_KEY, Utils
									.getMetaValue(getApplicationContext(),
											"api_key"));
				} else {
					Log.d("DEBUG", "没有可用网络");
				}
			} else if (!msg.equals(Intent.ACTION_BATTERY_CHANGED))
				return;

			int chargingStatus = intent.getIntExtra("status",
					BatteryManager.BATTERY_STATUS_UNKNOWN);
			boolean charging = false;
			switch (chargingStatus) {
			case BatteryManager.BATTERY_STATUS_CHARGING: // 充电状态
				charging = true;
				break;
			case BatteryManager.BATTERY_STATUS_DISCHARGING:// 放电中
				charging = false;
				break;
			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:// 未充电
				charging = false;
				break;
			case BatteryManager.BATTERY_STATUS_FULL:// 充满电
				charging = true;
				break;
			case BatteryManager.BATTERY_STATUS_UNKNOWN:// 未知状态
				charging = false;
				break;
			}

			// 充电开启视频录像
			if (!lastChargingStatus && charging && !isRecording()) {
				if (SharedSetting.getAutoStart()) {
					StartRecording();
					sendBroadcast(new Intent("cam_start"));
					iButtonChange.changeButtonImg("start");
					// cbt.changeCallback("startVideo");//修改按钮文字描述
					// LocationCache.isStart = "1";
				}
			}

			// 断电关闭视频录像
			if (lastChargingStatus && !charging && isRecording()) {
				StopRecording();
				sendBroadcast(new Intent("cam_stop"));
				iButtonChange.changeButtonImg("stop");
				// cbt.changeCallback("stopVideo");//修改按钮文字描述
				// LocationCache.isStart = "0";
			}

			lastChargingStatus = charging;

			return;
		}
	}

	ButtonChangeInterface iButtonChange;

	public void buttonChangCallBack(ButtonChangeInterface iButtonChange) {
		this.iButtonChange = iButtonChange;
	}

	/**
	 * 以下为视频显示surfaceview
	 */

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
			int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		mSurfaceHolder = surfaceHolder;
		if (AUTOSTART) {
			StartRecording();
		}
	}

}
