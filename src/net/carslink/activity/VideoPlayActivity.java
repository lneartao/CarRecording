package net.carslink.activity;

import net.carslink.widget.VideoPlayerWidget;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class VideoPlayActivity extends Activity implements OnClickListener,
		OnTouchListener {
	ImageButton videoplayer_layout_button_start_pause,
			videoplayer_layout_button_return, 
			videoplayer_layout_button_back,
			videoplayer_layout_button_go;

	FrameLayout videoplayer_layout_videoview;

	SeekBar videoplayer_layout_progressbar;
	TextView videoplayer_layout_textview_currenttime,
			videoplayer_layout_textview_totaltime;

	VideoPlayerWidget videoPlayerWidget;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_layout_videoplay);
		setupAllView();

		Intent getDataIntent = this.getIntent();
		String videoPath = getDataIntent.getStringExtra("videoPath");

		videoPlayerWidget = new VideoPlayerWidget(VideoPlayActivity.this, null,
				videoplayer_layout_progressbar,
				videoplayer_layout_textview_currenttime,
				videoplayer_layout_textview_totaltime);
		
		videoPlayerWidget.setVideoPath(videoPath);
		videoPlayerWidget.setControlButton(videoplayer_layout_button_start_pause);
	
		videoplayer_layout_videoview.addView(videoPlayerWidget);

	}

	@Override
	protected void onResume() {

		super.onResume();
	}

	/**
	 * 按钮布局
	 */
	public void setupAllView() {
		videoplayer_layout_button_start_pause = (ImageButton) findViewById(R.id.videoplayer_layout_button_start_pause);
		videoplayer_layout_button_return = (ImageButton) findViewById(R.id.videoplayer_layout_button_return);
		videoplayer_layout_button_back = (ImageButton) findViewById(R.id.videoplayer_layout_button_back);
		videoplayer_layout_button_go = (ImageButton) findViewById(R.id.videoplayer_layout_button_go);

		videoplayer_layout_button_start_pause.setOnClickListener(this);
		videoplayer_layout_button_return.setOnClickListener(this);

		videoplayer_layout_button_back.setOnTouchListener(this);
		videoplayer_layout_button_go.setOnTouchListener(this);

		videoplayer_layout_videoview = (FrameLayout) findViewById(R.id.videoplayer_layout_videoview);

		videoplayer_layout_textview_currenttime = (TextView) findViewById(R.id.videoplayer_layout_textview_currenttime);
		videoplayer_layout_textview_totaltime = (TextView) findViewById(R.id.videoplayer_layout_textview_totaltime);
		videoplayer_layout_progressbar = (SeekBar) findViewById(R.id.videoplayer_layout_progressbar);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.videoplayer_layout_button_return:
			videoPlayerWidget.stopVideo();
			break;

		case R.id.videoplayer_layout_button_start_pause:
			videoPlayerWidget.startOrPauseVideo();
			
			break;

		/*
		 * case R.id.videoplayer_layout_button_back: break;
		 * 
		 * case R.id.videoplayer_layout_button_go: break;
		 */

		default:
			break;
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch (view.getId()) {
		case R.id.videoplayer_layout_button_back:
			handleTouchEvent(event, "back");
			break;

		case R.id.videoplayer_layout_button_go:
			handleTouchEvent(event, "go");
			break;

		default:
			break;
		}

		return true;
	}

	public void handleTouchEvent(MotionEvent event, String type) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(videoPlayerWidget.getPlayingStatus()){
				addIsRunning = true;
				if (type.equals("back")) {
					new Thread(redTime).start();
				} else if (type.equals("go")) {
					new Thread(addTime).start();
				}
			}else{
				Toast.makeText(VideoPlayActivity.this, "视频暂停，无法快进或后退", Toast.LENGTH_SHORT).show();
			}

			break;

		case MotionEvent.ACTION_MOVE:
			break;

		case MotionEvent.ACTION_UP:
			addIsRunning = false;

			break;
		}
	}

	final int COUNT = 1;
	boolean addIsRunning = false;
	Runnable addTime = new Runnable() {
		@Override
		public void run() {
			while (addIsRunning) {
				// videoPlayerWidget.getCurrentTime();
				videoPlayerWidget.setSeekToTime(COUNT + videoPlayerWidget.getCurrentTime());
			}
		}
	};

	Runnable redTime = new Runnable() {
		@Override
		public void run() {
			while (addIsRunning) {
				videoPlayerWidget.setSeekToTime(videoPlayerWidget.getCurrentTime() - COUNT);
			}
		}
	};
}