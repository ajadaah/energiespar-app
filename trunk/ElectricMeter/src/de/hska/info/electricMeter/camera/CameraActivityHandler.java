package de.hska.info.electricMeter.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.hska.info.electricMeter.Constants;
import de.hska.info.electricMeter.R;
import de.hska.info.electricMeter.ocr.OCRThread;
import de.hska.info.electricMeter.wheel.WheelActivity;

/**
 * Camera activity handler manages communication between camera and threads. If
 * message arrives to this handler, it runs defines actions on specified
 * message.
 * 
 */
public class CameraActivityHandler extends Handler {

	private static final String TAG = CameraActivityHandler.class
			.getSimpleName();

	/**
	 * State of machine. Can be in preview mode, success (ocr ok) and done (ocr
	 * result can be send).
	 */
	private enum State {
		OCR_DICTDOWNLOAD, PREVIEW, SUCCESS, DONE
	}

	private CameraActivity cameraActivity;
	private CameraManager cameraManager;
	private OCRThread ocrThread;

	private State state;

	/**
	 * Camera activity handler constructor starts ocr thread and also camera
	 * preview.
	 * 
	 * @param context
	 * @param cameraMan
	 */
	public CameraActivityHandler(CameraActivity context, CameraManager cameraMan) {
		cameraActivity = context;
		ocrThread = new OCRThread(cameraActivity);
		ocrThread.start();

		cameraManager = cameraMan;
	}

	@Override
	public void handleMessage(Message msg) {

		switch (msg.what) {
		case R.id.ocr_dictdownload_finished:
			state = State.SUCCESS;
			
			cameraManager.startPreview();
			restartPreviewAndOCR();
			break;
		case R.id.auto_focus_msg:
			// Continuos autofocus
			if (state == State.PREVIEW) {
				cameraManager.requestAutofocus(this, R.id.auto_focus_msg);
			}
			break;
		case R.id.restart_preview_msg:
			Log.d(TAG, "Got restart preview message");
			restartPreviewAndOCR();
			break;
		case R.id.ocr_failed_msg:
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(ocrThread.getHandler(),
					R.id.ocr_msg);
			break;
		case R.id.ocr_succeeded_msg:
			Log.d(TAG, "Got ocr succeeded message");
			state = State.SUCCESS;
			Bundle bundle = msg.getData();
			Bitmap bitmap = bundle == null ? null : (Bitmap) bundle
					.getParcelable(OCRThread.OCR_BITMAP);
			// cameraActivity.displayResult(bitmap, (String) msg.obj);

			Intent wheelIntent = new Intent(cameraActivity,
					WheelActivity.class);
			wheelIntent.putExtra(Constants.METERVALUE, (String) msg.obj);
			wheelIntent.putExtra(Constants.METERNUMBER, cameraActivity.getMeternumber());
			cameraActivity.startActivity(wheelIntent);
			break;
		}
	}

	/**
	 * Start preview from camera and define default preview handler as
	 * ocrThread. After preview image was captured, ocr handler will be called
	 * to start actions on given message.
	 * 
	 * Drawing of grayed rectangle around the scan area is also happening here.
	 */
	private void restartPreviewAndOCR() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(ocrThread.getHandler(),
					R.id.ocr_msg);
			cameraManager.requestAutofocus(this, R.id.auto_focus_msg);
			cameraActivity.drawScanRectangle();
		}
	}
}
