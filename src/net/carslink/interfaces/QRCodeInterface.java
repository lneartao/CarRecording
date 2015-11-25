package net.carslink.interfaces;

import android.graphics.Bitmap;

/**
 * 接口说明：获取二维码回调函数借接口
 * @author Giam
 * 时间：2015/03/18
 */

public interface QRCodeInterface {
	public void getQRCodeSuccess(Bitmap qrBitmap);
}
