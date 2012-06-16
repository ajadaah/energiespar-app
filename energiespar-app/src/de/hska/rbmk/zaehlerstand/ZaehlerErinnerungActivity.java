package de.hska.rbmk.zaehlerstand;
import java.util.Calendar;
import de.hska.rbmk.Constants;
import de.hska.rbmk.R;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;


public class ZaehlerErinnerungActivity extends Activity {

	private EditText datumEditText, uhrzeitEditText;
	private CheckBox erinnerungWiederholenCB, vibrationCB;
	private LinearLayout hiddenLL;
	private ToggleButton toggleButtonErinnerung;
	private Spinner spinner_wiederholungszeitraum;
	private TextView naechsteErinnerungTV;
	
	private int mYear;
	private int mMonth;
	private int mDay;
	
	private int mHour;
	private int mMinute;
	
	private String datumString, uhrzeitString, naechsteErinnerungString;
	private int[] wiederholungsZeitraum;
	private long letzteErrinerungsZeit;
	
	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;
	
	SharedPreferences.Editor editor;
	SharedPreferences prefs;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zaehler_erinnerung);

	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(R.string.title_zaehlererinnerung);
	    actionBar.setIcon(R.drawable.ic_alarm);
		
		editor = getPreferences(0).edit();
		prefs = getPreferences(0);
		
		// capture our View elements
		datumEditText = (EditText) findViewById(R.id.editText1);
		uhrzeitEditText = (EditText) findViewById(R.id.editText2);
		erinnerungWiederholenCB = (CheckBox) findViewById(R.id.erinnerungWiederholenCB);
		vibrationCB = (CheckBox) findViewById(R.id.vibrationCB);
		hiddenLL = (LinearLayout) findViewById(R.id.wiederholungsZeitraumLayout);
		toggleButtonErinnerung = (ToggleButton) findViewById(R.id.toggleButtonErinnerung);
		spinner_wiederholungszeitraum = (Spinner) findViewById(R.id.spinner_wiederholungszeitraum);
		naechsteErinnerungTV = (TextView) findViewById(R.id.naechsteErinnerungTV);
		
		datumEditText.setOnTouchListener(editText_OnTouchDate);
		uhrzeitEditText.setOnTouchListener(editText_OnTouchTime);
		
		datumEditText.setKeyListener(null);
		uhrzeitEditText.setKeyListener(null);
	}
	


	private boolean checkAlarmStatus() {
		return (PendingIntent.getBroadcast(this, Constants.ALARM_REQUEST_CODE, 
		        new Intent(this, AlarmEmpfaenger.class), 
		        PendingIntent.FLAG_NO_CREATE) != null);
	}

	@Override
	protected void onPause() {
		int selectedPosition = spinner_wiederholungszeitraum.getSelectedItemPosition();
		editor.putInt("spinnerWiederholungszeitraum", selectedPosition);
		editor.putBoolean("wiederholungAktiv", erinnerungWiederholenCB.isChecked());
		editor.putBoolean("vibrationAktiv", vibrationCB.isChecked());
		editor.putString("naechsteErinnerungString", naechsteErinnerungString);
		editor.commit();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		// get the current date
		final Calendar jetzt = Calendar.getInstance();
		mYear = jetzt.get(Calendar.YEAR);
		mMonth = jetzt.get(Calendar.MONTH);
		mDay = jetzt.get(Calendar.DAY_OF_MONTH);
		mHour = jetzt.get(Calendar.HOUR_OF_DAY);
		mMinute = jetzt.get(Calendar.MINUTE);
		
		wiederholungsZeitraum = getResources().getIntArray(R.array.spinner_alarm_entry_values);
		
		String h = String.valueOf(jetzt.get(Calendar.HOUR_OF_DAY));
		if (h.length() == 1)
			h = "0" + h;
		
		String min = String.valueOf(jetzt.get(Calendar.MINUTE));
		if (min.length() == 1)
			min = "0" + min;
		
		uhrzeitString = h + ":" + min + " Uhr";
		datumString = (String) DateFormat.format("E, MMMM dd, yyyy", jetzt);
		
		// Wiederherstellung der alten Einstellungen
		spinner_wiederholungszeitraum.setSelection(prefs.getInt("spinnerWiederholungszeitraum",0));
		erinnerungWiederholenCB.setChecked(prefs.getBoolean("wiederholungAktiv",false));
		vibrationCB.setChecked(prefs.getBoolean("vibrationAktiv",false));
		naechsteErinnerungString = prefs.getString("naechsteErinnerungString","");
		letzteErrinerungsZeit = prefs.getLong("letzteErrinerungsZeit",0);
		
		boolean alarmAktiv = checkAlarmStatus();
		
		toggleButtonErinnerung.setChecked(alarmAktiv);
		
		if (!alarmAktiv)
		{
			naechsteErinnerungString = "";
		}
		else
		{
			Calendar alteErinnerung = Calendar.getInstance();
			alteErinnerung.setTimeInMillis(letzteErrinerungsZeit);
			
			Log.d("alteErinnerung", (String) DateFormat.format("MMMM dd, yyyy hh:mm:ss a", alteErinnerung));
			Log.d("jetzt", (String) DateFormat.format("MMMM dd, yyyy hh:mm:ss a", jetzt));
			
			if (alteErinnerung.getTimeInMillis() < jetzt.getTimeInMillis()) 
			{
				long zeitunterschied = jetzt.getTimeInMillis() - alteErinnerung.getTimeInMillis();
				int interval = 1000 * 60 * 60 * 24 * wiederholungsZeitraum[spinner_wiederholungszeitraum.getSelectedItemPosition()];
				long anzahl_der_intervalle = (zeitunterschied / interval)+1;

				alteErinnerung.add(Calendar.HOUR, (int) (anzahl_der_intervalle * 24 * wiederholungsZeitraum[spinner_wiederholungszeitraum.getSelectedItemPosition()]));
				
				Log.d("alteErinnerung korrigiert", (String) DateFormat.format("MMMM dd, yyyy hh:mm:ss a", alteErinnerung));
				
				naechsteErinnerungString = naechsteErinnerungTextGenerieren(alteErinnerung);
			}
		}
		
		// Beim Wechsel der Orientierung
    	if (erinnerungWiederholenCB.isChecked())
    	{
    		hiddenLL.setVisibility(LinearLayout.VISIBLE);
    	}
    	
    	updateDisplay();
    	
		super.onResume();
	}



	public void onClickToggleButtonErinnerung(View v) {
		if (toggleButtonErinnerung.isChecked())
		{
			// get a Calendar object with current time
			Calendar erinnerungsZeit = Calendar.getInstance();
			// add 5 seconds to the calendar object
//			erinnerungsZeit.add(Calendar.SECOND, 5);

			// set alarm
			erinnerungsZeit.set(mYear, mMonth, mDay, mHour, mMinute , 0);

			// is date in the future?
			Calendar jetzt = Calendar.getInstance();

			if (jetzt.getTimeInMillis() < erinnerungsZeit.getTimeInMillis()) {
				Intent alarmIntent = new Intent(this, AlarmEmpfaenger.class);
				alarmIntent.putExtra("wiederholungAktiv", erinnerungWiederholenCB.isChecked());
				alarmIntent.putExtra("vibrationAktiv", vibrationCB.isChecked());

				PendingIntent sender = PendingIntent.getBroadcast(this, Constants.ALARM_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

				if (erinnerungWiederholenCB.isChecked())
				{
					alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, erinnerungsZeit.getTimeInMillis(), 1000 * 60 * 60 * 24 * wiederholungsZeitraum[spinner_wiederholungszeitraum.getSelectedItemPosition()], sender);
				}
				else
				{
					alarmManager.set(AlarmManager.RTC_WAKEUP, erinnerungsZeit.getTimeInMillis(), sender);
				}

				if (erinnerungsZeit != null)
				{

					naechsteErinnerungString = naechsteErinnerungTextGenerieren(erinnerungsZeit);
					
//					String stunde = String.valueOf(erinnerungsZeit.get(Calendar.HOUR_OF_DAY));
//					String minute = String.valueOf(erinnerungsZeit.get(Calendar.MINUTE));
//					if (minute.length() == 1)
//						minute = "0" + minute;
//					if (stunde.length() == 1)
//						stunde = "0" + stunde;
//
//
//					if ((jetzt.get(Calendar.YEAR) == erinnerungsZeit.get(Calendar.YEAR)) && (jetzt.get(Calendar.MONTH) == erinnerungsZeit.get(Calendar.MONTH)) && ((jetzt.get(Calendar.DAY_OF_MONTH) == erinnerungsZeit.get(Calendar.DAY_OF_MONTH) || jetzt.get(Calendar.DAY_OF_MONTH)+1 == erinnerungsZeit.get(Calendar.DAY_OF_MONTH))))
//					{
//						if (jetzt.get(Calendar.DAY_OF_MONTH) == erinnerungsZeit.get(Calendar.DAY_OF_MONTH))
//						{
//							naechsteErinnerungString = "heute um " + stunde + ":" + minute + " Uhr";
//						}
//						else if (jetzt.get(Calendar.DAY_OF_MONTH)+1 == erinnerungsZeit.get(Calendar.DAY_OF_MONTH))
//						{
//							naechsteErinnerungString = "morgen um " + stunde + ":" + minute + " Uhr";
//						}
//					}
//					else
//					{
//						naechsteErinnerungString = DateFormat.format("E, MMMM dd, yyyy", erinnerungsZeit).toString() + " " + stunde + ":" + minute + " Uhr";
//					}
					
					editor.putLong("letzteErrinerungsZeit", erinnerungsZeit.getTimeInMillis());
				}
			}
			else // Datum liegt in der Vergangenheit
			{
				// Toast
				Toast.makeText(getApplicationContext(),
						"Datum muss in der Zukunft liegen",
						Toast.LENGTH_LONG).show();

				toggleButtonErinnerung.setChecked(false);
				naechsteErinnerungString = "";
			}


		}
    	else
    	{
    		erinnerungLoeschen();
    	}
    	
    	updateDisplay();
	}
	
	public void onCheckBoxClickVibration(View v) {
		if (toggleButtonErinnerung.isChecked())
		{
			erinnerungLoeschen();
			toggleButtonErinnerung.setChecked(false);
		}
	}
	

	public void onCheckBoxClickWiederholung(View v) {
    	if (!erinnerungWiederholenCB.isChecked())
    	{
    		hiddenLL.setVisibility(LinearLayout.GONE);
    	}
    	else {
    		hiddenLL.setVisibility(LinearLayout.VISIBLE);
    	}
    	
		if (toggleButtonErinnerung.isChecked())
		{
			erinnerungLoeschen();
			toggleButtonErinnerung.setChecked(false);
		}
    }
	
	private View.OnTouchListener editText_OnTouchDate = new View.OnTouchListener() {
	    public boolean onTouch(View v, MotionEvent event) {
	        	showDialog(DATE_DIALOG_ID);
	        return false;
	    }
	};
	
	private View.OnTouchListener editText_OnTouchTime = new View.OnTouchListener() {
	    public boolean onTouch(View v, MotionEvent event) {
	        	showDialog(TIME_DIALOG_ID);
	        return false;
	    }
	};
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case DATE_DIALOG_ID:
	        return new DatePickerDialog(this,
	                    mDateSetListener,
	                    mYear, mMonth, mDay);
	    case TIME_DIALOG_ID:
	        return new TimePickerDialog(this,
	                mTimeSetListener, mHour, mMinute, true);
	    }
	    return null;
	}
	
    // updates the date we display in the TextView
    private void updateDisplay() {
		datumEditText.setText(datumString);
		uhrzeitEditText.setText(uhrzeitString);
		
		naechsteErinnerungTV.setText(naechsteErinnerungString);
    }
    
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
    	public void onDateSet(DatePicker view, int year, 
    			int monthOfYear, int dayOfMonth) {
    		
    		Calendar cal = Calendar.getInstance();
    		cal.set(year, monthOfYear, dayOfMonth);
    		
    		datumString = DateFormat.format("E, MMMM dd, yyyy", cal).toString();
    		
    		mYear = year;
    		mMonth = monthOfYear;
    		mDay = dayOfMonth;
    		
    		if (toggleButtonErinnerung.isChecked())
    		{
    			erinnerungLoeschen();
    			toggleButtonErinnerung.setChecked(false);
    		}
    		
    		updateDisplay();
    	}
    };
            
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
    		new TimePickerDialog.OnTimeSetListener() {
    	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    		
    		Calendar cal = Calendar.getInstance();
    		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
    		
			String h = String.valueOf(hourOfDay);
			if (h.length() == 1)
				h = "0" + h;
			
			String min = String.valueOf(minute);
			if (min.length() == 1)
				min = "0" + min;
    		
    		uhrzeitString = h + ":" + min + " Uhr";
    		
    		mHour = hourOfDay;
    		mMinute = minute;
    		
    		if (toggleButtonErinnerung.isChecked())
    		{
    			erinnerungLoeschen();
    			toggleButtonErinnerung.setChecked(false);
    		}
    		
    		updateDisplay();
    	}
    };
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        {
	            // app icon in action bar clicked; go back
	        	finish();
	            return true;
	        }
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private String naechsteErinnerungTextGenerieren(Calendar zeit)
	{
		Calendar jetzt = Calendar.getInstance();
		
		String returnString = "";
		
		String stunde = String.valueOf(zeit.get(Calendar.HOUR_OF_DAY));
		String minute = String.valueOf(zeit.get(Calendar.MINUTE));
		if (minute.length() == 1)
			minute = "0" + minute;
		if (stunde.length() == 1)
			stunde = "0" + stunde;


		if ((jetzt.get(Calendar.YEAR) == zeit.get(Calendar.YEAR)) && (jetzt.get(Calendar.MONTH) == zeit.get(Calendar.MONTH)) && ((jetzt.get(Calendar.DAY_OF_MONTH) == zeit.get(Calendar.DAY_OF_MONTH) || jetzt.get(Calendar.DAY_OF_MONTH)+1 == zeit.get(Calendar.DAY_OF_MONTH))))
		{
			if (jetzt.get(Calendar.DAY_OF_MONTH) == zeit.get(Calendar.DAY_OF_MONTH))
			{
				returnString = "heute um " + stunde + ":" + minute + " Uhr";
			}
			else if (jetzt.get(Calendar.DAY_OF_MONTH)+1 == zeit.get(Calendar.DAY_OF_MONTH))
			{
				returnString = "morgen um " + stunde + ":" + minute + " Uhr";
			}
		}
		else
		{
			returnString = DateFormat.format("E, MMMM dd, yyyy", zeit).toString() + " " + stunde + ":" + minute + " Uhr";
		}
		
		return returnString;
	}
	
	private void erinnerungLoeschen()
	{
		Intent alarmIntent = new Intent(this, AlarmEmpfaenger.class);
		
		PendingIntent sender = PendingIntent.getBroadcast(this, Constants.ALARM_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.cancel(sender);
		
		sender.cancel();
		
		naechsteErinnerungString = "";
		naechsteErinnerungTV.setText("");
	}
    
}
