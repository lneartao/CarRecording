package net.carslink.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class SeePictureActivity extends Activity {
	private ImageView seepicture_activity_layout_pictureview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_layout_seepicture);
		setupView();
		
		readPictureFile();
	}
	
	public void setupView(){
		seepicture_activity_layout_pictureview = (ImageView)findViewById(R.id.seepicture_activity_layout_pictureview);
	}
	
	public void readPictureFile(){
		Intent getDataIntent = this.getIntent();
		String imagePath = getDataIntent.getStringExtra("picturePath");
		
		Bitmap imgBitmap = BitmapFactory.decodeFile(imagePath);
		
		seepicture_activity_layout_pictureview.setImageBitmap(imgBitmap);
	}
}
