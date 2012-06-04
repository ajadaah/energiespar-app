package de.hska.info.electricMeter.ocr;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import de.hska.info.electricMeter.R;
import de.hska.info.electricMeter.camera.CameraActivity;
import de.hska.info.electricMeter.ocr.zxing.PlanarYUVLuminanceSource;

public class OCRHandler extends Handler {

	private static final String TAG = OCRHandler.class.getSimpleName();

	private CameraActivity cameraActivity;
	private OCR ocr;

	private boolean running = true;

	public OCRHandler(CameraActivity activity) {
		cameraActivity = activity;

		ocr = new OCR(cameraActivity);
	}

	@Override
	public void handleMessage(Message msg) {
		if (!running) {
			return;
		}
		switch (msg.what) {
		case R.id.ocr_msg:
			ocr((byte[]) msg.obj, msg.arg1, msg.arg2);
			break;

		case R.id.quit_msg:
			running = false;
			Looper.myLooper().quit();
			break;
		}
	}

	/**
	 * Decode image data.
	 * 
	 * @param data
	 *            The YUV preview frame.
	 * @param width
	 *            The width of the preview frame.
	 * @param height
	 *            The height of the preview frame.
	 */
	private void ocr(byte[] data, int width, int height) {
		long start = System.currentTimeMillis();
		String resultString = "";
		Bitmap resultImage = null;
		PlanarYUVLuminanceSource source = cameraActivity.getCameraManager()
				.buildLuminanceSource(data, width, height);
		if (source != null) {
			resultImage = source.renderCroppedGreyscaleBitmap();
			resultString = ocr.ocrScanFromBitmap(source
					.renderCroppedGreyscaleBitmap());

//			if (resultString != "0374670") {
				// Save failed image to sd card to analyze and train it later
//				if (resultImage != null)
//					ocr.saveWrongImage(resultImage);
//			}
		}

		Handler handler = cameraActivity.getHandler();
		if (resultString.length() == 7) {
			long end = System.currentTimeMillis();
			if (handler != null) {
				Message message = Message.obtain(handler,
						R.id.ocr_succeeded_msg, resultString);
				Bundle bundle = new Bundle();
				bundle.putParcelable(OCRThread.OCR_BITMAP,
						source.renderCroppedGreyscaleBitmap());
				message.setData(bundle);
				message.sendToTarget();
			}
		} else {
			if (handler != null) {
				Message message = Message.obtain(handler, R.id.ocr_failed_msg);
				message.sendToTarget();
			}
		}
	}
}
