package net.carslink.widget;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.carslink.activity.AboutUsActivity;
import net.carslink.activity.LoginRegActivity;
import net.carslink.activity.R;
import net.carslink.asynctask.LogoutTask;
import net.carslink.util.SharedSetting;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class Setting extends ListView {

    List<HashMap<String, String>> settingData = null;
    SimpleAdapter settingAdapter = null;
    Context c = null;
    
    public Setting(Context context) {
        super(context);
        c = context;
    }
    
    public Setting(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;
        this.refreshSettingData();
        settingAdapter = new SimpleAdapter(c, settingData,
                R.layout.lv_item_setting, new String[] { "title", "info" },
                new int[] { R.id.editText1, R.id.editText2 });

        this.setAdapter(settingAdapter);
        this.setOnItemClickListener(new OnItemClickListenerImpl());
    }
    
    public void refreshSettingListData() {
        refreshSettingData();
        settingAdapter.notifyDataSetChanged();
    }

    private class OnItemClickListenerImpl implements OnItemClickListener {
        @SuppressWarnings("unchecked")
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            HashMap<String, String> map = (HashMap<String, String>) settingAdapter
                    .getItem(position);
            final String title = map.get("title");
            if(title.equals(c.getString(R.string.setting_title_sim))){
            	final String deviceId = SharedSetting.getDeviceId();
            	if(deviceId.equals("13912341230")){
            		Intent intent = new Intent(c, LoginRegActivity.class);
            		c.startActivity(intent);
            		return;
            	}else{
            		new AlertDialog.Builder(c)
            		.setTitle("您已登录")
            		.setMessage("账户："+ deviceId)
            		.setPositiveButton("返回", null)
            		.setNegativeButton("注销", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new LogoutTask(c, deviceId).execute();
						}
					})
					.create()
					.show();
            	}
            	/*final EditText inputServer = new EditText(c);
                inputServer.setInputType(InputType.TYPE_CLASS_NUMBER);
                inputServer.setText(SharedSetting.getDeviceId());
                
                AlertDialog.Builder builder = new AlertDialog.Builder(c);
                builder.setTitle(title).setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                    .setNegativeButton("取消", null);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedSetting.setDeviceId(inputServer.getText().toString());
                        Toast.makeText(c, "Device ID is set.",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        refreshSettingListData();
                        ConnectivityManager connectivityManager = 
                                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo info = connectivityManager.getActiveNetworkInfo();  
                        if(info != null && info.isAvailable()) {
                            PushManager.startWork(c,
                                PushConstants.LOGIN_TYPE_API_KEY,
                                Utils.getMetaValue(c, "api_key"));
                        }
                    }
                });
                builder.show();*/
            }else if(title.equals(c.getString(R.string.setting_title_storage))){//获取设备存储信息
            	GetStorageFiles gsi = new GetStorageFiles(c);
            	
            	int systemVersion = android.os.Build.VERSION.SDK_INT;
            	boolean haveSD = gsi.externalMemoryAvailable();
            	double availableInternalMemorySize,
            		   totalInternalMemorySize,
            		   availableExternalMemorySize,
            		   totalExternalMemorySize,
            		   appAvailableSize;
            	
            	if(haveSD){
            		if(systemVersion < 18){
            			availableInternalMemorySize = gsi.getAvailableInternalMemorySizeLow();
                		totalInternalMemorySize = gsi.getTotalInternalMemorySizeLow();
                		availableExternalMemorySize = gsi.getAvailableExternalMemorySizeLow();
                		totalExternalMemorySize = gsi.getTotalExternalMemorySizeLow();
            		}else{
            			availableInternalMemorySize = gsi.getAvailableInternalMemorySize();
                		totalInternalMemorySize = gsi.getTotalInternalMemorySize();
                		availableExternalMemorySize = gsi.getAvailableExternalMemorySize();
                		totalExternalMemorySize = gsi.getTotalExternalMemorySize();
            		}
            		
            		appAvailableSize = availableExternalMemorySize-(SharedSetting.getMinFreeSpace()/1024);
            		
            		if(appAvailableSize<0){
            			appAvailableSize=0;
            			
            		}
            		
            		String info = "内部存储空间：" + switchSpaceSize(totalInternalMemorySize) + "\n"
      					  + "剩余空间：" + switchSpaceSize(availableInternalMemorySize) + "\n"
      					  + "外部存储空间：" + switchSpaceSize(totalExternalMemorySize) + "\n"
      					  + "剩余空间：" + switchSpaceSize(availableExternalMemorySize) + "\n"
      					  + "当前程序可用空间：" + switchSpaceSize(appAvailableSize);
      		
            		new AlertDialog.Builder(c)
				                .setTitle(title)
				                .setMessage(info)
				                .setNegativeButton("确定", null)
				                .show();
            		
            	}else{
            		Toast.makeText(c, "没有外部存储卡", Toast.LENGTH_SHORT).show();
            	}
            }else if(title.equals(c.getString(R.string.setting_title_about))){
            	Intent intent = new Intent(c, AboutUsActivity.class);
        		c.startActivity(intent);
            }else{
                SettingOptionResult settingOptionResult = getSettingOption(title);
                new AlertDialog.Builder(c)
                    .setTitle("请设置" + title)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setSingleChoiceItems(settingOptionResult.options,
                            settingOptionResult.checkedKey,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    commitSetting(title, which);
                                    dialog.dismiss();
                                }
                            }).setNegativeButton("取消", null).show();
            }
        }
    }

    public String switchSpaceSize(double valus){
    	DecimalFormat df = new DecimalFormat("###.00");
    	
    	if(valus<1024){
    		return df.format(valus) + " byte";
    	}else if(1024<=valus && valus<1048576){
    		valus = valus/1024;
    		return df.format(valus) + " kb";
    	}else if(1048576<=valus && valus<1073741824 ){
    		valus = valus/1024/1024;
    		return df.format(valus) + " MB";
    	}else if(1073741824<=valus){
    		valus = valus/1024/1024/1024;
    		return df.format(valus) + " GB";
    	}
    	return 0 + "byte";
    }
    
    private void commitSetting(String title, int which) {
        Log.i("DEBUG", title + " " + which);
        if (title.equals(c.getString(R.string.setting_title_auto_start))) {
            boolean curSetting = SharedSetting.getAutoStart();
            switch (which) {
            case 0:
                if (curSetting == false) {
                    SharedSetting.setAutoStart(true);
                    refreshSettingListData();
                }
                break;
            case 1:
                if (curSetting == true) {
                    SharedSetting.setAutoStart(false);
                    refreshSettingListData();
                }
                break;
            }
        } else if (title.equals(c.getString(R.string.setting_title_fenbianlv))) {
            int fenbianlv = SharedSetting.getFenBianLv();
            switch (which) {
            case 0:
                if (fenbianlv != 720) {
                    SharedSetting.setFenBianLv(720);
                    refreshSettingListData();
                }
                break;
            case 1:
                if (fenbianlv != 1080) {
                    SharedSetting.setFenBianLv(1080);
                    refreshSettingListData();
                }
                break;
            }
        } else if (title.equals(c.getString(R.string.setting_title_autocarfrontpic))) {
            switch (which) {
            case 0:
                SharedSetting.setAutoBackPicture(true);
                refreshSettingListData();
                break;
            case 1:
                SharedSetting.setAutoBackPicture(false);
                refreshSettingListData();
                break;
            }
        }else if (title.equals(c.getString(R.string.setting_title_videoduration))) {
            switch (which) {
            case 0:
                SharedSetting.setVideoDuration(60*1000);
                refreshSettingListData();
                break;
            case 1:
                SharedSetting.setVideoDuration(3*60*1000);
                refreshSettingListData();
                break;
            case 2:
                SharedSetting.setVideoDuration(5*60*1000);
                refreshSettingListData();
                break;
            }
        } else if (title.equals(c.getString(R.string.setting_title_videofoldersize))) {
            switch (which) {
            case 0:
                SharedSetting.setTempFolderSize(100*1024);
                refreshSettingListData();
                break;
            case 1:
                SharedSetting.setTempFolderSize(600*1024);
                refreshSettingListData();
                break;
            case 2:
                SharedSetting.setTempFolderSize(1000*1024);
                refreshSettingListData();
                break;
            case 3:
                SharedSetting.setTempFolderSize(5000*1024);
                refreshSettingListData();
                break;
            }
        }
        
        

        //TODO
        else {
        }
    }

    private void refreshSettingData() {
        if (settingData == null)
            settingData = new ArrayList<HashMap<String, String>>();
        else
            settingData.clear();
        String title = "";
        
        HashMap<String, String> sim = new HashMap<String, String>();
        title = c.getString(R.string.setting_title_sim);
        sim.put("title", title);
        String dId = SharedSetting.getDeviceId();
        String dInfo = "未设置";
        if(!dId.equals("13912341230")){
        	dInfo = dId;
        }
        sim.put("info", dInfo);
        settingData.add(sim);
        
        HashMap<String, String> autoStartRecord = new HashMap<String, String>();
        title = c.getString(R.string.setting_title_auto_start);
        autoStartRecord.put("title", title);
        autoStartRecord.put("info",
                getCurrentSetting(R.string.setting_title_auto_start).info);
        settingData.add(autoStartRecord);

        HashMap<String, String> recordFenBianlv = new HashMap<String, String>();
        title = c.getString(R.string.setting_title_fenbianlv);
        recordFenBianlv.put("title", title);
        recordFenBianlv.put("info",
                getCurrentSetting(R.string.setting_title_fenbianlv).info);
        settingData.add(recordFenBianlv);
        
        HashMap<String, String> autoCarFrontPicture = new HashMap<String, String>();
        title = c.getString(R.string.setting_title_autocarfrontpic);
        autoCarFrontPicture.put("title", title);
        autoCarFrontPicture.put("info",
                getCurrentSetting(R.string.setting_title_autocarfrontpic).info);
        settingData.add(autoCarFrontPicture);
       
        HashMap<String, String> videoDuration = new HashMap<String, String>();
        title = c.getString(R.string.setting_title_videoduration);
        videoDuration.put("title", title);
        videoDuration.put("info",
                getCurrentSetting(R.string.setting_title_videoduration).info);
        settingData.add(videoDuration);
        
        HashMap<String, String> folderSize = new HashMap<String, String>();
        title = c.getString(R.string.setting_title_videofoldersize);
        folderSize.put("title", title);
        folderSize.put("info",
                getCurrentSetting(R.string.setting_title_videofoldersize).info);
        settingData.add(folderSize);

        HashMap<String, String> storageInfo = new HashMap<String, String>();//查看设备存储信息
        title = c.getString(R.string.setting_title_storage);
        storageInfo.put("title", title);
        storageInfo.put("info","点击查看");
        settingData.add(storageInfo);
        
        HashMap<String, String> about = new HashMap<String, String>();//查看设备存储信息
        title = c.getString(R.string.setting_title_about);
        about.put("title", title);
        about.put("info","点击了解");
        settingData.add(about);
        
        //TODO
        return;
    }

    private SettingOptionResult getSettingOption(String title) {
        SettingOptionResult result = new SettingOptionResult();
        if (title.equals(c.getString(R.string.setting_title_auto_start))) {
            result.checkedKey = getCurrentSetting(R.string.setting_title_auto_start).checkedKey;
            result.options = new String[] { "开启", "关闭" };
        } else if (title.equals(c.getString(R.string.setting_title_fenbianlv))) {
            result.checkedKey = getCurrentSetting(R.string.setting_title_fenbianlv).checkedKey;
            result.options = new String[] { "720P", "1080P" };
        } else if (title.equals(c.getString(R.string.setting_title_autocarfrontpic))) {
            result.checkedKey = getCurrentSetting(R.string.setting_title_autocarfrontpic).checkedKey;
            result.options = new String[] { "自动拍摄", "不拍摄" };
        }else if (title.equals(c.getString(R.string.setting_title_videoduration))) {
            result.checkedKey = getCurrentSetting(R.string.setting_title_videoduration).checkedKey;
            result.options = new String[] { "1分钟", "3分钟", "5分钟"};
        }else if (title.equals(c.getString(R.string.setting_title_videofoldersize))) {
            result.checkedKey = getCurrentSetting(R.string.setting_title_videofoldersize).checkedKey;
            result.options = new String[] { "100M", "600M", "1G", "5G"};
        }else {
            result.checkedKey = 0;
            result.options = new String[] { "option 1", "option 2", "option 3",
                    "option 4" };
        }
        return result;
    }

    private CurrentSetting getCurrentSetting(int title) {
        CurrentSetting currentSetting = new CurrentSetting();

        if (title == R.string.setting_title_auto_start) {
            boolean curSetting = SharedSetting.getAutoStart();
            if (curSetting == false) {
                currentSetting.checkedKey = 1;
                currentSetting.info = "已禁用ֹ";
            } else {
                currentSetting.checkedKey = 0;
                currentSetting.info = "已开启";
            }
        } else if (title == R.string.setting_title_fenbianlv) {
            // VIDEO_WIDTH = 1280; 1920;
            // VIDEO_HEIGHT = 720; 1080;
            int fenbianlv = SharedSetting.getFenBianLv();

            if (fenbianlv == 720) {
                currentSetting.checkedKey = 0;
                currentSetting.info = "720P";
            } else if (fenbianlv == 1080) {
                currentSetting.checkedKey = 1;
                currentSetting.info = "1080P";
            }
        } else if(title == R.string.setting_title_autocarfrontpic){
            boolean autotake = SharedSetting.getAutoBackPicture();
            if(autotake){
                currentSetting.checkedKey = 0;
                currentSetting.info = "自动拍摄";
            }else{
                currentSetting.checkedKey = 1;
                currentSetting.info = "不拍摄";
            }
        }else if(title == R.string.setting_title_videoduration){
            int duration = SharedSetting.getVideoDuration();
            switch(duration){
            case 60000:
                currentSetting.checkedKey = 0;
                currentSetting.info = "1分钟";
                break;
            case 180000:
                currentSetting.checkedKey = 1;
                currentSetting.info = "3分钟";
                break;
            case 300000:
                currentSetting.checkedKey = 2;
                currentSetting.info = "5分钟";
                break;
            }
        }else if(title == R.string.setting_title_videofoldersize){
            int freeSpace = SharedSetting.getTempFolderSize();
            switch(freeSpace){
            case 100*1024:
                currentSetting.checkedKey = 0;
                currentSetting.info = "100M";
                break;
            case 600*1024:
                currentSetting.checkedKey = 1;
                currentSetting.info = "600M";
                break;
            case 1000*1024:
                currentSetting.checkedKey = 2;
                currentSetting.info = "1G";
                break;
            case 5000*1024:
                currentSetting.checkedKey = 3;
                currentSetting.info = "5G";
                break;
            }
        }else {
            currentSetting.checkedKey = 0;
            currentSetting.info = "title not found.";
        }
        return currentSetting;
    }
	
    private class CurrentSetting {
        public int checkedKey;
        public String info;
    }

    private class SettingOptionResult {
        public int checkedKey;
        public String[] options;
    }
}
