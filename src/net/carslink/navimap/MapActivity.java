package net.carslink.navimap;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.carslink.activity.R;

public class MapActivity extends Activity {
    // 地图和导航核心逻辑类
    private AMap mAmap;
    private AMapNavi mAmapNavi;
    private AMapNaviPath mNaviPath;
    private LocationManagerProxy mLocationManger;
    private LocationSource.OnLocationChangedListener mListener;
    private PoiSearch.Query mQuery;
    private PoiSearch mPoiSearch;
    private PoiResult mPoiResult;
    private AMapNaviListener mAmapNaviListener;
    private RouteOverLay mRouteOverLay;
    private GeocodeSearch mGeocodeSearch;

    // View
    private MapView mMapView;// 地图控件
    private ProgressDialog mGPSProgressDialog;// GPS过程显示状态
    private ProgressDialog mProgressDialog;// 路径规划过程显示状态

    private EditText et_des;
    private Button btn_back, btn_navi;
    private RelativeLayout history_view;
    private EditText et_search;
    private ListView lv_search, lv_history;
    private AutoCompleteTextView act_strategy;

    // 变量
    private SimpleAdapter searchAdapter = null;
    private MapListAdapter historyAdapter = null;
    List<HashMap<String, Object>> searchData = new ArrayList<HashMap<String, Object>>();
    List<HashMap<String, Object>> historyData = new ArrayList<HashMap<String, Object>>();
    private String mCurrentCity = "";
    private String[] mStrategyMethods = null;
    private boolean isFirst = true;
    private boolean isWeChatNavi = false;
    public static boolean isActive = false;

    // 驾车路径规划起点，途经点，终点的list
    private List<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
    private List<NaviLatLng> mWayPoints = new ArrayList<NaviLatLng>();
    private List<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
    // 记录起点、终点、途经点位置
    private NaviLatLng mStartPoint = new NaviLatLng();
    private NaviLatLng mEndPoint = new NaviLatLng();
    private NaviLatLng mWayPoint = new NaviLatLng();

    //历史记录数据库
    private HistoryDB mHistoryDB;
    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction("net.carslink.dashcam.Navigation");
        registerReceiver(broadcastReceiver, filter);
        TTSController ttsManager = TTSController.getInstance(this);// 初始化语音模块
        ttsManager.init();
        AMapNavi.getInstance(this).setAMapNaviListener(ttsManager);// 设置语音模块播报

