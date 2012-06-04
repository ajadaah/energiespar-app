package de.hska.info.electricMeter.camera;

import java.io.IOException;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import de.hska.info.electricMeter.ocr.zxing.PlanarYUVLuminanceSource;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 * 
 * Class modified from original zxing class.
 */

public class CameraManager {

	private static final String TAG = "CameraManager";

	private static final int MIN_FRAME_WIDTH = 240;
	private static final int MIN_FRAME_HEIGHT = 140;
	private static final int MAX_FRAME_WIDTH = 800;
	private static final int MAX_FRAME_HEIGHT = 480;

	/** Context from activity */
	private Context context;
	/** Main camera interface */
	private Camera camera;
	/** Is preview running now? */
	private boolean isPreview = false;
	/** Should image be reversed? */
	private boolean reverseImage;

	private final PreviewCallback previewCallback;
	private final AutoFocusCallback autofocusCallback;
	private final CameraConfigurationManager configManager;

	private Point screenResolution;
	private Point cameraResolution;

	private Rect framingRect;
	private Rect framingRectInPreview;

	private Size previewSize;

	public CameraManager(Context context) {
		this.context = context;
		configManager = new CameraConfigurationManager(context);
		previewCallback = new PreviewCallback(configManager);
		autofocusCallback = new AutoFocusCallback();
	}

	/**
	 * Opens camera driver.
	 * 
	 * @param holder
	 * @return
	 * @throws IOException
	 */
	public Camera openCamera(SurfaceHolder holder) throws IOException {
		if (camera == null)
			camera = Camera.open();

		camera.setPreviewDisplay(holder);

		configManager.initFromCameraParameters(camera);
		configManager.setDesiredCameraParameters(camera);

		return camera;
	}

