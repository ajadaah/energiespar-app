package de.hska.info.electricMeter.camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

/**
 * Get a preview frame if autofocus finished.
 */
public class PreviewCallback implements Camera.PreviewCallback {

	private CameraConfigurationManager configManager;
	private Handler previewHandler;
	private int previewMsg;

	public PreviewCallback(CameraConfigurationManager conf) {
		configManager = conf;
	}

	/**
	 * 
	 */
	public void onPreviewFrame(byte[] data, Camera camera) {
		Point cameraResolution = configManager.getCameraResolution();
		
		Handler localPreviewHandler = previewHandler;

		if (localPreviewHandler != null) {
			Message msg = localPreviewHandler.obtainMessage(previewMsg,
					cameraResolution.x, cameraResolution.y, data);
			msg.sendToTarget();
			previewHandler = null;
		}
	}

	/**
	 * 
	 * @param previewHandler
	 * @param previewMsg
	 */
	public void setHandler(Handler previewHandler, int previewMsg) {
		this.previewHandler = previewHandler;
		this.previewMsg = previewMsg;
	}
}
