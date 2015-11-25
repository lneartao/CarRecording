package net.carslink.activity;

import java.util.List;

import net.carslink.navimap.MapActivity;
import net.carslink.service.BackgroundService;
import net.carslink.util.ScreenWH;
import net.carslink.util.SharedSetting;
import net.carslink.widget.AnimationWidget;
import net.carslink.widget.CreateQRCode;
import net.carslink.widget.GestureListener;
import net.carslink.widget.Setting;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.push.custom.Utils;

import net.carslink.activity.R;
import net.carslink.interfaces.ButtonChangeInterface;
import net.carslink.interfaces.QRCodeInterface;
import net.carslink.interfaces.SettingViewInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

@SuppressLint({ "InflateParams", "RtlHardcoded", "ClickableViewAccessibility" })
public class MainActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener, OnTouchListener {
	ImageButton leftmenu_layout_button_map, leftmenu_layout_button_erweima,
			leftmenu_layout_button_moremenu;

	ImageButton right_videoview_layout_button_replay,
			right_videoview_layout_button_recording,
			right_videoview_layout_button_keeplong;

	ImageView left_qrbox_layout_view_qrimg;

	ListView setting_layout_list_settings;
	FrameLayout right_videoview_layout_priview;

	LinearLayout left_qrbox_layout_mainbox;

	RadioGroup keeplongRadioGroup;

	boolean mBound = false;// 服务启动时为true

	AnimationWidget animations;
	GestureListener myGestureListener;

