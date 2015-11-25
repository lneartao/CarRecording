package net.carslink.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

@SuppressLint("InflateParams")
public class AboutUsActivity extends Activity implements OnClickListener {

	Button aboutus_activity_layout_return, aboutus_activity_layout_aboutus,
			aboutus_activity_layout_update, aboutus_activity_layout_version;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_layout_aboutus);
		
		setupAllView();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void setupAllView() {
		aboutus_activity_layout_return = (Button) findViewById(R.id.aboutus_activity_layout_return);
		aboutus_activity_layout_aboutus = (Button) findViewById(R.id.aboutus_activity_layout_aboutus);
		aboutus_activity_layout_update = (Button) findViewById(R.id.aboutus_activity_layout_update);
		aboutus_activity_layout_version = (Button) findViewById(R.id.aboutus_activity_layout_version);

		aboutus_activity_layout_return.setOnClickListener(this);
		aboutus_activity_layout_aboutus.setOnClickListener(this);
		aboutus_activity_layout_update.setOnClickListener(this);
		aboutus_activity_layout_version.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.aboutus_activity_layout_return:
			this.finish();
			break;
			
		case R.id.aboutus_activity_layout_aboutus:
			showTextBoxPop(ABOUT_VIEW);
			break;
			
		case R.id.aboutus_activity_layout_update:
			showTextBoxPop(UPDATE_VIEW);
			break;
			
		case R.id.aboutus_activity_layout_version:
			showTextBoxPop(VERSION_VIEW);
			break;

		default:
			break;
		}

	}
	
	/**
	 * 显示保留视频类型的radio
	 * 
	 * @param view
	 *            单选按钮群
	 */
	View textBoxView;
	PopupWindow textBoxPopWin;
	int ABOUT_VIEW = 1,
			VERSION_VIEW = 3,
			UPDATE_VIEW = 2;
	public void showTextBoxPop(int whoSHow) {
		View rootView = getWindow().getDecorView();
		
		if (textBoxPopWin == null) {
			LayoutInflater inflater = this.getLayoutInflater();
			textBoxView = inflater.inflate(R.layout.text_box, null);

			textBoxPopWin = new PopupWindow(textBoxView, rootView.getWidth()/4*3, rootView.getHeight()/4*3*2);

			textBoxPopWin.setFocusable(true);// 设置焦点
			textBoxPopWin.setOutsideTouchable(true);// 设置弹窗之外触屏时消失
			textBoxPopWin.setBackgroundDrawable(getResources().getDrawable(R.drawable.img_bg_keeplong));

			textBoxPopWin.setAnimationStyle(android.R.style.Animation_InputMethod);

			textBoxPopWin.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
				}
			});
		}

		int x = 0;
		int y = 0;

		textBoxPopWin.showAtLocation(rootView, Gravity.CENTER, x, y);
	}
}