        setContentView(R.layout.activity_layout_map);
        initView(savedInstanceState);
        initStrategy();
        initListener();
        initList();
        initMapAndNavi();
        setUpMap();
        getBundleExtra();
        isActive = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        // 以上两句必须重写
        // 以下两句逻辑是为了保证进入首页开启定位和加入导航回调
        AMapNavi.getInstance(this).setAMapNaviListener(aMapNaviListener());
        mAmapNavi.startGPS();
        TTSController.getInstance(this).startSpeaking();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        // 以上两句必须重写
        // 下边逻辑是移除监听
        AMapNavi.getInstance(this).removeAMapNaviListener(aMapNaviListener());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        // 这是最后退出页，所以销毁导航和播报资源
        AMapNavi.getInstance(this).destroy();// 销毁导航
        TTSController.getInstance(this).stopSpeaking();
        TTSController.getInstance(this).destroy();
        unregisterReceiver(broadcastReceiver);
        isActive = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (history_view.getVisibility() == View.VISIBLE) {
                history_view.setVisibility(View.INVISIBLE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 初始化界面所需View控件
     *
     * @param savedInstanceState
     */
    private void initView(Bundle savedInstanceState) {
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mAmap = mMapView.getMap();
        mRouteOverLay = new RouteOverLay(mAmap, null);

        et_des = (EditText) findViewById(R.id.et_des);
        et_des.setInputType(InputType.TYPE_NULL);
        et_search = (EditText) findViewById(R.id.et_search);

        act_strategy = (AutoCompleteTextView) findViewById(R.id.act_strategy);

        lv_search = (ListView) findViewById(R.id.search_list);
        lv_history = (ListView) findViewById(R.id.history_list);

        btn_back = (Button) findViewById(R.id.back_btn);
        btn_navi = (Button) findViewById(R.id.navi_btn);

        history_view = (RelativeLayout) findViewById(R.id.history_view);
    }

    /**
     * 初始化搜索列表和历史记录列表
     */
    private void initList() {
        // 动态数组数据源中与ListItem中每个显示项对应的Key
        String[] from = new String[]{"itemTitle", "itemInfo"};
        // ListItem的XML文件里面的两个TextView ID
        int[] to = new int[]{R.id.title, R.id.info};

        // 将动态数组数据源data中的数据填充到ListItem的XML文件list_item.xml中去
        // 从动态数组数据源data中，取出from数组中key对应的value值，填充到to数组中对应ID的控件中去
        searchAdapter = new SimpleAdapter(this, searchData, R.layout.lv_item_map, from, to);
        historyAdapter = new MapListAdapter(this, historyData, R.layout.lv_item_map, from, to);
        lv_history.setAdapter(historyAdapter);

        mHistoryDB = new HistoryDB(this);
        mCursor = mHistoryDB.select();
        showHistory();
    }

    /**
     * 初始化线路策略
     */
    private void initStrategy() {
        Resources res = getResources();
        mStrategyMethods = new String[]{
                res.getString(R.string.navi_strategy_speed),
                res.getString(R.string.navi_strategy_cost),
                res.getString(R.string.navi_strategy_distance),
                res.getString(R.string.navi_strategy_nohighway),
                res.getString(R.string.navi_strategy_timenojam),
                res.getString(R.string.navi_strategy_costnojam)
        };

        act_strategy.setDropDownBackgroundResource(R.drawable.shape_whiteborder);
        act_strategy.setInputType(InputType.TYPE_NULL);
        ArrayAdapter<String> strategyAdapter = new ArrayAdapter<String>(this, R.layout.strategy_inputs, mStrategyMethods);
        act_strategy.setAdapter(strategyAdapter);
    }

    /**
     * 初始化所需监听
     */
    private void initListener() {
        //输入框文字变化监听
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchData.clear();
                if (s.length() <= 0) {
                    lv_history.setVisibility(View.VISIBLE);
                    mRouteOverLay.removeFromMap();
                    return;
                }
                doSearchQuery(s.toString());
                lv_history.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                et_des.setText(et_search.getText());
            }
        });

        //地址提示列表内容点击
        lv_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = mPoiResult.getPois().get(position).getTitle();
                String snippet = mPoiResult.getPois().get(position).getSnippet();
                LatLonPoint latLonPoint = mPoiResult.getPois().get(position).getLatLonPoint();

                et_search.setText(title);