	BroadcastReceiver bRestartService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!SharedSetting.isInited())
			SharedSetting.initSharedSetting(getSharedPreferences("SettingXML",
					0));

		setContentView(R.layout.activity_layout_main);

		new ScreenWH(MainActivity.this);
		animations = new AnimationWidget(MainActivity.this);

		createSettingView();
		setupAllView();

		setupGesture();

		startBackgroundService();
		/*
		 * 注册广播
		 */
		/*this.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				setBright(255);
				Log.w("BR_DEBUG_CATCH", "ACTION SCREEN OFF");
			}
		}, new IntentFilter(Intent.ACTION_SCREEN_OFF));

		this.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.w("BR_DEBUG_CATCH", "ACTION SCREEN ON");
				setBright(0);
			}
		}, new IntentFilter(Intent.ACTION_SCREEN_ON));*/

		//重启服务广播
		bRestartService = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(!isServiceExisted(getApplicationContext(), "net.carslink.service.BackgroundService")){
					mBound = true;
					startBackgroundService();
				}
			}
			
		};
		this.registerReceiver(bRestartService, new IntentFilter(Intent.ACTION_TIME_TICK));
		
		// 百度云推送
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY,
				Utils.getMetaValue(MainActivity.this, "api_key"));
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {

		Setting sett = (Setting) findViewById(R.id.setting_layout_list_settings);
		if (sett != null) {
			sett.refreshSettingListData();
		}

		super.onResume();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if(bRestartService != null){
			this.unregisterReceiver(bRestartService);
		}
		
		stopBackgroundService();
		super.onDestroy();
	}

	/*private void setBright(int value) {
		android.provider.Settings.System.putInt(getContentResolver(),
				android.provider.Settings.System.SCREEN_BRIGHTNESS, value);
	}
*/
	/**
	 * 按钮布局
	 */
	public void setupAllView() {
		leftmenu_layout_button_map = (ImageButton) findViewById(R.id.leftmenu_layout_button_map);
		leftmenu_layout_button_erweima = (ImageButton) findViewById(R.id.leftmenu_layout_button_erweima);
		leftmenu_layout_button_moremenu = (ImageButton) findViewById(R.id.leftmenu_layout_button_moremenu);

		right_videoview_layout_button_replay = (ImageButton) findViewById(R.id.right_videoview_layout_button_replay);
		right_videoview_layout_button_recording = (ImageButton) findViewById(R.id.right_videoview_layout_button_recording);
		right_videoview_layout_button_keeplong = (ImageButton) findViewById(R.id.right_videoview_layout_button_keeplong);

		right_videoview_layout_priview = (FrameLayout) findViewById(R.id.right_videoview_layout_priview);
		setting_layout_list_settings = (ListView) findViewById(R.id.setting_layout_list_settings);

		left_qrbox_layout_view_qrimg = (ImageView) findViewById(R.id.left_qrbox_layout_view_qrimg);
		left_qrbox_layout_view_qrimg.setBackgroundResource(R.drawable.img_rotate);

		left_qrbox_layout_mainbox = (LinearLayout) findViewById(R.id.left_qrbox_layout_mainbox);

		leftmenu_layout_button_map.setOnClickListener(this);
		leftmenu_layout_button_erweima.setOnClickListener(this);
		leftmenu_layout_button_moremenu.setOnClickListener(this);

		right_videoview_layout_button_replay.setOnClickListener(this);
		right_videoview_layout_button_recording.setOnClickListener(this);
		right_videoview_layout_button_keeplong.setOnClickListener(this);
	}

	/*
	 * 开启后台服务
	 */
	private BackgroundService backgroundService = null;
	private Intent backgroundServiceIntent;
	private ServiceConnection bacServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
			Toast.makeText(MainActivity.this, "服务被干掉", Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) service;
			mBound = true;
			backgroundService = binder.getService();
			backgroundService
					.setVideoPlayLayout(right_videoview_layout_priview);

			backgroundService.buttonChangCallBack(new ButtonChangeInterface() {

				@Override
				public void changeButtonImg(String startOrStop) {
					if (startOrStop.equals("start")) {
						right_videoview_layout_button_recording
								.setBackgroundResource(R.drawable.img_btn_stop2);
					} else {
						right_videoview_layout_button_recording
								.setBackgroundResource(R.drawable.img_btn_recording);
					}
				}
			});
		}
	};

	public void startBackgroundService() {
		if (backgroundService == null) {
			backgroundServiceIntent = new Intent(this, BackgroundService.class);
			bindService(backgroundServiceIntent, bacServiceConnection,
					Context.BIND_AUTO_CREATE);
		}
	}

	public void stopBackgroundService() {
		if (backgroundService != null) {
			unbindService(bacServiceConnection);
		}
	}

	/**
	 * 创建程序设置界面
	 */
	View leftBoxView;
	public static int SETTING_VIEW = 1, QR_VIEW = 2, NOT_VIEW = 3;
	public static int whoViewShow;// 一开始没有view显示

	@SuppressLint("NewApi")
	public void createSettingView() {
		LayoutInflater li = this.getLayoutInflater();
		leftBoxView = li.inflate(R.layout.layout_left_menu, null);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);

		int boxWidth = ScreenWH.width / 3 * 2;
		int buttonWidth = boxWidth / 4;
		int popBoxWidth = boxWidth - buttonWidth;

		params.width = boxWidth;
		params.gravity = Gravity.LEFT;
		params.leftMargin = -popBoxWidth;

		this.addContentView(leftBoxView, params);

		whoViewShow = NOT_VIEW;
	}

	public void showSettingView(final int whoViewShow) {
		animations.mTranslateAnimation(0, 1f / 4 * 3, 0, 0, 100, 0, true);
		TranslateAnimation translateAniamtion = animations
				.getTranslateAnimation();
		translateAniamtion.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation translateAniamtion) {
			}

			@Override
			public void onAnimationRepeat(Animation translateAniamtion) {
			}

			@Override
			public void onAnimationEnd(Animation translateAniamtion) {
				leftBoxView.clearAnimation();
				// leftBoxView.layout(0, 0, ScreenWH.width/3*2,
				// ScreenWH.height);

				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
						leftBoxView.getWidth(), leftBoxView.getHeight());
				params.leftMargin = 0;
				leftBoxView.setLayoutParams(params);

				if (whoViewShow == MainActivity.SETTING_VIEW) {
					MainActivity.whoViewShow = whoViewShow;
				} else {
					MainActivity.whoViewShow = whoViewShow;
				}
			}
		});

		leftBoxView.startAnimation(animations.getTranslateAnimation());
	}

	public void hideSettingView(final int whoViewShow) {
		animations.mTranslateAnimation(0, -1f / 4 * 3, 0, 0, 100, 0, true);
		TranslateAnimation translateAniamtion = animations
				.getTranslateAnimation();
		translateAniamtion.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation translateAniamtion) {
			}

			@Override
			public void onAnimationRepeat(Animation translateAniamtion) {
			}

			@Override
			public void onAnimationEnd(Animation translateAniamtion) {
				leftBoxView.clearAnimation();
				// int boxWidth = ScreenWH.width / 3 * 2;
				// leftBoxView.layout(-boxWidth/4*3, 0, boxWidth/4,
				// ScreenWH.height);

				// 必须使用layoutparmas，不然调用 setImageBitmap() 位置会混乱
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
						leftBoxView.getWidth(), leftBoxView.getHeight());
				params.leftMargin = -ScreenWH.width / 2;
				leftBoxView.setLayoutParams(params);

				MainActivity.whoViewShow = MainActivity.NOT_VIEW;
			}
		});

		leftBoxView.startAnimation(translateAniamtion);
	}

	/*
	 * 获取微信二维码
	 */
	public void getWeixinQRCode() {
		CreateQRCode qrCode = new CreateQRCode(MainActivity.this);
		qrCode.createWeixinQRCode();
		qrCode.setCallback(new QRCodeInterface() {

			@Override
			public void getQRCodeSuccess(Bitmap qrBitmap) {
				left_qrbox_layout_view_qrimg.clearAnimation();
				left_qrbox_layout_view_qrimg.setImageBitmap(qrBitmap);
			}
		});
	}

	/**
	 * 显示保留视频类型的radio
	 * 
	 * @param view
	 *            单选按钮群
	 */
	View keeplongRadioView;
	PopupWindow keeplongPopWin;

	public void showKeepLongRadioPop(View view) {
		int viewH = view.getHeight();
		int viewW = view.getWidth();
		int[] location = new int[2];

		// 相关按钮动画
		// right_videoview_layout_button_keeplong.setBackgroundResource(R.drawable.keepbutton_click);
		animations.mRotateAnimation(0, 180, 0.5f, 0.5f, 200, 0, true);
		right_videoview_layout_button_keeplong.startAnimation(animations
				.getRotateAnimation());

		if (keeplongPopWin == null) {
			LayoutInflater inflater = this.getLayoutInflater();
			keeplongRadioView = inflater.inflate(
					R.layout.layout_radio_keeplong, null);

			// 查找radiogroup，并监听
			keeplongRadioGroup = (RadioGroup) keeplongRadioView
					.findViewById(R.id.keeplong_radio_layout_radiogroup);
			keeplongRadioGroup.setOnCheckedChangeListener(this);

			keeplongPopWin = new PopupWindow(keeplongRadioView, viewW * 2,
					viewH * 2 + 30);

			keeplongPopWin.setFocusable(true);// 设置焦点
			keeplongPopWin.setOutsideTouchable(true);// 设置弹窗之外触屏时消失
			keeplongPopWin.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.img_bg_keeplong));

			keeplongPopWin
					.setAnimationStyle(android.R.style.Animation_InputMethod);

			keeplongPopWin.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					// right_videoview_layout_button_keeplong.setBackgroundResource(R.drawable.keepbutton_normal);
					animations.mRotateAnimation(180, 0, 0.5f, 0.5f, 200, 0,
							true);
					right_videoview_layout_button_keeplong
							.startAnimation(animations.getRotateAnimation());
				}
			});
		}

		view.getLocationInWindow(location);

		location[0] = location[0] - viewW / 2;// x方向坐标
		location[1] = location[1] - viewH * 2 - 30;// y方向坐标

		// animations.mRotateAnimation(180, 360, 0.5f, 1f, 200, 0);

		keeplongPopWin.showAtLocation(view, Gravity.NO_GRAVITY, location[0],
				location[1]);
	}

	/**
	 * 按钮监听
	 */
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.leftmenu_layout_button_map:
			if(backgroundService.isRecording()){
				new AlertDialog.Builder(MainActivity.this)
				.setMessage("你正在录像，是否停止录像，跳转导航！")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						backgroundService.StopRecording();
						right_videoview_layout_button_recording.setBackgroundResource(R.drawable.img_btn_recording);
						Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
						startActivity(mapIntent);
					}
				})
				.setNegativeButton("否", null)
				.create()
				.show();
			}else{
				Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
				startActivity(mapIntent);
			}
			break;

		case R.id.leftmenu_layout_button_erweima:
			String deviceId = SharedSetting.getDeviceId();

			if (deviceId.equals("13912341230")) {
				/*Intent LoginIntent = new Intent(MainActivity.this,
				LoginRegActivity.class);
				startActivity(LoginIntent);*/
				
				if(backgroundService.isRecording()){
					new AlertDialog.Builder(MainActivity.this)
					.setMessage("你正在录像，是否停止录像，前往登录")
					.setPositiveButton("是", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							backgroundService.StopRecording();
							right_videoview_layout_button_recording.setBackgroundResource(R.drawable.img_btn_recording);
							Intent loginIntent = new Intent(MainActivity.this,LoginRegActivity.class);
							startActivity(loginIntent);
						}
					})
					.setNegativeButton("否", null)
					.create()
					.show();
				}else{
					Intent loginIntent = new Intent(MainActivity.this,LoginRegActivity.class);
					startActivity(loginIntent);
				}
				
				return;
			}

			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					setting_layout_list_settings.setVisibility(View.GONE);
					if (whoViewShow == QR_VIEW) {
						hideSettingView(NOT_VIEW);
						leftmenu_layout_button_erweima
								.setBackgroundResource(R.drawable.img_btn_qr);
					} else {
						if (whoViewShow == NOT_VIEW) {
							showSettingView(QR_VIEW);
						} else if (whoViewShow == SETTING_VIEW) {
							leftmenu_layout_button_moremenu
									.setBackgroundResource(R.drawable.img_btn_setting);
							whoViewShow = QR_VIEW;
						}
						
						Bitmap rotate = null;
						try{//捕获内存溢出异常
							rotate = BitmapFactory.decodeResource(getResources(), R.drawable.img_rotate);
						}catch(OutOfMemoryError oom){
							oom.printStackTrace();
						}
						
						left_qrbox_layout_view_qrimg.setImageBitmap(rotate);

						animations.mRotateAnimation(0, 359, 0.5f, 0.5f, 500,-1, false);
						left_qrbox_layout_view_qrimg.startAnimation(animations.getRotateAnimation());
						leftmenu_layout_button_erweima.setBackgroundResource(R.drawable.img_btn_qr_show);
						getWeixinQRCode();
					}
				}
			});

			break;

		case R.id.leftmenu_layout_button_moremenu:
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setting_layout_list_settings.setVisibility(View.VISIBLE);
					if (whoViewShow == NOT_VIEW) {
						leftmenu_layout_button_moremenu
								.setBackgroundResource(R.drawable.img_btn_setting_show);
						showSettingView(SETTING_VIEW);
					} else if (whoViewShow == QR_VIEW) {
						leftmenu_layout_button_erweima
								.setBackgroundResource(R.drawable.img_btn_qr);
						leftmenu_layout_button_moremenu
								.setBackgroundResource(R.drawable.img_btn_setting_show);
						whoViewShow = SETTING_VIEW;
					} else if (whoViewShow == SETTING_VIEW) {
						hideSettingView(NOT_VIEW);
						leftmenu_layout_button_moremenu
								.setBackgroundResource(R.drawable.img_btn_setting);
					}
				}
			});

			break;

		case R.id.right_videoview_layout_button_replay:
			if(backgroundService.isRecording()){
				new AlertDialog.Builder(MainActivity.this)
				.setMessage("你正在录像，是否停止录像，查看文件！")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						backgroundService.StopRecording();
						right_videoview_layout_button_recording.setBackgroundResource(R.drawable.img_btn_recording);
						Intent fileManage = new Intent(MainActivity.this,FileManageActivity.class);
						startActivity(fileManage);
					}
				})
				.setNegativeButton("否", null)
				.create()
				.show();
			}else{
				Intent fileManage = new Intent(MainActivity.this,FileManageActivity.class);
				startActivity(fileManage);
			}
			break;

		case R.id.right_videoview_layout_button_recording:
			if (backgroundService.isRecording()) {
				// backgroundService.setSurface(DashCamStat.MINI_STOP);
				right_videoview_layout_button_recording
						.setBackgroundResource(R.drawable.img_btn_recording);
				backgroundService.StopRecording("click");
			} else {
				// backgroundService.setSurface(DashCamStat.MINI_START);
				right_videoview_layout_button_recording
						.setBackgroundResource(R.drawable.img_btn_stop2);
				backgroundService.StartRecording("click");
			}
			break;

		case R.id.right_videoview_layout_button_keeplong:
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showKeepLongRadioPop(right_videoview_layout_button_keeplong);
				}
			});
			break;

		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup keeplongRadioGroup, int checkedId) {
		int id = keeplongRadioGroup.getCheckedRadioButtonId();
		RadioButton radioButton = (RadioButton) keeplongRadioGroup
				.findViewById(id);
		if (mBound) {
			switch (id) {
			case R.id.keeplong_radio_layout_long:
				//Toast.makeText(MainActivity.this, radioButton.getText() + "",Toast.LENGTH_SHORT).show();
				backgroundService.setIsfavorite(1);

				break;
			case R.id.keeplong_radio_layout_interim:
				//Toast.makeText(MainActivity.this, radioButton.getText() + "",Toast.LENGTH_SHORT).show();
				backgroundService.setIsfavorite(0);

				break;
			default:
				break;
			}
		}
	}

	/**
	 * 设置手势
	 */
	GestureDetector gestureDetector;
	public static boolean isOnScroll = false;
	static int leftDis;

	public void setupGesture() {
		myGestureListener = new GestureListener(MainActivity.this);
		myGestureListener.setupListenerView(leftBoxView);
		gestureDetector = new GestureDetector(MainActivity.this,
				myGestureListener);
		getWindow().getDecorView().setOnTouchListener(this);
		getWindow().getDecorView().setLongClickable(true);

		myGestureListener.setCallback(new SettingViewInterface() {

			@Override
			public void hideViewCallback() {
				hideSettingView(whoViewShow);
				leftmenu_layout_button_moremenu
						.setBackgroundResource(R.drawable.img_btn_setting);
				leftmenu_layout_button_erweima
						.setBackgroundResource(R.drawable.img_btn_qr);
			}

			@Override
			public void moveView(int leftDis) {
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
						leftBoxView.getWidth(), leftBoxView.getHeight());
				params.leftMargin = leftDis;
				leftBoxView.setLayoutParams(params);

				MainActivity.leftDis = leftDis;
				isOnScroll = true;
			}

			@Override
			public void popView(int leftMargin) {
				handlePopView(leftMargin);
			}
		});
	}

	public void handlePopView(int leftMargin) {
		int isShow = setting_layout_list_settings.getVisibility();
		if (leftMargin >= 0) {
			if (isShow != View.GONE) {
				whoViewShow = SETTING_VIEW;
				leftmenu_layout_button_moremenu
						.setBackgroundResource(R.drawable.img_btn_setting_show);
				leftmenu_layout_button_erweima
						.setBackgroundResource(R.drawable.img_btn_qr);
			} else {
				whoViewShow = QR_VIEW;
				leftmenu_layout_button_moremenu
						.setBackgroundResource(R.drawable.img_btn_setting);
				leftmenu_layout_button_erweima
						.setBackgroundResource(R.drawable.img_btn_qr_show);
			}
		} else {
			whoViewShow = NOT_VIEW;
			leftmenu_layout_button_moremenu
					.setBackgroundResource(R.drawable.img_btn_setting);
			leftmenu_layout_button_erweima
					.setBackgroundResource(R.drawable.img_btn_qr);
		}
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				leftBoxView.getWidth(), leftBoxView.getHeight());
		params.leftMargin = leftMargin;
		leftBoxView.setLayoutParams(params);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP && isOnScroll) {
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {

					isOnScroll = false;
					int boxWidth = ScreenWH.width / 3 * 2;
					int leftMargin = -boxWidth / 4 * 3;
					if (leftMargin/2 <= leftDis) {
						handlePopView(0);
					} else {
						handlePopView(leftMargin);
					}
				}
			});

			Log.v("GESTURE", "是否抬起了" + event.getAction());
		}
		return gestureDetector.onTouchEvent(event);
	}

	//检查服务是否存在
    public boolean isServiceExisted(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);

        int len = serviceList.size();
        
        if(!(len > 0)) {
            return false;
        }

        for(ActivityManager.RunningServiceInfo aService : serviceList){
            ComponentName serviceName = aService.service;

            if(serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        
        return false;
    }
	
	/*
	 * 重写返回监听
	 */
	/*
	 * @Override public boolean dispatchKeyEvent(KeyEvent event) { if
	 * (event.getAction() == KeyEvent.ACTION_DOWN) { switch (event.getKeyCode())
	 * { case KeyEvent.KEYCODE_BACK: return true; case KeyEvent.KEYCODE_HOME:
	 * return true; } } return super.dispatchKeyEvent(event); }
	 */

}
