package de.hska.rbmk.zaehlerstand;

import de.hska.rbmk.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

public class AlarmEmpfaenger extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Bundle bundle = intent.getExtras();
			String message = bundle.getString("alarm_message");
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			NotifierHelper.sendNotification(context);
		} catch (Exception e) {
			Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
			e.printStackTrace();

		}
	}
	public static class NotifierHelper {
	    private static final int NOTIFY_1 = 0x1001;
	    public static void sendNotification(Context caller) {
	        NotificationManager notifier = (NotificationManager) caller.getSystemService(Context.NOTIFICATION_SERVICE);

	        final Notification notify = new Notification(R.drawable.ic_launcher, "", System.currentTimeMillis());

	        notify.icon = R.drawable.ic_launcher;
	        notify.tickerText = "Erinnerung: Zählerstand erfassen";
	        notify.when = System.currentTimeMillis();
	        notify.flags |= Notification.FLAG_AUTO_CANCEL;

//	        notify.vibrate = new long[] {100, 200, 200, 200, 200, 200, 1000, 200, 200, 200, 1000, 200};

	        Intent toLaunch = new Intent(caller, ZaehlerstandErfassenActivity.class);
	        PendingIntent intentBack = PendingIntent.getActivity(caller, 0, toLaunch, 0);

	        notify.setLatestEventInfo(caller, "Energiespar App", "Antippen, um Zählerstand zu erfassen", intentBack);
	        notifier.notify(NOTIFY_1, notify);
	    }
	}
}

