package de.hska.rbmk.zaehlerstand;

import de.hska.rbmk.Constants;
import de.hska.rbmk.R;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class AlarmEmpfaenger extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Bundle bundle = intent.getExtras();
			boolean wiederholungAktiv = bundle.getBoolean("wiederholungAktiv", false);
			boolean vibrationAktiv = bundle.getBoolean("vibrationAktiv", false);
			String app_name = bundle.getString("app_name", "App_Name");
			NotifierHelper.sendNotification(context, app_name, wiederholungAktiv, vibrationAktiv);
			
		} catch (Exception e) {
			Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
			e.printStackTrace();

		}
	}
	public static class NotifierHelper {
	    private static final int NOTIFY_1 = 0x1001;
	    
	    public static void sendNotification(Context caller, String app_name, boolean wiederholungAktiv, boolean vibrationAktiv) {
			if (!wiederholungAktiv)
			{
				Intent alarmIntent = new Intent(caller, AlarmEmpfaenger.class);
				PendingIntent sender = PendingIntent.getBroadcast(caller, Constants.ALARM_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				AlarmManager alarmManager = (AlarmManager) caller.getSystemService(Context.ALARM_SERVICE);
				alarmManager.cancel(sender);
				sender.cancel();
			}
	    	
	        NotificationManager notifier = (NotificationManager) caller.getSystemService(Context.NOTIFICATION_SERVICE);

	        final Notification notify = new Notification(R.drawable.ic_launcher, "", System.currentTimeMillis());

	        notify.icon = R.drawable.ic_launcher;
	        notify.tickerText = "Erinnerung: Zählerstand erfassen";
	        notify.flags = Notification.FLAG_AUTO_CANCEL;

	        // Vibration
	        if (vibrationAktiv)
	        	notify.defaults = Notification.DEFAULT_ALL;
	        else
		        notify.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;

	        Intent toLaunch = new Intent(caller, ZaehlerstandErfassenActivity.class);
	        PendingIntent intentBack = PendingIntent.getActivity(caller, 0, toLaunch, 0);

	        notify.setLatestEventInfo(caller, app_name, "Antippen, um Zählerstand zu erfassen", intentBack);
	        notifier.notify(NOTIFY_1, notify);
	    }
	}
}

