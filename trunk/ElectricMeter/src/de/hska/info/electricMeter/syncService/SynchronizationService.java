package de.hska.info.electricMeter.syncService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.ManagerFactoryParameters;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import de.hska.info.electricMeter.ElectricMeterActivity;
import de.hska.info.electricMeter.R;
import de.hska.info.electricMeter.dataStorage.MeterReadingEntry;
import de.hska.info.electricMeter.dataStorage.MeterReadingsDbAdapter;
import de.hska.info.electricMeter.dataStorage.MeterType;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SynchronizationService extends IntentService {
	
	private final String logContext = "SynchronizationService";
	
	private MeterReadingsDbAdapter mDbHelper;
	private Cursor mReadingsCursor;
	
	private NotificationManager mNotificationManager;
	private final int SERVICENOTIFICATION_ID = 1;

	public SynchronizationService() {
		super("SynchronizationService");
	}

	@Override
	protected void onHandleIntent(Intent workUnit) {
		Log.d(logContext, "new intent from queue.");
		
		//this.waitForXSeconds(2); // give the device some time to switch the activity...

		List<MeterReadingEntry> meterReadingEntryList = new ArrayList<MeterReadingEntry>();
		mReadingsCursor = mDbHelper.getUnsynchronizedMeterReadings();
		Log.d(logContext, "loaded " + mReadingsCursor.getCount() + " unsynchronized meter reading entries from the database, currently at position " + mReadingsCursor.getPosition() + ".");
		mReadingsCursor.moveToFirst();
		boolean moreAvailable = mReadingsCursor.getCount() > 0;
		while (moreAvailable) {
			int rowID = mReadingsCursor.getInt(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_ROWID));
			String connectionInfo = mReadingsCursor.getString(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_CONNECTIONINFO));
		    String timestamp = mReadingsCursor.getString(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_TIMESTAMP));
		    int meterNumber = mReadingsCursor.getInt(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERNUMBER));
		    int meterValue = mReadingsCursor.getInt(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERVALUE));;
		    int meterType = mReadingsCursor.getInt(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERTYPE));;
		    String isMeterValueRevised = mReadingsCursor.getString(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERVALUEREVISED)); 
		    String isSynchronized = mReadingsCursor.getString(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_SYNCHRONIZED));
		    meterReadingEntryList.add(new MeterReadingEntry(rowID, connectionInfo, new Date(new Long(timestamp)), meterNumber, meterValue, MeterType.get(meterType), Boolean.getBoolean(isMeterValueRevised), Boolean.getBoolean(isSynchronized)));
		    moreAvailable = !mReadingsCursor.isLast();
			mReadingsCursor.moveToNext();
		}
		mReadingsCursor.close();
		
		Log.d(logContext, "mapped " + meterReadingEntryList.size() + " meter reading entries from the database");
				
		boolean hasBeenProcessed = meterReadingEntryList.size() == 0; // == 0 means no readings available; > 0 means readings available - will be set to true later in two cases: 1. if the data has been sent successfully 2. if there was a server side error during sending the data
		int triesLeftBeforeNotification = 3;
		int waitTime = 15; 
		while (!hasBeenProcessed) {
			if (this.isOnline()) {
				this.updateNotification("Senden von Electric Meter Zählerständen", "Electric Meter Service Hinweis", meterReadingEntryList.size() + ((meterReadingEntryList.size() == 1) ? " Zählerstand wird synchronisiert..." : " Zählerstände werden synchronisiert..."));
				try {
					String hostAndPort = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_server_agent_url", "http://localhost:8765");
					String SERVICE_EPR = hostAndPort + "/meterReadingService";
					Log.d(logContext, "Sever URL: " + SERVICE_EPR);
					HttpPost httpPost = new HttpPost(SERVICE_EPR);          
					String requestXML = prepareSOAPCall(meterReadingEntryList);
					Log.d(logContext, "SOAP message (length=" + requestXML.length() + "), last 250 chars: " + requestXML.substring(requestXML.length() - 250));
					StringEntity se = new StringEntity(requestXML,HTTP.UTF_8);

					se.setContentType("text/xml");  
					//httppost.setHeader("Content-Type","application/soap+xml;charset=UTF-8");
					httpPost.setHeader("Content-Type", "text/xml");
					httpPost.setEntity(se);  
					
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters, 10000); // wait for a connection for 10 seconds
					HttpConnectionParams.setSoTimeout(httpParameters, 15000); // wait for a response for 15 seconds

					HttpClient httpClient = new DefaultHttpClient(httpParameters);
					BasicHttpResponse httpResponse = 
					    (BasicHttpResponse) httpClient.execute(httpPost);
					
					int responseStatusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(logContext, "SOAP call returned status " + responseStatusCode); // TODO better handling of the return code
					if(responseStatusCode != 200) { // there has been an error
						this.updateNotification("Electric Meter Serverfehler", "Electric Meter Service Hinweis", "Die Zählerstände konnten nicht synchronisiert werden, Serverproblem: " + responseStatusCode + " - " + httpResponse.getStatusLine().getReasonPhrase());
					} else { //everything alright, clean up local database entries 
						this.updateNotification("Electric Meter Zählerstände gesendet", "Electric Meter Service Hinweis", meterReadingEntryList.size() + ((meterReadingEntryList.size() == 1) ? " Zählerstand wurde synchronisiert." : " Zählerstände wurden synchronisiert."));
						Log.d(logContext, "updating the synchronization status of " + meterReadingEntryList.size() + " database entries.");
						for (MeterReadingEntry meterReadingEntry : meterReadingEntryList) {
							mDbHelper.updateSynchronizedStatus(meterReadingEntry.rowID, true);
						}
						Log.d(logContext, "updated the synchronization status of " + meterReadingEntryList.size() + " database entries.");
					}
					hasBeenProcessed = true;
				} catch (ClientProtocolException e) { // in case of connection problems (server not reachable, ...)
					Log.e(logContext, "error during SOAP call: " + e.getMessage());
					this.updateNotification("Electric Meter Service konnte sich nicht verbinden", "Electric Meter Service Hinweis", "Die Zählerstände konnten nicht synchronisiert werden.");
					hasBeenProcessed = true;
				} catch (UnsupportedEncodingException e) { // in case of encoding problems of the SOAP message
					Log.e(logContext, "unsupported data encoding call: " + e.getMessage());
					hasBeenProcessed = true;
				} catch (IOException e) { //
					Log.e(logContext, "error during SOAP call: " + e.getMessage());
					this.updateNotification("Electric Meter Service konnte sich nicht verbinden", "Electric Meter Service Hinweis", "Die Zählerstände konnten nicht synchronisiert werden.");
					hasBeenProcessed = true;
				}
			} else {
				if(triesLeftBeforeNotification <= 0) { // in case of several failed tries, notify the user and wait for a longer time the next time only before trying again, reset the number of failed tries left
					this.updateNotification("Electric Meter Service konnte sich nicht verbinden", "Electric Meter Service Hinweis", "Keine Datenverbindung zum Senden der Zählerstände. Versuche es weiter...");
					triesLeftBeforeNotification = 3;
					waitTime = 300;
				} else { // set normal waitTime when counting down the failed tries
					triesLeftBeforeNotification--;
					waitTime = 15;
				}
				Log.i(logContext, "Connection not available, will wait for " + waitTime + " seconds before trying again.");
				this.waitForXSeconds(waitTime);
			}
		}
		
	} // EOS

	private String prepareSOAPCall(List<MeterReadingEntry> meterReadingEntryList) {
		StringBuilder sb = new StringBuilder();
		//sb.ensureCapacity(((meterReadingEntryList.size()*400) + 300);
		sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:met=\"http://MeterReadingService.ServerAgent.electricMeter.info.hska.de/\">"
		   + "<soapenv:Header/><soapenv:Body><met:storeMeterReadings>");
		for (MeterReadingEntry meterReadingEntry : meterReadingEntryList) {
			sb.append(meterReadingEntry.toXML());
		}
		sb.append("</met:storeMeterReadings></soapenv:Body></soapenv:Envelope>");
		return sb.toString();
	}
	
	private boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();  // result could be null in case of airplane mode
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
	private void waitForXSeconds(int seconds) {
		long endTime = System.currentTimeMillis() + seconds * 1000;
		while (System.currentTimeMillis() < endTime) {
			synchronized (this) {
				try {
					wait(endTime - System.currentTimeMillis());
				} catch (Exception e) {
				}
			}
		}
	}
	
	private void updateNotification(String ticker, String header, String message) {
		Notification notification = new Notification(R.drawable.icon_capture, ticker, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Context context = getApplicationContext();
		CharSequence contentTitle = header;
		CharSequence contentText = message;
		
		//Intent notificationIntent = new Intent(context, ElectricMeterActivity.class);
		//PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, null, 0);//getActivity(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		try {
			mNotificationManager.notify(SERVICENOTIFICATION_ID, notification);
		} catch (Exception e) {
			Log.e(logContext, "unknown error (" + e.getCause() +"): " + e.getMessage());
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(logContext, "new StartCommand, fetching database helper...");
		mDbHelper = new MeterReadingsDbAdapter(this);
		Log.d(logContext, "database helper fetched.");
		mDbHelper.open();
		Log.d(logContext, "database helper opened access to the database.");
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		//Toast.makeText(this, "synchronization service is shutting down.", Toast.LENGTH_SHORT).show();
		Log.d(logContext, "closing database...");
		mDbHelper.close();
		Log.d(logContext, "database closed.");
		Log.d(logContext, "shutting down service...");
		super.onDestroy();
	}
}
