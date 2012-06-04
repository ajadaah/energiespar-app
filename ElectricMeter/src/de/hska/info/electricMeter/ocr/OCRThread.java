package de.hska.info.electricMeter.ocr;

import java.util.concurrent.CountDownLatch;

import android.os.Handler;
import android.os.Looper;
import de.hska.info.electricMeter.camera.CameraActivity;

public class OCRThread extends Thread {

	public static final String OCR_BITMAP = "ocr_bitmap";

	private CameraActivity cameraActivity;
	private Handler handler;
	private final CountDownLatch handlerInitLatch;

	public OCRThread(CameraActivity activity) {
		cameraActivity = activity;
		handlerInitLatch = new CountDownLatch(1);
	}

	public Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		return handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		handler = new OCRHandler(cameraActivity);
		handlerInitLatch.countDown();
		Looper.loop();
	}
}
