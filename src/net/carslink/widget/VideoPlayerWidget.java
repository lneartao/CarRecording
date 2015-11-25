package net.carslink.widget;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import net.carslink.activity.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class VideoPlayerWidget extends SurfaceView implements
		SurfaceHolder.Callback, OnCompletionListener, OnErrorListener,
		OnPreparedListener, OnInfoListener, OnSeekCompleteListener,
		OnVideoSizeChangedListener {
	private MediaPlayer mediaPlayer;
	private SurfaceHolder holder;
	private Context context;
	private ImageView videoplayer_layout_button_start_pause;
	
	private boolean nowPlayer = true;// 判断是否正在播放还是重新开始播放

	Timer progressTimer;
	TimerTask progressTimerTask = null;
	
	private SeekBar progressBar;
	private TextView textview_currenttime,
	textview_totaltime;

	int duration;//总时间
	
	public VideoPlayerWidget(Context context, AttributeSet attrs,
			SeekBar progressBar, TextView textview_currenttime,
			TextView textview_totaltime) {
		
		super(context, attrs);
		this.context = context;
		this.progressBar = progressBar;
		this.textview_currenttime = textview_currenttime;
		this.textview_totaltime = textview_totaltime;
		
		// 进度视图
		this.progressBar.setMax(100);

		setupHolder();
		initMediaPlayer();
		setupProgressListene();
	}

	/**
	 * 进度条监听
	 */
	private void startProgressTimer() {
		progressTimer = new Timer();
		progressTimerTask = new TimerTask() {
			@Override
			public void run() {
				if (mediaPlayer == null)
					return;
				if (mediaPlayer.isPlaying() && progressBar.isPressed() == false) {
					int position = getCurrentTime();
					handleProgress.sendEmptyMessage(position);
				}
			}
		};
		progressTimer.scheduleAtFixedRate(progressTimerTask, 0, 50);
	}

	private void stopProgressTimer() {
		progressTimerTask.cancel();
		progressTimerTask = null;
		progressTimer.cancel();
		progressTimer.purge();
		progressTimer = null;
	}

	private Handler handleProgress = new Handler() {
		public void handleMessage(Message msg) {
			int position = msg.what;
			if (duration > 0) {
				long progress = progressBar.getMax()*position / duration;
				progressBar.setProgress((int) progress);
				//Log.v("PUSH_VIDEO", "进度："+duration);
			}
		};
	};

	/**
	 * 绑定进度条监听
	 */
	private void setupProgressListene(){
		progressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar progressBar) {
				int progress = progressBar.getProgress();
				long position = duration*progressBar.getProgress()/progressBar.getMax();
				
				if (duration > 0) {
					mediaPlayer.seekTo((int)position);
				}
				progressBar.setProgress((int) progress);
				textview_currenttime.setText(setVideoTimeStr((int)position));
		
				if(nowPlayer == false){
					mediaPlayer.seekTo((int)position);
					mediaPlayer.start();
					nowPlayer = true;
				}
				
				startProgressTimer();
			}	
			
			@Override
			public void onStartTrackingTouch(SeekBar progressBar) {
				if(progressTimer != null){
					stopProgressTimer();
				}
			}
			
			@Override
			public void onProgressChanged(SeekBar progressBar, int progress, boolean fromUser) {
				int position = duration*progress/progressBar.getMax();
				textview_currenttime.setText(setVideoTimeStr(position));

				//Log.v("PUSH_VIDEO", "我拖动了进度条，我值在改变" + progress+ "\n我是当前时间" + position);
			}
		});
    }

	/**
	 * 初始化播放器
	 */
	private void initMediaPlayer() {
		mediaPlayer = new MediaPlayer();

		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnInfoListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnSeekCompleteListener(this);
		mediaPlayer.setOnVideoSizeChangedListener(this);
	}

	/**
	 * 获得surfaceview控制权
	 * @return SurfaceHolder->holder
	 */
	@SuppressWarnings("deprecation")
	private void setupHolder() {
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	public void setControlButton(ImageView videoplayer_layout_button_start_pause){
		this.videoplayer_layout_button_start_pause = videoplayer_layout_button_start_pause;
	}
	
	public void setVideoPath(String videoPath) {
		try {
			mediaPlayer.setDataSource(videoPath);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 播放暂停视频
	 */
	public void startOrPauseVideo() {
		if (nowPlayer) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				videoplayer_layout_button_start_pause.setBackgroundResource(R.drawable.img_btn_player);
			} else {
				mediaPlayer.start();
				videoplayer_layout_button_start_pause.setBackgroundResource(R.drawable.img_btn_stop);
			}
		} else {
			startProgressTimer();
			mediaPlayer.start();
			videoplayer_layout_button_start_pause.setBackgroundResource(R.drawable.img_btn_stop);
			nowPlayer = true;
		}

	}

	public void stopVideo() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
		((Activity) context).finish();
	}

	/**
	 * 获得播放状态，供外部访问
	 * @return
	 */
	public boolean getPlayingStatus(){
		return mediaPlayer.isPlaying();
	}
	
	// 重设进度条和事间
	public void resetAllData() {
		videoplayer_layout_button_start_pause.setBackgroundResource(R.drawable.img_btn_player);
		progressBar.setProgress(0);
		textview_currenttime.setText("00:00:00");
	}
	
	/**
	 * 设置显示的时间格式
	 * 
	 * @return
	 */
	private String setVideoTimeStr(int playTime) {
		String timeStr = "";
		playTime /= 1000;
		
		if (playTime < 60) {
			timeStr = "00:00:" + addZero(playTime);
		} else if (playTime >= 60 && playTime < 3600) {
			int min = playTime / 60;
			int sec = playTime % 60;

			timeStr = "00:" + addZero(min) + ":" + addZero(sec);
		} else if (playTime >= 3600) {
			int hou = playTime / 60 / 60;
			int min = (playTime / 60) % 60;
			int sec = playTime % 60;

			timeStr = addZero(hou) + ":" + addZero(min) + ":" + addZero(sec);
		}

		return timeStr;
	}

	private String addZero(int totalTime) {
		if (totalTime < 10) {
			return "0" + totalTime;
		}
		return totalTime + "";
	}

	/**
	 * 获得当前播放时间
	 * 
	 * @return
	 */
	public int getCurrentTime() {
		return mediaPlayer.getCurrentPosition();
	}

	/**
	 * 设置播放进度
	 * 
	 * @param seekToTime
	 */
	public void setSeekToTime(int seekToTime) {
		mediaPlayer.seekTo(seekToTime);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		// 指定MediaPlayer在当前的Surface中进行播放
		mediaPlayer.setDisplay(surfaceHolder);
		// 在指定了MediaPlayer播放的容器后，就可以使用prepare或者prepareAsync来准备播放了
		mediaPlayer.prepareAsync();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		if (progressTimer != null) {
			stopProgressTimer();
		}
		
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
		try{
			mediaPlayer.release();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	// 这个方法在设置player的source后至少触发一次
	@Override
	public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width,
			int height) {
	}

	@Override
	public void onSeekComplete(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onInfo(MediaPlayer mediaPlayer, int whatInfo, int extra) {
		// 当一些特定信息出现或者警告时触发
		switch (whatInfo) {
		case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
			Log.v("Play Error:::", "MEDIA_INFO_BAD_INTERLEAVING");
			break;
		case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
			Log.v("Play Error:::", "MEDIA_INFO_METADATA_UPDATE");
			break;
		case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
			Log.v("Play Error:::", "MEDIA_INFO_VIDEO_TRACK_LAGGING");
			break;
		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			Log.v("Play Error:::", "MEDIA_INFO_NOT_SEEKABLE");
			break;
		}
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		
		mediaPlayer.start();
		videoplayer_layout_button_start_pause.setBackgroundResource(R.drawable.img_btn_stop);
		duration = mediaPlayer.getDuration();// 获取视频总时间长度
		if(duration/1000<=0){
			new AlertDialog.Builder(context)
			.setMessage("该视频无法打开，具体原因是长度小于一秒")
			.setPositiveButton("确定",null)
			.create()
			.show();
			return;
		}
		textview_totaltime.setText(setVideoTimeStr(duration));

		// 开启进度条定时监听器
		startProgressTimer();
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int whatError, int extra) {
		switch (whatError) {
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			Log.v("Play Error:::", "MEDIA_ERROR_SERVER_DIED");
			break;
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			Log.v("Play Error:::", "MEDIA_ERROR_UNKNOWN");
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		stopProgressTimer();
		resetAllData();
		nowPlayer = false;
		
	}
}
