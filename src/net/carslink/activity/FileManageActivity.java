package net.carslink.activity;

import java.io.File;
import java.text.DecimalFormat;

import net.carslink.adapter.FilesAdapter;
import net.carslink.widget.GetStorageFiles;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

public class FileManageActivity extends Activity implements OnItemClickListener, android.view.View.OnClickListener {
	GetStorageFiles gsi;
	File[] mainFiles;

	GridView list_files_layout_gridview;
	TextView list_files_layout_button_back,
			 list_files_layout_pathtext,
			 list_files_layout_button_menu;
	
	private int DIR_LAYER = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout_filemanage);

		setupView();

		gsi = new GetStorageFiles(FileManageActivity.this);
		mainFiles = gsi.getMainFiles();
		
		FilesAdapter filesAdapter = new FilesAdapter(FileManageActivity.this,mainFiles);

		list_files_layout_gridview.setAdapter(filesAdapter);
		list_files_layout_gridview.setOnItemClickListener(this);
		this.registerForContextMenu(list_files_layout_gridview);
		
		setPathtext(gsi.getCurrentPath());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("操作方式");
		getMenuInflater().inflate(R.menu.contextmenu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int position = ((AdapterContextMenuInfo) item.getMenuInfo()).position;

		switch (item.getItemId()) {
		case R.id.contextmenu_open:
			if (mainFiles[position].isDirectory()) {
				enterNextFolder(position);
			} else {
				openFile(position);
			}
			return true;

		case R.id.contextmenu_delete:
			deleteFolderOrFile(mainFiles[position]);
			updateFilesView(position);
			return true;

		case R.id.contextmenu_rename:
			renameFolderOrFiles(position);
			return true;

		case R.id.contextmenu_attribute:
			getFolderOrFilesAttribute(position);
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	/*
	 * view是当前item的view，通过它可以获得该项中的各个组件。
	 * position是当前item的ID。这个id根据你在适配器中的写法可以自己定义。 id是当前的item在gridview中的相对位置
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mainFiles[position].isDirectory()) {
			enterNextFolder(position);
		} else {
			openFile(position);
		}
	}



	@Override
	public void onClick(View view) {
		switch(view.getId()){
			case R.id.list_files_layout_button_back:
				if(DIR_LAYER != 0){
					backParentFolder();
					//Toast.makeText(FileManageActivity.this, "我是一个子文件夹", Toast.LENGTH_LONG).show();
				}else{
					this.finish();
				}
				break;
				
			/*case R.id.list_files_layout_button_menu:
				break;*/
				
			default:
				break;
		}
		
	}
	
	/**
	 * 设置布局界面
	 */
	public void setupView(){
		list_files_layout_gridview = (GridView) findViewById(R.id.list_files_layout_gridview);
		
		list_files_layout_button_back = (TextView)findViewById(R.id.list_files_layout_button_back);
		list_files_layout_pathtext = (TextView)findViewById(R.id.list_files_layout_pathtext);
		//list_files_layout_button_menu = (TextView)findViewById(R.id.list_files_layout_button_menu);
		
		list_files_layout_button_back.setOnClickListener(this);
		//list_files_layout_button_menu.setOnClickListener(this);
	}
	
	public void setPathtext(String path){
		list_files_layout_pathtext.setText(path);
	}
	
	/**
	 * 进入文件夹
	 * 
	 * @param position
	 */
	public void enterNextFolder(int position) {
		DIR_LAYER++;//进入文件夹添加一层
		
		File[] childFiles = gsi.getChildFiles(position);
		
		FilesAdapter filesAdapter = new FilesAdapter(FileManageActivity.this, childFiles);
		list_files_layout_gridview.setAdapter(filesAdapter);
		mainFiles = childFiles;
		
		setPathtext(gsi.getCurrentPath());
	}

	public void backParentFolder(){
		DIR_LAYER--;
		
		mainFiles = gsi.getMainFiles();
		
		FilesAdapter filesAdapter = new FilesAdapter(FileManageActivity.this, mainFiles);
		list_files_layout_gridview.setAdapter(filesAdapter);

		setPathtext(gsi.getCurrentPath());
	}
	
	public void updateFilesView(int position) {
		mainFiles = gsi.updateFiles(position);
		
		FilesAdapter filesAdapter = new FilesAdapter(FileManageActivity.this, mainFiles);
		list_files_layout_gridview.setAdapter(filesAdapter);
	}

	/**
	 * 打开文件
	 * 
	 * @param position
	 */
	public void openFile(int position) {
		String fileName = mainFiles[position].getName();
		String type = GetStorageFiles.getFileType(fileName);

		if (type.equals("mp4")) {
			Intent videoIntent = new Intent(FileManageActivity.this, VideoPlayActivity.class);
			videoIntent.putExtra("videoPath", mainFiles[position].getAbsolutePath());
			startActivity(videoIntent);
		} else if (type.equals("jpg") || type.equals("png") || type.equals("jpeg")) {
			Intent imageIntent = new Intent(FileManageActivity.this, SeePictureActivity.class);
			imageIntent.putExtra("picturePath", mainFiles[position].getAbsolutePath());
			startActivity(imageIntent);
		} else {
			return;
		}
	}

	/**
	 * 删除文件夹或文件
	 * 
	 * @param file
	 */
	public void deleteFolderOrFile(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}

			for (int i = 0; i < childFiles.length; i++) {
				deleteFolderOrFile(childFiles[i]);
			}
			file.delete();
		}
	}

	public void renameFolderOrFiles(final int position) {
		final String fileName = mainFiles[position].getName();
		final EditText editText_fileName = new EditText(this);

		editText_fileName.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);
		editText_fileName.setText(fileName);

		new AlertDialog.Builder(this).setTitle("请输入")
				.setView(editText_fileName)
				.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface di, int arg1) {
						String newName = editText_fileName.getText().toString();
						File newFile = new File(mainFiles[position]
								.getParentFile() + File.separator + newName);
						mainFiles[0].renameTo(newFile);
						updateFilesView(position);
					}
				}).setNegativeButton("取消", null).show();
	}

	public void getFolderOrFilesAttribute(int position) {
		String allInfo = "";
		
		String fileName = mainFiles[position].getName();
		allInfo += "文件名：" + fileName + "\n";
		
		if (!mainFiles[position].isDirectory()) {
			String fileType = GetStorageFiles.getFileType(fileName);
			String MINEType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileType);
			String fileSize = switchSpaceSize(mainFiles[position].length());
			allInfo += ("类型：" + fileType + "\n"
					+ "MINE：" + MINEType + "\n"
					+ "大小：" + fileSize + "\n");
		}else{
			String fileSize = switchSpaceSize( 
												getFolderSize(mainFiles[position])
											 );
			allInfo += "大小：" + fileSize + "\n";
		}
		
		String filePath = mainFiles[position].getParentFile().getPath();
		
		allInfo += "路径：" + filePath + "\n";
		
		new AlertDialog.Builder(FileManageActivity.this)
        .setTitle("文件属性")
        .setMessage(allInfo)
        .setNegativeButton("确定", null)
        .show();

	}

	/**
	 * 修改显示大小
	 * 
	 * @param valus
	 *            修改值
	 * @return
	 */
	public String switchSpaceSize(double valus) {
		DecimalFormat df = new DecimalFormat("###.00");

		if (valus < 1024) {
			return df.format(valus) + " byte";
		} else if (1024 <= valus && valus < 1048576) {
			valus = valus / 1024;
			return df.format(valus) + " kb";
		} else if (1048576 <= valus && valus < 1073741824) {
			valus = valus / 1024 / 1024;
			return df.format(valus) + " MB";
		} else if (1073741824 <= valus) {
			valus = valus / 1024 / 1024 / 1024;
			return df.format(valus) + " GB";
		}
		return 0 + "byte";
	}

	/**
	 * 获取文件夹大小
	 * 
	 * @param file
	 *            File实例
	 * @return long
	 */
	public long getFolderSize(File file) {
		long size = 0;
		
		try {
			File[] fileList = file.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isDirectory()) {
					size = size + getFolderSize(fileList[i]);

				} else {
					size = size + fileList[i].length();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return size;
	}
}
