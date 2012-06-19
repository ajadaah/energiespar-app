package de.hska.rbmk.zaehlerstand;

import java.util.Calendar;

import de.hska.rbmk.Constants;
import de.hska.rbmk.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.Toast;

public class AlarmService extends Service {
	
	Calendar erinnerungsZeit, jetzt;
	boolean vibrationAktiv;
	boolean wiederholungAktiv;
	int wiederholungsZeitraum;
	
	SharedPreferences prefs;
	SharedPreferences.Editor editor;
	
	//compat to support older devices
	@Override
	public void onStart(Intent intent, int startId) {
		onStartCommand(intent, 0, startId);
	}


	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		prefs = getSharedPreferences(Constants.SHARED_PREFERENCES,MODE_PRIVATE);
		editor = getSharedPreferences(Constants.SHARED_PREFERENCES,MODE_PRIVATE).edit();
		
		sucheNachErinnerung();
		// TODO: evtl immer nach 24h checken?
		
		return startId;
	}

	private void sucheNachErinnerung() {
		erinnerungsZeit = Calendar.getInstance();
		jetzt = Calendar.getInstance();

		// Hole nötige Werte durch SharedPreference
		erinnerungsZeit.setTimeInMillis(prefs.getLong("letzteErrinerungsZeit", 0));
		vibrationAktiv = prefs.getBoolean("vibrationAktiv",false);
		wiederholungAktiv = prefs.getBoolean("wiederholungAktiv",false);
		wiederholungsZeitraum = prefs.getInt("wiederholungsZeitraum",1);

		if (jetzt.getTimeInMillis() < erinnerungsZeit.getTimeInMillis())
		{

			long zeitunterschied = erinnerungsZeit.getTimeInMillis() - jetzt.getTimeInMillis();

			if ((zeitunterschied / 1000 / 60 / 60 / 24) < 1) // ist der Zeitpunkt heute? (in den nächsten 24h)
			{
				Intent alarmIntent = new Intent(this, AlarmEmpfaenger.class);
				alarmIntent.putExtra("wiederholungAktiv", wiederholungAktiv);
				alarmIntent.putExtra("vibrationAktiv", vibrationAktiv);
				alarmIntent.putExtra("app_name", getString(R.string.app_name));

				PendingIntent sender = PendingIntent.getBroadcast(this, Constants.ALARM_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

				alarmManager.set(AlarmManager.RTC_WAKEUP, erinnerungsZeit.getTimeInMillis(), sender);

				if (wiederholungAktiv)
				{
					erinnerungsZeit.add(Calendar.DAY_OF_YEAR, wiederholungsZeitraum);
					editor.putLong("letzteErrinerungsZeit", erinnerungsZeit.getTimeInMillis());
					editor.commit();
					planeNeustartDesDienstes();
				}
			}
			else // wenn nicht, dann starte die Prüfung nochmal in 24 Stunden
			{
				planeNeustartDesDienstes();
			}
		}
	}
	

	private void planeNeustartDesDienstes() {
		// erneutes Ausführen des Dienstes
		Intent serviceIntent = new Intent(this,AlarmService.class);
		PendingIntent restartServiceIntent = PendingIntent.getService(this, Constants.ALARM_SERVICE_CODE, serviceIntent,0);
		AlarmManager alarms = (AlarmManager)getSystemService(ALARM_SERVICE);
		// stoppe alle alte Erinnerungen
		alarms.cancel(restartServiceIntent);
		// plane Alarm für morgen
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);

		// plane alarm
		alarms.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), restartServiceIntent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}