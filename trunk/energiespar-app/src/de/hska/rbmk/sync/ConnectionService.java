package de.hska.rbmk.sync;

import de.hska.rbmk.R;
import de.hska.rbmk.datenVerwaltung.DbAdapter;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Riccardo Baral
 * 
 */
public class ConnectionService extends Service {
	
	private static final int NOTIFICATION_ID = 77;
	int mStartMode;
	Handler mHandler;
	
	public boolean serviceRunning;
	public boolean wasSuccess;
	
    Socket serverSocket;  
    DataOutputStream outS = null;
    DataInputStream inS = null;
	
	private SQLiteDatabase db;
	private DbAdapter dbAdapter;
    
    public String server_ip;
	public String server_port;
	public byte[] transmission;
	
	Intent updateIntent;
	
	@Override
	public IBinder onBind(Intent intent) {
		return(myConnectionServiceStub);
	}
	
	// gets called by Activities, mostly through SignalServiceConnection
	// interface constraints by IConncetionService.aidl
	private IConnectionService.Stub myConnectionServiceStub = new IConnectionService.Stub() {
		
		// this method checks if the ConnectionService is connected to a server
		// it uses sendBroadcast to inform other Activities about the status 
		public void getConnectionStatus() throws RemoteException
		{
			updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
			if (serverSocket != null) 
			{
	    		if (serverSocket.isBound())
		    		updateIntent.putExtra("connectionStatus", true);
	    		else
		    		updateIntent.putExtra("connectionStatus", false);
			}
			else
				updateIntent.putExtra("connectionStatus", false);

    		sendBroadcast(updateIntent);
		}
		
		public void setIPandPort(String ip, String port) throws RemoteException {
			server_ip = ip;
			server_port = port;
		}
		
		// this method displays a string of integers to the user in the DualAdvancerControlActivity
		public void sendCharArray(byte[] charArray) throws RemoteException {
			try
			{
				String charString = "";
				
				for (int i = 0; i < charArray.length; i++) {
					charString += charArray[i]+" ";
				}
				
				updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
				updateIntent.putExtra("notification", ">> "+charString);
				sendBroadcast(updateIntent);

				outS.write(charArray);

			} catch (Exception e) {
				updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
				updateIntent.putExtra("notification", e.toString());
				sendBroadcast(updateIntent);
			}
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();

		dbAdapter = new DbAdapter(this);
		
		final Notification notification = new Notification(R.drawable.ic_launcher, "Synchronisiere Zählerstände", System.currentTimeMillis());
//		Intent notificationIntent = new Intent();//this, DualAdvancerControlActivity.class);
//	    final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	    notification.setLatestEventInfo(this, "EnergieBerater", "Zählerstände werden synchronisiert...", null); //pendingintent
	    startForeground(NOTIFICATION_ID, notification);
	}

	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        serviceRunning = true;
        
        wasSuccess = false;
        
        // extras in this case are the IP and Port
		Bundle extras = intent.getExtras();
    	
    	if (extras != null) {
    		server_ip = extras.getString("ip address");
    		server_port = extras.getString("port number");
    	}
		
    	// this is for logging purposes
//		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
//		updateIntent.putExtra("notification", "Trying to connect to "+server_ip+":"+server_port);
//		sendBroadcast(updateIntent);
		
		// the actual thread in which the connection is established
		new Thread(new Runnable() {
		    public void run() {

		    	try {
//					byte[] streamData = new byte[28];
					
		    		serverSocket = new Socket(server_ip, Integer.parseInt(server_port));
		    		
//		    		if (serverSocket.isBound())
//		    		{
//		    			updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
//	    	    		updateIntent.putExtra("connectionStatus", true);
//	    	    		updateIntent.putExtra("notification", "Connection established.");
//		        		sendBroadcast(updateIntent);
//		    		}
		    		
		    		
//		            outS = new DataOutputStream(serverSocket.getOutputStream());
//		            inS = new DataInputStream(serverSocket.getInputStream());
		            
		            
		            
		            PrintWriter out = null;
		            out = new PrintWriter(serverSocket.getOutputStream(), true);
		            BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
		            
					String charString = "Welcome";
//					byte[] charArray = {};
//					for (int i = 0; i < charString.length(); i++) {
//						charArray[i] = (byte) charString.charAt(i);
//					}
//					outS.write(charArray);
		            
//		            out.println(charString);
		            String msg;
		            
//		            while (serviceRunning) {
		            	while((msg = in.readLine()) != null)
		            	{
		            		Log.d("SERVICE Incoming", msg);
		            		
		            		try {
								Long.parseLong(msg);
								
								DbAdapter mRDBA = new DbAdapter(getApplicationContext());

								mRDBA.open();
								ArrayList<String> statements = mRDBA.getDatebaseChangesByDate(msg);
								mRDBA.close();
								
								for (int i = 0; i < statements.size(); i++)
								{
									Log.d("SERVICE Sending", statements.get(i)); 
									out.println(statements.get(i));
								}
								
								out.println("END");
								
								Log.d("SERVICE", "Transfer complete");
								
								wasSuccess = true;
								serviceRunning = false;
								

								
								break;
								
							} catch (NumberFormatException e) {
								Log.d("SERVICE Check", "(NumberFormatException: not a long)");
								// do nothing
							}
		            		
		            		
		            	}

//    		    		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
//    		    		updateIntent.putExtra("transmission", streamData);
//    		    		sendBroadcast(updateIntent);
		            	
//		            }
		            
					serverSocket.close();
					out.close();
					in.close();
		    		stopSelf();
					
				} catch (ConnectException ce) {
					// connection refused by server
//		    		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
//		    		updateIntent.putExtra("notification", "Connection refused."); // logging
//		    		sendBroadcast(updateIntent);
					Log.d("SERVICE", "Connection refused");
		    		stopSelf();
				} catch (EOFException eof) {
					// EOF reached, connection terminated by server
//		    		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
//		    		updateIntent.putExtra("notification", "Connection terminated by server."); // logging
//		    		updateIntent.putExtra("connectionStatus", false);
//		    		sendBroadcast(updateIntent);
					Log.d("SERVICE", "Connection terminated by server");
		    		stopSelf();
				} catch (SocketException se) {
					// socket exception
//		    		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
//		    		updateIntent.putExtra("notification", "Connection: "+se.getMessage()); // logging
//		    		updateIntent.putExtra("connectionStatus", false);
//		    		sendBroadcast(updateIntent);
					Log.d("SERVICE", "Socket exception");
		    		stopSelf();
				} catch (Exception e) {
					// any other exception
//		    		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
//		    		updateIntent.putExtra("notification", "Error: "+e.getMessage()); // logging
//		    		updateIntent.putExtra("connectionStatus", false);
//		    		sendBroadcast(updateIntent);
					Log.d("SERVICE", "Other error");
		    		stopSelf();
				}
		    }
		  }).start();
		
        return mStartMode;
    }

	@Override
	public void onDestroy() {
		serviceRunning = false;

		try {
			if (serverSocket != null)
				serverSocket.close();
			if (outS != null)
				outS.close();
			if (inS != null)
				inS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		Toast.makeText(this, "Verbindung getrennt", Toast.LENGTH_SHORT).show();
		if (wasSuccess)
		{
			final Notification notification = new Notification(R.drawable.ic_launcher, "Zählerstände erfolgreich synchronisiert", System.currentTimeMillis());
	//		Intent notificationIntent = new Intent();//this, DualAdvancerControlActivity.class);
	//	    final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		    notification.setLatestEventInfo(this, "EnergieBerater", "Zählerstände wurden synchronisiert.", null); //pendingintent
		    startForeground(NOTIFICATION_ID, notification);
		}
	}
}

