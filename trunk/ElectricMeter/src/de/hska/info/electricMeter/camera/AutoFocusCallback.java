package de.hska.info.electricMeter.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Autofocus callback class.
 */
public class AutoFocusCallback implements Camera.AutoFocusCallback {

	/** Define interval in that autofocus will fire */
	private static final long AUTOFOCUS_INTERVAL = 5000L;

	/** Autofocus handler */
	private Handler focusHandler;
	/** Handler message */
	private int focusMessage;

	/**
	 * This function is called after each autofocus try (each AUTOFOCUS_INTERVAL
	 * seconds). On success, success message will be send back to handler.
	 */
	public void onAutoFocus(boolean success, Camera camera) {
		Log.i("Camera", "Autofocus ok");

		if (focusHandler != null) {
			Message msg = focusHandler.obtainMessage(focusMessage, success);

			focusHandler.sendMessageDelayed(msg, AUTOFOCUS_INTERVAL);
			focusHandler = null;
		} else {
			Log.d("camera", "no handler for autofocus");
		}
	}

	/**
	 * Set autofocus handler to this callback class
	 * 
	 * @param focusHandler
	 * @param focusMessage
	 */
	public void setHandler(Handler focusHandler, int focusMessage) {
		this.focusHandler = focusHandler;
		this.focusMessage = focusMessage;
	}
}
