package de.hska.rbmk.sync;

import de.hska.rbmk.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
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
	
    Socket serverSocket = null;  
    DataOutputStream outS = null;
    DataInputStream inS = null;
	
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

		final Notification notification = new Notification(R.drawable.ic_launcher, "Synchronisations Service", System.currentTimeMillis());
		Intent notificationIntent = new Intent();//this, DualAdvancerControlActivity.class);
	    final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	    notification.setLatestEventInfo(this, "Synchronisation", "Zählerstände werden synchronisiert...", pendingIntent); 
	    startForeground(NOTIFICATION_ID, notification);
	}

	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        serviceRunning = true;
    	
        // extras in this case are the IP and Port
		Bundle extras = intent.getExtras();
    	
    	if (extras != null) {
    		server_ip = extras.getString("ip address");
    		server_port = extras.getString("port number");
    	}
		
    	// this is for logging purposes
		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
		updateIntent.putExtra("notification", "Trying to connect to "+server_ip+":"+server_port);
		sendBroadcast(updateIntent);
		
		// the actual thread in which the connection is established
		new Thread(new Runnable() {
		    public void run() {

		    	try {
					byte[] streamData = new byte[28];
					
		    		serverSocket = new Socket(server_ip, Integer.parseInt(server_port));
		    		
		    		if (serverSocket.isBound())
		    		{
		    			updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
	    	    		updateIntent.putExtra("connectionStatus", true);
	    	    		updateIntent.putExtra("notification", "Connection established.");
		        		sendBroadcast(updateIntent);
		    		}
		    		
		    		
		            outS = new DataOutputStream(serverSocket.getOutputStream());
		            inS = new DataInputStream(serverSocket.getInputStream());
		            
		            while (serviceRunning) {
    		            inS.readFully(streamData, 0, 28);

    		    		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
    		    		updateIntent.putExtra("transmission", streamData);
    		    		sendBroadcast(updateIntent);
		            }
		            
					serverSocket.close();
					outS.close();
					inS.close();
		    		stopSelf();
					
				} catch (ConnectException ce) {
					// connection refused by server
		    		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
		    		updateIntent.putExtra("notification", "Connection refused."); // logging
		    		sendBroadcast(updateIntent);
		    		stopSelf();
				} catch (EOFException eof) {
					// EOF reached, connection terminated by server
		    		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
		    		updateIntent.putExtra("notification", "Connection terminated by server."); // logging
		    		updateIntent.putExtra("connectionStatus", false);
		    		sendBroadcast(updateIntent);
		    		stopSelf();
				} catch (SocketException se) {
					// socket exception
		    		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
		    		updateIntent.putExtra("notification", "Connection: "+se.getMessage()); // logging
		    		updateIntent.putExtra("connectionStatus", false);
		    		sendBroadcast(updateIntent);
		    		stopSelf();
				} catch (Exception e) {
					// any other exception
		    		updateIntent = new Intent();//DualAdvancerControlActivity.NEW_TRANSMISSION);
		    		updateIntent.putExtra("notification", "Error: "+e.getMessage()); // logging
		    		updateIntent.putExtra("connectionStatus", false);
		    		sendBroadcast(updateIntent);
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

		Toast.makeText(this, "Service Disconnected", Toast.LENGTH_SHORT).show();
	}
}

