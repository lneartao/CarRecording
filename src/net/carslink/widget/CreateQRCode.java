package net.carslink.widget;

import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import net.carslink.util.SharedSetting;
import net.carslink.activity.R;
import net.carslink.interfaces.QRCodeInterface;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class CreateQRCode {
	Context context;
	private int QR_WIDTH = 300, QR_HEIGHT = 300;
	private QRCodeInterface mi;// 回调函数

	public CreateQRCode(Context context) {
		this.context = context;
	}

	public void setCallback(QRCodeInterface mi) {
		this.mi = mi;
	}

	public void createWeixinQRCode() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();

		if (info != null && info.isAvailable()) {
			String deviceId = SharedSetting.getDeviceId();
			
			String url = "http://weixin.carslink.net/wxio/api_get_qr_ticket/"
					+ deviceId;
			
			// String jsonStr = connServerForResult(url);
			AsyncHttpClient client = new AsyncHttpClient();
			client.setTimeout(360000);
			RequestParams params = null;
			client.get(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response) {
					String qrUrl = getUrlFromJson(response);

					Bitmap bitmap = createQRImage(qrUrl);

					if (bitmap != null) {
						mi.getQRCodeSuccess(bitmap);
					}
				}
			});
		} else {
			Log.d("DEBUG", "没有可用网络");
			Toast.makeText(context, context.getString(R.string.no_network),
					Toast.LENGTH_SHORT).show();
		}
	}

	// 要转换的地址或字符串,可以是中文, 没有用这个方法
	private Bitmap createQRImage(String url) {
		Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
				Bitmap.Config.ARGB_8888);
		try {
			// 判断URL合法性
			if (url == null || "".equals(url) || url.length() < 1) {
				return null;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			// 图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(url,
					BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
			// 下面这里按照二维码的算法，逐个生成二维码的图片，
			// 两个for循环是图片横列扫描的结果
			for (int y = 0; y < QR_HEIGHT; y++) {
				for (int x = 0; x < QR_WIDTH; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * QR_WIDTH + x] = 0xff000000;
					} else {
						pixels[y * QR_WIDTH + x] = 0xffffffff;
					}
				}
			}
			// 生成二维码图片的格式，使用ARGB_8888
			bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
			// 显示到一个ImageView上面
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	// 解析返回json
	private String getUrlFromJson(String strResult) {
		String result = "";
		try {
			JSONObject jsonObj = new JSONObject(strResult);
			result = jsonObj.getString("url");
		} catch (JSONException e) {
			System.out.println("Json parse error");
			e.printStackTrace();
		}
		return result;
	}

}
