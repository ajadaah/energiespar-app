package de.hska.info.electricMeter.ocr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import de.hska.info.electricMeter.R;
import de.hska.info.electricMeter.camera.CameraActivity;

/**
 * OCR functionality
 * 
 */
public class OCR {

	private CameraActivity cameraActivity;
	private TessBaseAPI baseAPI;
	private File sdcardDir;
	private ProgressDialog progressDialog;
	private Handler handler;
	private String DICT_URL;
	private String DICTIONARY;
	final private String DEFAULT_DICT_URL = "http://dl.dropbox.com/u/12000633/dict/eng.traineddata";

	/** */
	public boolean ocrDone = true;

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public OCR(Context context) {
		this.cameraActivity = (CameraActivity) context;
		handler = cameraActivity.getHandler();
		baseAPI = new TessBaseAPI();
		sdcardDir = Environment.getExternalStorageDirectory();

		testOCREngine();
	}

	/**
	 * This function provides a basic ocr scan of an input image.
	 * 
	 * Input parameter is a leptonica Pix structure (YCrCb from Camera, BMP,
	 * etc). Preview frames are always in Yuv format (YCrCb). Image depth must
	 * be known.
	 * 
	 * @param data
	 * @param height
	 * @param width
	 */
	public String ocrScanFromPreview(byte[] data, int width, int height,
			int depth) {

		Pix image = Pix.createFromPix(data, width, height, depth);

		return ocrScan(image);
	}

	/**
	 * This function provides a basic ocr scan of an input image.
	 * 
	 * Input parameter is a Jpeg image as byte array.
	 * 
	 * @param data
	 * @param width
	 * @param height
	 */
	public String ocrScanFromJpeg(byte[] data) {

		Pix image = ReadFile.readMem(data);

		return ocrScan(image);
	}

	/**
	 * This function provides a basic ocr scan of an input image.
	 * 
	 * Image must be in ARGB_8888 format.
	 * 
	 */
	public String ocrScanFromBitmap(Bitmap image) {
		return ocrScan(ReadFile.readBitmap(image));
	}

	/**
	 * This function provides a basic ocr scan of an input image.
	 * 
	 * Input parameter is a leptonica Pix structure, that containes a picture.
	 * 
	 */
	public String ocrScan(Pix image) {
		// image = OCRUtils.preprocessImage(image);
		String recognizedString = "";

		if (ocrDone) {
			ocrDone = false;

			// Limit recognition to only characters from this list
			baseAPI.setVariable("tessedit_char_whitelist", "0123456789");
			// Limit recogntion to PSM_SINGLE_WORD mode
			baseAPI.setVariable("tessedit_pageseg_mode", "8");
			baseAPI.setImage(image);

			recognizedString = baseAPI.getUTF8Text();

			Log.i("OCR", "recognized: " + recognizedString);

			baseAPI.clear();
			ocrDone = true;
		}

		return recognizedString;
	}

	/**
	 * Async processing of ocr scan.
	 * 
	 * TODO
	 * 
	 */
	public class decodeYUVClass extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... args) {
			return null;
		}

	}

	/**
	 * This function provides OCR Engine test functions: - directories exists -
	 * trained dictionary exists - read/write permissions
	 */
	private boolean testOCREngine() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cameraActivity);
		DICT_URL = preferences.getString("pref_ocr_dict_key", DEFAULT_DICT_URL);
		
		if(DICT_URL.equals(DEFAULT_DICT_URL)) {
			DICTIONARY = "eng";
		}
		else
			DICTIONARY = "xxx";
		
		String externalState = Environment.getExternalStorageState();
		boolean mediaWritable = false;
		boolean mediaAvailable = false;

		// External media writable and readable
		if (Environment.MEDIA_MOUNTED.equals(externalState)) {
			mediaAvailable = mediaWritable = true;
		}
		// External media only readable
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalState)) {
			mediaAvailable = true;
		}

		// CHeck if dictionary file exists
		if (mediaAvailable) {
			File sdcardDir = Environment.getExternalStorageDirectory();

			String trainedDataFile = DICTIONARY + ".traineddata";
			String trainedDataDir = sdcardDir.toString()
					+ "/tesseract/tessdata/";
			String trainedDataFull = trainedDataDir + trainedDataFile;

			File tessData = new File(trainedDataFull);
			// Trained data does not exists
			if (!tessData.exists()) {
				if (mediaWritable) {

					new File(trainedDataDir).mkdirs();

					progressDialog = new ProgressDialog(cameraActivity);
					progressDialog.setMessage("Downloading OCR dictionary");
					progressDialog
							.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressDialog.setIndeterminate(false);
					progressDialog.setMax(100);
					new DownloadDictThread().execute(trainedDataFull, DICT_URL);
					// Use this code to copy dict from assets

					// try {
					// InputStream is = am.open(trainedDataFile,
					// AssetManager.ACCESS_BUFFER);
					//
					// FileOutputStream fos = new FileOutputStream(
					// trainedDataFull);
					//
					// // copy asset trained file to sdcard
					// byte[] buffer = new byte[1024];
					// int read;
					// while ((read = is.read(buffer)) != -1) {
					// fos.write(buffer);
					// }
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
				}
				// Trained data does not exists and media not writable
				else {
					return false;
				}
			} else {
				// Send successefull result to cameraActivityhandler to launch camera preview
				Message message = Message.obtain(handler,
						R.id.ocr_dictdownload_finished);
				message.sendToTarget();
			}
		}

		// Test if init ok
		if (!baseAPI.init(sdcardDir.toString() + "/tesseract", DICTIONARY)) {
			return false;
		}

		return true;
	}

	/**
	 * Saved wrong recognized image to file system to analyze it later
	 * 
	 * @param resultImage
	 */
	public void saveWrongImage(Bitmap resultImage) {
		File f = new File("/sdcard/tesseract/cameraOutput"
				+ System.currentTimeMillis() + ".jpg");
		try {
			f.createNewFile();

			if (f.canWrite())
				WriteFile.writeImpliedFormat(ReadFile.readBitmap(resultImage),
						f);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * This class provides asynchronous download possibility
	 *
	 */
	public class DownloadDictThread extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			int count;

			try {
				
				URL url = new URL(params[1]);
				URLConnection connection = url.openConnection();

				connection.connect();

				int fLength = connection.getContentLength();
				if (fLength == -1) {
					fLength = 1926792;
				}

				InputStream is = new BufferedInputStream(url.openStream());
				OutputStream os = new FileOutputStream(params[0]);

				byte buffer[] = new byte[1024];
				long total = 0;

				while ((count = is.read(buffer)) != -1) {
					total += count;

					publishProgress((int) (total * 100 / fLength));
					os.write(buffer, 0, count);
				}

				os.flush();
				os.close();
				is.close();

				Message message = Message.obtain(handler,
						R.id.ocr_dictdownload_finished);
				message.sendToTarget();
			} catch (Exception e) {
				Message message = Message.obtain(handler,
						R.id.ocr_dictdownload_failed);
				message.sendToTarget();
			}

			return null;
		}

		@Override
		protected void onPreExecute() {
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressDialog.setProgress(values[0]);
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss();
			super.onPostExecute(result);
		}
	}
}
