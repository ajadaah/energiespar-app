package de.hska.info.electricMeter.camera;

import java.io.IOException;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Camera surface view. Displays camera preview on the screen.
 * 
 */
public class CameraSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {

	private CameraActivityHandler handler;
	private CameraActivity context;
	private CameraManager cameraManager;
	private SurfaceHolder holder;

	/**
	 * Constructor that adds callback to this class
	 * 
	 * @param context
	 * @param cameraManager2
	 */
	public CameraSurfaceView(Context context, CameraManager cameraManager) {
		super(context);

		if (context instanceof CameraActivity)
			this.context = (CameraActivity) context;

		this.cameraManager = cameraManager;

		this.holder = this.getHolder();
		this.holder.addCallback(this);
		this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	/**
	 * Setup camera parameters and begin preview
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// ocr.setScanRegion(cameraManager.getPreviewSize());
	}

	/**
	 * Get instance of a camera here and initialize preview holder
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			cameraManager.openCamera(holder);

			if (handler == null) {
				handler = new CameraActivityHandler(context, cameraManager);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stop preview and release camera
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		cameraManager.closeCamera();
	}
	
	/**
	 * Do something on touch event
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		return super.onTouchEvent(event);
	}

	@Override
	public CameraActivityHandler getHandler() {
		return handler;
	}

	/**
	 * (obsolete because we use only portait mode)
	 * 
	 * Set camera orientation to current display orientation.
	 * 
	 * @link{http://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation%28int%29
	 * 
	 * @param activity
	 * @param cameraId
	 * @param camera
	 */
	/*
	 * public void setCameraDisplayOrientation(Activity activity) {
	 * android.hardware.Camera.CameraInfo info = new
	 * android.hardware.Camera.CameraInfo(); int rotation =
	 * activity.getWindowManager().getDefaultDisplay() .getRotation(); int
	 * degrees = 0; switch (rotation) { case Surface.ROTATION_0: degrees = 0;
	 * break; case Surface.ROTATION_90: degrees = 90; break; case
	 * Surface.ROTATION_180: degrees = 180; break; case Surface.ROTATION_270:
	 * degrees = 270; break; }
	 * 
	 * int result; if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	 * result = (info.orientation + degrees) % 360; result = (360 - result) %
	 * 360; // compensate the mirror } else { // back-facing result =
	 * (info.orientation - degrees + 360) % 360 + 90; }
	 * cameraView.setDisplayOrientation(result); }
	 */
}
