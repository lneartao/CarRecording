package net.carslink.widget;

import net.carslink.activity.MainActivity;
import net.carslink.interfaces.SettingViewInterface;
import net.carslink.util.ScreenWH;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

//创建手势侦听对象
public class GestureListener extends SimpleOnGestureListener {

	private Context mContext;
	private View listenerView;
	private AnimationWidget animations;

	private int leftMargin = 0;
	private int boxWidth;
	private int leftDis;//view移动的距离
	private MotionEvent event;
	
	public GestureListener(Context mContext) {
		this.mContext = mContext;

		animations = new AnimationWidget(mContext);

		boxWidth = ScreenWH.width / 3 * 2;
		leftMargin = -boxWidth / 4 * 3;
	}

	public void setupListenerView(View listenerView) {
		this.listenerView = listenerView;
	}

	@Override
	// 按下触摸屏按下时立刻触发
	public boolean onDown(MotionEvent e) {
		Log.v("GESTURE", "按下");
		return true;
	}

	// 短按，触摸屏按下片刻后抬起，会触发这个手势，如果迅速抬起则不会
	@Override
	public void onShowPress(MotionEvent e) {
		Log.v("GESTURE", "短按");
	}

	// 释放，手指离开触摸屏时触发(长按、滚动、滑动时，不会触发这个手势)
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Log.v("GESTURE", "释放");

		return false;
	}

	// 滚动，按下后滚动
	private static final int SWIPE_MIN_DISTANCE = 50;
	private static final int SWIPE_THRESHOLD_VELOCITY = 0;

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		Log.v("GESTURE", "滚动，按下后滚动");
		if (listenerView.getVisibility() == View.VISIBLE) {

			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(distanceX) > SWIPE_THRESHOLD_VELOCITY
					&& MainActivity.whoViewShow != MainActivity.NOT_VIEW) {
				// 左滑动
				int len = (int) (e1.getX() - e2.getX());
				leftDis = -len;

				if (Math.abs(len) <= (boxWidth / 4 * 3)) {
					iSettingView.moveView(leftDis);
				}
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(distanceX) > SWIPE_THRESHOLD_VELOCITY) {
				// 右滑动
				int len = (int) (e2.getX() - e1.getX());
				leftDis = leftMargin + len;

				if (Math.abs(len) <= (boxWidth / 4 * 3)) {
					iSettingView.moveView(leftDis);
				}
			}

		}

		return false;
	}

	// 长按，触摸屏按下后既不抬起也不移动，过一段时间后触发
	@Override
	public void onLongPress(MotionEvent e) {
		Log.v("GESTURE", "长按");
		return;
	}

	// 滑动，触摸屏按下后快速移动并抬起，会先触发滚动手势，跟着触发一个滑动手势
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.v("GESTURE", "滑动，触摸屏按下后快速移动并抬起，会先触发滚动手势，跟着触发一个滑动手势");
		if (listenerView.getVisibility() == View.VISIBLE) {
			// 隐藏部分为屏幕宽一半
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY
					&& MainActivity.whoViewShow != MainActivity.NOT_VIEW) {
				// 左滑动
				iSettingView.popView(leftMargin);
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// 右滑动
				iSettingView.popView(0);
			}
		}

		return false;
	}

	// 双击，手指在触摸屏上迅速点击第二下时触发
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		Log.v("GESTURE", "双击");
		return false;
	}

	// 双击后按下跟抬起各触发一次
	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		Log.v("GESTURE", "双击后按下跟抬起各触发一次");
		return false;
	}

	// 单击
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		Log.v("GESTURE", "单击");

		int isShow = listenerView.getVisibility();

		if (MainActivity.whoViewShow == MainActivity.NOT_VIEW) {
			if (isShow == View.GONE) {
				listenerView.setVisibility(View.VISIBLE);
				listenerView.startAnimation(animations.mAlphaAnimation(0, 1f,
						300, 0, true));
			} else {
				listenerView.setVisibility(View.GONE);
				listenerView.startAnimation(animations.mAlphaAnimation(1f, 0f,
						300, 0, true));
			}
		} else {
			iSettingView.hideViewCallback();
		}

		return true;
	}
	
	//回调函数
	SettingViewInterface iSettingView;

	public void setCallback(SettingViewInterface iSettingView) {
		this.iSettingView = iSettingView;
	}
}