                mEndPoint = new NaviLatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
                mEndPoints.clear();
                mEndPoints.add(mEndPoint);
                mStartPoints.clear();
                mStartPoints.add(mStartPoint);
                calculateRoute();
                addHistory(title, snippet, latLonPoint.getLatitude(), latLonPoint.getLongitude());
                showHistory();
            }
        });

        //搜索结果列表触碰收起键盘
        lv_search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                return false;
            }
        });

        //历史记录列表内容点击
        lv_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == historyData.size() - 1) {
                    deleteHistory();
                    showHistory();
                    return;
                }
                String title = historyData.get(position).get("itemTitle").toString();
                et_search.setText(title);
                double lat = Double.parseDouble(historyData.get(position).get("latitude").toString());
                double lng = Double.parseDouble(historyData.get(position).get("longitude").toString());
                mEndPoint = new NaviLatLng(lat, lng);
                mEndPoints.clear();
                mEndPoints.add(mEndPoint);
                mStartPoints.clear();
                mStartPoints.add(mStartPoint);
                calculateRoute();
            }
        });

        //历史记录列表触碰收起键盘
        lv_history.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                return false;
            }
        });

        //路线规划策略点击
        act_strategy.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                act_strategy.showDropDown();
                return false;
            }
        });

        //输入框点击
        et_des.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                history_view.setVisibility(View.VISIBLE);
                et_search.requestFocus();
                et_search.setSelection(et_search.getText().length());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(et_search, InputMethodManager.SHOW_FORCED);
            }
        });

        //返回按钮点击
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNaviPath = null;
                et_search.setText("");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                history_view.setVisibility(View.INVISIBLE);
            }
        });

        //导航按钮点击
        btn_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNaviPath == null){
                    showToast(getString(R.string.no_destination));
                    return;
                }
                Intent intent = new Intent(MapActivity.this, NavigatorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                et_search.setText("");
            }
        });
    }

    /**
     * 初始化地图和导航相关内容
     */
    private void initMapAndNavi() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);// 设置声音控制
        mAmapNavi = AMapNavi.getInstance(this);// 初始化导航引擎
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.img_point));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 175, 236, 255));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        mAmap.setMyLocationStyle(myLocationStyle);
        mAmap.setLocationSource(mLocationSource);// 设置定位监听
        mAmap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAmap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // aMap.setMyLocationType()
    }

    /**
     * 微信端发起导航，获取目的坐标
     */
    private void getBundleExtra() {
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            mEndPoint = new NaviLatLng(extra.getDouble("lat"), extra.getDouble("lng"));
            mEndPoints.clear();
            mEndPoints.add(mEndPoint);
            isWeChatNavi = true;
            getAddress(new LatLonPoint(extra.getDouble("lat"), extra.getDouble("lng")));
        }
    }

    //定位
    private AMapLocationListener mLocationListener = new AMapLocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLocationChanged(AMapLocation location) {
            mCurrentCity = location.getCity();
            mStartPoint = new NaviLatLng(location.getLatitude(), location.getLongitude());
            if (mListener != null && location != null) {
                mListener.onLocationChanged(location);// 显示系统小蓝点
            }
            if (isFirst){
                isFirst = false;
                mAmap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
            }
            dismissGPSProgressDialog();
            //微信发起导航
            if (isWeChatNavi){
                isWeChatNavi = false;
                mStartPoints.clear();
                mStartPoints.add(mStartPoint);
                calculateRoute();
            }

        }
    };

    private LocationSource mLocationSource = new LocationSource() {
        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            showGPSProgressDialog();
            isFirst = true;
            mListener = onLocationChangedListener;
            if (mLocationManger == null) {
                mLocationManger = LocationManagerProxy.getInstance(getApplication());
                mLocationManger.setGpsEnable(true);
                mLocationManger.requestLocationData(LocationProviderProxy.AMapNetwork, 2000, 10, mLocationListener);
            }
        }

        @Override
        public void deactivate() {
            mListener = null;
            if (mLocationManger != null) {
                mLocationManger.removeUpdates(mLocationListener);
                mLocationManger.destroy();
            }
            mLocationManger = null;

        }
    };

    //poi搜索
    protected void doSearchQuery(String keyWord) {
        mQuery = new PoiSearch.Query(keyWord, "", mCurrentCity);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        mQuery.setPageSize(20);// 设置每页最多返回多少条poiitem
        mQuery.setPageNum(0);// 设置查第一页

        mPoiSearch = new PoiSearch(this, mQuery);
        mPoiSearch.setOnPoiSearchListener(poiSearchListener);
        mPoiSearch.searchPOIAsyn();
    }

    //poi搜索结果回调
    private PoiSearch.OnPoiSearchListener poiSearchListener = new PoiSearch.OnPoiSearchListener() {
        @Override
        public void onPoiSearched(PoiResult result, int rCode) {
            if (rCode == 0) {
                if (result != null && result.getQuery() != null) {// 搜索poi的结果
                    if (result.getQuery().equals(mQuery)) {// 是否是同一条
                        mPoiResult = result;
                        // 取得搜索到的poiitems有多少页
                        List<PoiItem> poiItems = mPoiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                        List<SuggestionCity> suggestionCities = mPoiResult.getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                        if (poiItems != null && poiItems.size() > 0) {
                            for (PoiItem poiItem : poiItems) {
                                HashMap<String, Object> map = new HashMap<String, Object>();
                                map.put("itemTitle", poiItem.getTitle());
                                map.put("itemInfo", poiItem.getSnippet());
                                searchData.add(map);
                                Log.i("hhj", "name : " + poiItem.getTitle());
                            }
                            lv_search.setAdapter(searchAdapter);
                        } else if (suggestionCities != null && suggestionCities.size() > 0) {
//                            showSuggestCity(suggestionCities);
                            showToast(getString(R.string.no_result));
                        } else {
                            showToast(getString(R.string.no_result));
                        }
                    }
                } else {
                    showToast(getString(R.string.no_result));
                }
            } else if (rCode == 27) {
//                ToastUtil.show(PoiKeywordSearchActivity.this, R.string.error_network);
            } else if (rCode == 32) {
//                ToastUtil.show(PoiKeywordSearchActivity.this, R.string.error_key);
            } else {
//                ToastUtil.show(PoiKeywordSearchActivity.this, getString(R.string.error_other) + rCode);
            }
        }

        @Override
        public void onPoiItemDetailSearched(PoiItemDetail poiItemDetail, int i) {

        }
    };

    //规划路线
    private void calculateRoute() {
        showProgressDialog();
        int driveMode = getDriveMode();
        if (mAmapNavi.calculateDriveRoute(mStartPoints, mEndPoints, mWayPoints, driveMode)) {
            history_view.setVisibility(View.INVISIBLE);
        } else {

        }
    }

    //线路策略
    private int getDriveMode() {
        String strategyMethod = act_strategy.getText().toString();
        // 速度优先
        if (mStrategyMethods[0].equals(strategyMethod)) {
            return AMapNavi.DrivingDefault;
        }
        // 花费最少
        else if (mStrategyMethods[1].equals(strategyMethod)) {
            return AMapNavi.DrivingSaveMoney;

        }
        // 距离最短
        else if (mStrategyMethods[2].equals(strategyMethod)) {
            return AMapNavi.DrivingShortDistance;
        }
        // 不走高速
        else if (mStrategyMethods[3].equals(strategyMethod)) {
            return AMapNavi.DrivingNoExpressways;
        }
        // 时间最短且躲避拥堵
        else if (mStrategyMethods[4].equals(strategyMethod)) {
            return AMapNavi.DrivingFastestTime;
        } else if (mStrategyMethods[5].equals(strategyMethod)) {
            return AMapNavi.DrivingAvoidCongestion;
        } else {
            return AMapNavi.DrivingDefault;
        }
    }

    //导航回调函数
    private AMapNaviListener aMapNaviListener() {
        if (mAmapNaviListener == null) {
            mAmapNaviListener = new AMapNaviListener() {
                @Override
                public void onTrafficStatusUpdate() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onStartNavi(int arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onReCalculateRouteForYaw() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onReCalculateRouteForTrafficJam() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onLocationChange(AMapNaviLocation location) {

                }

                @Override
                public void onInitNaviSuccess() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onInitNaviFailure() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onGetNavigationText(int arg0, String arg1) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onEndEmulatorNavi() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onCalculateRouteSuccess() {
                    dismissProgressDialog();
                    mNaviPath = mAmapNavi.getNaviPath();
                    if (mNaviPath == null) {
                        return;
                    }
                    // 获取路径规划线路，显示到地图上
                    mRouteOverLay.setRouteInfo(mNaviPath);
                    mRouteOverLay.addToMap();
                    mRouteOverLay.zoomToSpan();
                }

                @Override
                public void onCalculateRouteFailure(int arg0) {
                    dismissProgressDialog();
                    showToast(getString(R.string.wrong_route));
                }

                @Override
                public void onArrivedWayPoint(int arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onArriveDestination() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onGpsOpenStatus(boolean arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onNaviInfoUpdated(AMapNaviInfo arg0) {
                    // TODO Auto-generated method stub

                }
            };
        }
        return mAmapNaviListener;
    }

    private void getAddress(LatLonPoint latLonPoint){
        mGeocodeSearch = new GeocodeSearch(this);
        mGeocodeSearch.setOnGeocodeSearchListener(geocodeSearchListener);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        mGeocodeSearch.getFromLocationAsyn(query);
    }

    private GeocodeSearch.OnGeocodeSearchListener geocodeSearchListener = new GeocodeSearch.OnGeocodeSearchListener() {
        @Override
        public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
            if (rCode == 0) {
                if (result != null && result.getRegeocodeAddress() != null && result.getRegeocodeAddress().getFormatAddress() != null) {
                    String addressName = result.getRegeocodeAddress().getFormatAddress();
//                    String addressName = result.getRegeocodeAddress().getPois().get(0).getTitle();
                    et_des.setText(addressName);
                } else {
                    showToast(getString(R.string.no_result));
                }
            } else if (rCode == 27) {
//                ToastUtil.show(GeocoderActivity.this, R.string.error_network);
            } else if (rCode == 32) {
//                ToastUtil.show(GeocoderActivity.this, R.string.error_key);
            } else {

            }
        }

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {

        }
    };

    // ---------------UI操作----------------

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (mProgressDialog == null)
            mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setMessage("线路规划中");
        mProgressDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 显示GPS进度框
     */
    private void showGPSProgressDialog() {
        if (mGPSProgressDialog == null)
            mGPSProgressDialog = new ProgressDialog(this);
        mGPSProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mGPSProgressDialog.setIndeterminate(false);
        mGPSProgressDialog.setCancelable(true);
        mGPSProgressDialog.setMessage("定位中...");
        mGPSProgressDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dismissGPSProgressDialog() {
        if (mGPSProgressDialog != null) {
            mGPSProgressDialog.dismiss();
        }
    }

    //历史记录
    private void addHistory(String title, String snippet, double lat, double lng) {
        if (mCursor.moveToFirst()) {
            for (int i = 0; i < mCursor.getCount(); i++) {
                mCursor.moveToPosition(i);
                if (mCursor.getString(mCursor.getColumnIndex("title")).equals(title) && mCursor.getString(mCursor.getColumnIndex("snippet")).equals(snippet))
                    return;
            }
        }
        mHistoryDB.insert(title, snippet, lat, lng);
        if (mCursor.getCount() >= 10){
            mCursor.moveToPosition(mCursor.getCount() - 1);
            mHistoryDB.delete(mCursor.getInt(mCursor.getColumnIndex("id")));
        }
    }

    private void showHistory() {
        mCursor = mHistoryDB.select();
        historyData.clear();
        if (mCursor.moveToFirst()) {
            for (int i = 0; i < mCursor.getCount(); i++) {
                mCursor.moveToPosition(i);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("itemTitle", mCursor.getString(mCursor.getColumnIndex("title")));
                map.put("itemInfo", mCursor.getString(mCursor.getColumnIndex("snippet")));
                map.put("latitude", mCursor.getDouble(mCursor.getColumnIndex("latitude")));
                map.put("longitude", mCursor.getDouble(mCursor.getColumnIndex("longitude")));
                historyData.add(map);
            }
        }
        if (historyData.size() > 0) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("title", "清空历史记录");
            historyData.add(map);
        }
        historyAdapter.notifyDataSetChanged();
    }

    private void deleteHistory() {
        historyData.clear();
        mHistoryDB.truncate();
    }

    //微信导航广播
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("hhj","navigation broadcast");
            mNaviPath = null;
            String extra = intent.getStringExtra("extra");
            String[] latlng = extra.split(",");
            double lng, lat;
            lat = Double.parseDouble(latlng[0]);
            lng = Double.parseDouble(latlng[1]);
            mEndPoint = new NaviLatLng(lat, lng);
            mEndPoints.clear();
            mEndPoints.add(mEndPoint);
            mStartPoints.clear();
            mStartPoints.add(mStartPoint);
            calculateRoute();
            getAddress(new LatLonPoint(lat, lng));
        }
    };

}
