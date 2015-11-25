package net.carslink.adapter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.carslink.activity.R;
import net.carslink.widget.GetStorageFiles;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint({ "NewApi", "ViewHolder" })
public class FilesAdapter extends BaseAdapter {
	private Context context;
	private File[] files;
	private AssetManager asm;
	private HashMap<String, Drawable> showIco = null;//顯示圖標
	
	private int FILE_ITEM_LAYOUT = R.layout.lv_item_file;
	private LayoutInflater inflater;
	
	public FilesAdapter(Context context,File[] files) {
		this.context = context;
		this.files = files;
		
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		showIco = new HashMap<String, Drawable>();
		asm = this.context.getAssets();
		try {
			showIco.put("folder", Drawable.createFromStream(asm.open("showico/folder.png"), null));
			showIco.put("folder_empty", Drawable.createFromStream(asm.open("showico/folder_empty.png"), null));
			showIco.put("unknown", Drawable.createFromStream(asm.open("showico/unknown.png"), null));
			showIco.put("folder_interim", Drawable.createFromStream(asm.open("showico/folder_interim.png"), null));
			showIco.put("folder_long", Drawable.createFromStream(asm.open("showico/folder_long.png"), null));
			showIco.put("picture_ico", Drawable.createFromStream(asm.open("showico/picture_ico.png"), null));
			showIco.put("video_mp4", Drawable.createFromStream(asm.open("showico/video_mp4.png"), null));
			showIco.put("img_jpg", Drawable.createFromStream(asm.open("showico/img_jpg.png"), null));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return files.length;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}


	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		View v = inflater.inflate(FILE_ITEM_LAYOUT, parent, false);;
		
		File item = files[pos];
		
		if(!item.exists())
			return null;
		
		ImageView fileIcoView = (ImageView)v.findViewById(R.id.file_item_ico);
		TextView fileNameView = (TextView)v.findViewById(R.id.file_item_name);
		
		String fileName = item.getName();
		//String filePath = item.getPath();
		
		if(item.isDirectory()){
			
			String folderName = item.getName();
			if(folderName.equals("临时录像")){
				setFileIco(fileIcoView, showIco.get("folder_interim"));
			}else if(folderName.equals("保留录像")){
				setFileIco(fileIcoView, showIco.get("folder_long"));
			}else if(folderName.equals("车前照片")){
				setFileIco(fileIcoView, showIco.get("picture_ico"));
			}else{
				setFileIco(fileIcoView, showIco.get("folder_interim"));
			}
				
			
		}else{
			String type = GetStorageFiles.getFileType(fileName);//获取文件类型
			if(type.equals("mp4")){
				/*Bitmap bitmap = Bitmap.createBitmap(150, 150,Bitmap.Config.ARGB_8888);
				bitmap = getVideoThumbnail(item.getPath(),150,150,MediaStore.Images.Thumbnails.MICRO_KIND);*/
				setFileIco(fileIcoView, showIco.get("video_mp4"));
			}else if(type.equals("jpeg") || type.equals("png") || type.equals("jpg")){
				setFileIco(fileIcoView, showIco.get("img_jpg"));
			}else{
				setFileIco(fileIcoView, showIco.get("unknown"));
			}
		}
		
		fileNameView.setText(item.getName());
		
		return v;
	}

	@SuppressWarnings("deprecation")
	public void setFileIco(View view, Drawable ico){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackground(ico);
	    } else {
	    	view.setBackgroundDrawable(ico);
	    }
	}
	
	@SuppressWarnings("deprecation")
	public Drawable getBitmapsFromVideo(String filePath) { 
		//MediaMetadataRetriever retriever = new MediaMetadataRetriever(); 
		//retriever.setDataSource(filePath); 
		//取得视频的长度(单位为毫秒) 
		//String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); 
		//取得视频的长度(单位为秒) 
		//int seconds = Integer.valueOf(time) / 1000; 
		//得到最后一秒视频
		Bitmap bitmap = Bitmap.createBitmap(150, 150,Bitmap.Config.ARGB_8888);
		//bitmap = retriever.getFrameAtTime(seconds,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
		
		//bitmap = ThumbnailUtils.extractThumbnail(bitmap, 130, 180, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		bitmap = getVideoThumbnail(filePath,150,150,MediaStore.Images.Thumbnails.MICRO_KIND);
		//bitmap = compressImage(bitmap);
		
/*		int l = bitmap.getByteCount()/1024;
		int w= bitmap.getWidth();
		int h = bitmap.getHeight();*/
		
		Drawable ico = new BitmapDrawable(bitmap);
		ico = (Drawable)ico;
		
		//bitmap.recycle();
		
		return ico;
	}

	private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {  
        Bitmap bitmap = null;  
        // 获取视频的缩略图  
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);  
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,  
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		
        return bitmap;  
    }  
	
}
