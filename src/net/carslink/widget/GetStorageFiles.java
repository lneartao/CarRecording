package net.carslink.widget;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import net.carslink.util.SharedSetting;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class GetStorageFiles {
	private final int ERROR = -1;
	
	private Context context;
	
	private File mainFolder;
	private File[] mainFiles;//当前显示文件列表
	private File parentFolder;
	
	private String currentPath;
	
	private String MAIN_FOLDER = SharedSetting.getAppMainPath();
	
	public GetStorageFiles(Context context){
		this.context = context;
		init();
	}
	
	public File[] getMainFiles(){
		mainFiles =parentFolder.listFiles();//父文件列表
		
		currentPath = parentFolder.getPath();
		
		if(mainFiles.length > 0){
			parentFolder = parentFolder.getParentFile();
		}	
		
		mainFiles = sortingFiles(mainFiles);
		
		return mainFiles;
	}
	
	public String getCurrentPath(){
		return currentPath;
	}
	
	public void init(){
		mainFolder = new File(MAIN_FOLDER);
		mainFiles = mainFolder.listFiles();
		
		mainFiles = sortingFiles(mainFiles);
		
		parentFolder = mainFolder;
	}
	
	/**
	 * 扫描该文件夹下的子文件
	 * @param position 父文件夹的位置
	 * @return 
	 */
	public File[] getChildFiles(int position){
		parentFolder = mainFiles[position].getParentFile();
		currentPath = mainFiles[position].getPath();
		
		mainFiles = mainFiles[position].listFiles();
		mainFiles = sortingFiles(mainFiles);
		
		return mainFiles;
	}
	
	public File[] updateFiles(int position){
		File parentFolder = mainFiles[position].getParentFile();
		mainFiles = parentFolder.listFiles();
		
		mainFiles = sortingFiles(mainFiles);
		
		return mainFiles;
	}
	
	/**
	 * 获取文件类型
	 * @param fileName 文件名
	 * @return
	 */
	public static String getFileType(String fileName){
		if(fileName.equals(""))
			return null;
		
		String type = null;
		
		int point = fileName.lastIndexOf(".")+1;
		type = fileName.substring(point, fileName.length());
		
		return type;
	}
	
	public File[] sortingFiles(File[] files){
		Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(
                        f1.lastModified());
            }
        });
		
		return files;
	}
	
    /**
     * SDCARD是否存在
     */
    public boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }
    
    /**
     * 获取手机内部剩余存储空间
     * 设备系统版本低于18，则调用low方法
     * @return
     */
    public double getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        double blockSize = stat.getBlockSizeLong();
        double availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }
    
    
	public double getAvailableInternalMemorySizeLow() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        double blockSize = stat.getBlockSize();
        double availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * 获取手机内部总的存储空间
     * 设备系统版本低于18，则调用low方法
     * @return
     */
    public double getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        double blockSize = stat.getBlockSizeLong();
        double totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }
    
    public double getTotalInternalMemorySizeLow() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        double blockSize = stat.getBlockSize();
        double totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }
    
    

    /**
     * 获取SDCARD剩余存储空间
     * 设备系统版本低于18，则调用low方法
     * @return
     */
    public double getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            double blockSize = stat.getBlockSizeLong();
            double availableBlocks = stat.getAvailableBlocksLong();
            return availableBlocks * blockSize;
        } else {
            return ERROR;
        }
    }
    
    public double getAvailableExternalMemorySizeLow() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            double blockSize = stat.getBlockSize();
            double availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    /**
     * 获取SDCARD总的存储空间
     * 设备系统版本低于18，则调用low方法
     * @return
     */
    public double getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            double blockSize = stat.getBlockSizeLong();
            double totalBlocks = stat.getBlockCountLong();
            return totalBlocks * blockSize;
        } else {
            return ERROR;
        }
    }
    
    public double getTotalExternalMemorySizeLow() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            double blockSize = stat.getBlockSize();
            double totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return ERROR;
        }
    }
}
