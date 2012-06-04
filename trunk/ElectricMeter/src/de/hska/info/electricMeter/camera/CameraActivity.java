package de.hska.info.electricMeter.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import de.hska.info.electricMeter.Constants;
import de.hska.info.electricMeter.R;
import de.hska.info.electricMeter.wheel.WheelActivity;

/**
 * Camera activity. This activity defines camera surface view as main content
 * view and adds overlay rectangle. Also it initializes camera manager that
 * starts and stops camera.
 * 
 */
public class CameraActivity extends Activity {

	// Scan region in percent
	public static float OCR_SCAN_REGION_LEFT = 0.0f;
	public static float OCR_SCAN_REGION_RIGHT = 1.0f;
	public static float OCR_SCAN_REGION_TOP = 0.2f;
	public static float OCR_SCAN_REGION_BOTTOM = 0.4f;

	private CameraSurfaceView cameraView;
	private CameraOverlay cameraOverlay;
	private CameraManager cameraManager;
	private CameraFlashButton cameraFlashButton;
	private ImageButton manualInputButton;

	/** Default electric meter */
	private int meternumber = -1;

	public CameraOverlay getCameraOverlay() {
		return cameraOverlay;
	}

	public void setCameraOverlay(CameraOverlay cameraOverlay) {
		this.cameraOverlay = cameraOverlay;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setMeternumber(getIntent().getExtras().getInt(Constants.METERNUMBER));
	}

	@Override
	protected void onResume() {
		super.onResume();

		cameraManager = new CameraManager(this);
		cameraFlashButton = new CameraFlashButton(this, cameraManager);
		manualInputButton = new ImageButton(this);
		manualInputButton.setImageResource(R.drawable.btn_manual_input);
		manualInputButton.setOnClickListener(onClickManualInputButtonListener);
		cameraView = new CameraSurfaceView(this, cameraManager);
		cameraOverlay = new CameraOverlay(this, null);
		cameraOverlay.setCameraManager(cameraManager);

		setContentView(cameraView);

		// Adds scan area overlay
		addContentView(cameraOverlay, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		
		LinearLayout ll = new LinearLayout(this);
		//add camera flash button
		ll.addView(cameraFlashButton);
		//add button to skip OCR and manually input the value
		ll.addView(manualInputButton);
		LayoutParams lp = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		addContentView(ll, lp);

	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	public CameraActivityHandler getHandler() {
		return (CameraActivityHandler) cameraView.getHandler();
	}

	public void drawScanRectangle() {
		cameraOverlay.drawScanRectangle();
	}

	public void displayResult(Bitmap resultBmp, String text) {
		cameraOverlay.drawResultBitmap(resultBmp, text);
	}
	
	public int getMeternumber() {
		return meternumber;
	}

	public void setMeternumber(int meternumber) {
		this.meternumber = meternumber;
	}

	private OnClickListener onClickManualInputButtonListener = new OnClickListener() {
		
		public void onClick(View view) {
			Intent manualInputIntent = new Intent(view.getContext(), WheelActivity.class);
			manualInputIntent.putExtra(Constants.METERVALUE, "0");
			manualInputIntent.putExtra(Constants.METERNUMBER, meternumber);
			startActivityForResult(manualInputIntent, 0);
			
		}
	};
}