	/**
	 * Closes camera driver.
	 */
	public void closeCamera() {
		if (camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null); // Very important. Set preview
			// callback to null, so that
			// camera not trying to call
			// preview method after release.
			camera.release();
			camera = null;

			framingRect = null;
			framingRectInPreview = null;
		}
	}

	/**
	 * Asks the camera hardware to begin drawing preview frames to the screen.
	 */
	public void startPreview() {
		Camera localCamera = camera;
		if (localCamera != null && !isPreview) {
			camera.startPreview();
			isPreview = true;
		}
	}

	/**
	 * A single preview frame will be returned to the handler supplied. The data
	 * will arrive as byte[] in the message.obj field, with width and height
	 * encoded as message.arg1 and message.arg2, respectively.
	 * 
	 * @param handler
	 *            The handler to send the message to.
	 * @param message
	 *            The what field of the message to be sent.
	 */
	public void requestPreviewFrame(Handler handler, int message) {
		Camera localCamera = camera;
		if (camera != null && isPreview) {
			previewCallback.setHandler(handler, message);
			localCamera.setOneShotPreviewCallback(previewCallback);
		}
	}

	/**
	 * Asks the camera hardware to perform an autofocus.
	 * 
	 * @param handler
	 *            The Handler to notify when the autofocus completes.
	 * @param message
	 *            The message to deliver.
	 */
	public void requestAutofocus(Handler handler, int message) {
		if (camera != null && isPreview) {
			autofocusCallback.setHandler(handler, message);
			camera.autoFocus(autofocusCallback);
		}
	}

	/*
	 * public void setParameters() { if (camera != null) { Parameters params =
	 * camera.getParameters(); WindowManager manager = (WindowManager) context
	 * .getSystemService(Context.WINDOW_SERVICE); Display display =
	 * manager.getDefaultDisplay(); int width = display.getWidth(); int height =
	 * display.getHeight();
	 * 
	 * if (width < height) { Log.i(TAG,
	 * "Display reports portrait orientation; assuming this is incorrect"); int
	 * temp = width; width = height; height = temp; }
	 * 
	 * screenResolution = new Point(width, height);
	 * 
	 * Log.i(TAG, "Screen resolution: " + screenResolution); cameraResolution =
	 * findBestPreviewSizeValue(params, screenResolution, false); Log.i(TAG,
	 * "Camera resolution: " + cameraResolution);
	 * 
	 * // camera.setDisplayOrientation(90);
	 * 
	 * if (CameraActivity.DEFAULT_OCR_SCAN_TYPE == Constants.OCR_SCAN_TYPE_JPEG)
	 * { // Set camera image size List<Camera.Size> supportedImageSizes = params
	 * .getSupportedPictureSizes();
	 * 
	 * if (!supportedImageSizes.isEmpty()) { params.setJpegQuality(100);
	 * params.setRotation(90);
	 * params.setPictureSize(supportedImageSizes.get(0).width,
	 * supportedImageSizes.get(0).height);
	 * params.setPictureFormat(PixelFormat.JPEG); camera.setParameters(params);
	 * 
	 * }
	 * 
	 * previewSize = camera.getParameters().getPictureSize(); } else if
	 * (CameraActivity.DEFAULT_OCR_SCAN_TYPE == Constants.OCR_SCAN_TYPE_VIDEO) {
	 * previewSize = camera.getParameters().getPreviewSize(); }
	 * camera.setPreviewCallback(previewCallback); } }
	 */

	/**
	 * Returns camera preview size.
	 * 
	 * @return
	 */
	public Size getPreviewSize() {
		return previewSize;
	}

	/**
	 * A factory method to build the appropriate LuminanceSource object based on
	 * the format of the preview buffers, as described by Camera.Parameters.
	 * 
	 * @param data
	 *            A preview frame.
	 * @param width
	 *            The width of the image.
	 * @param height
	 *            The height of the image.
	 * @return A PlanarYUVLuminanceSource instance.
	 * 
	 * @author zxing
	 */
	public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data,
			int width, int height) {
		Rect rect = getFramingRectInPreview();
		if (rect == null) {
			return null;
		}
		// Go ahead and assume it's YUV rather than die.
		return new PlanarYUVLuminanceSource(data, width, height, rect.left,
				rect.top, rect.width(), rect.height(), reverseImage);
	}

	/**
	 * Like {@link #getFramingRect} but coordinates are in terms of the preview
	 * frame, not UI / screen.
	 * 
	 * @author zxing
	 */
	public Rect getFramingRectInPreview() {
		if (framingRectInPreview == null) {
			Rect framingRect = getFramingRect();
			if (framingRect == null) {
				return null;
			}
			Rect rect = new Rect(framingRect);
			Point cameraResolution = configManager.getCameraResolution();
			Point screenResolution = configManager.getScreenResolution();
			rect.left = rect.left * cameraResolution.x / screenResolution.x;
			rect.right = rect.right * cameraResolution.x / screenResolution.x;
			rect.top = rect.top * cameraResolution.y / screenResolution.y;
			rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
			framingRectInPreview = rect;
		}
		return framingRectInPreview;
	}

	/**
	 * Calculates the framing rect which the UI should draw to show the user
	 * where to place the barcode. This target helps with alignment as well as
	 * forces the user to hold the device far enough away to ensure the image
	 * will be in focus.
	 * 
	 * @return The rectangle to draw on screen in window coordinates.
	 * 
	 * @author zxing
	 */
	public Rect getFramingRect() {
		if (framingRect == null) {
			if (camera == null) {
				return null;
			}
			Point screenResolution = configManager.getScreenResolution();
			int width = screenResolution.x * 19 / 20;
			if (width < MIN_FRAME_WIDTH) {
				width = MIN_FRAME_WIDTH;
			} else if (width > MAX_FRAME_WIDTH) {
				width = MAX_FRAME_WIDTH;
			}
			int height = screenResolution.y * 2 / 10;
			if (height < MIN_FRAME_HEIGHT) {
				height = MIN_FRAME_HEIGHT;
			} else if (height > MAX_FRAME_HEIGHT) {
				height = MAX_FRAME_HEIGHT;
			}
			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height) / 3;
			framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
					topOffset + height);
			Log.d(TAG, "Calculated framing rect: " + framingRect);
		}
		return framingRect;
	}

	/**
	 * Set framing rect
	 */
	public void setFramingRect(Rect newRect) {
		if (framingRect != null) {
			framingRect = newRect;
		}
	}

	public void toggleFlash() {
		configManager.setTorch(camera);
	}

	public CameraConfigurationManager getConfigManager() {
		return this.configManager;
	}
}
