package net.carslink.navimap;

import net.carslink.activity.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;

/**
 * Created by wonghoukit on 2015/4/1 0001.
 */
public class NavigatorActivity extends Activity implements AMapNaviViewListener {
    private AMapNaviView mAmapAMapNaviView;
    public static boolean isNavigating = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_navigator);
        init(savedInstanceState);
        isNavigating = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAmapAMapNaviView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAmapAMapNaviView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mAmapAMapNaviView.onPause();
        AMapNavi.getInstance(this).stopNavi();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAmapAMapNaviView.onDestroy();

        TTSController.getInstance(this).stopSpeaking();
        isNavigating = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    /**
     * 初始化
     *
     * @param savedInstanceState
     */
    private void init(Bundle savedInstanceState) {
        mAmapAMapNaviView = (AMapNaviView) findViewById(R.id.navimap_view);
        mAmapAMapNaviView.onCreate(savedInstanceState);
        mAmapAMapNaviView.setAMapNaviViewListener(this);
        TTSController.getInstance(this).startSpeaking();
        // 开启实时导航
        AMapNavi.getInstance(this).startNavi(AMapNavi.GPSNaviMode);
    }

    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {
        finish();
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }
}
