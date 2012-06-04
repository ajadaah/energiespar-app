package de.hska.info.electricMeter.camera;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import de.hska.info.electricMeter.R;

public class CameraFlashButton extends ImageButton {

	private CameraManager cameraManager;
	private boolean flash = false;
	
	public CameraFlashButton(Context context, CameraManager cameraMan) {
		super(context);

		cameraManager = cameraMan;
		
		// Here comes the flash btn image
		setImageResource(R.drawable.btn_flashonicon);

		this.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				cameraManager.toggleFlash();
				flash = !flash;
				if(flash) {
					setImageResource(R.drawable.btn_flashofficon);
				} else {
					setImageResource(R.drawable.btn_flashonicon);
				}
			}
		});
	}
}