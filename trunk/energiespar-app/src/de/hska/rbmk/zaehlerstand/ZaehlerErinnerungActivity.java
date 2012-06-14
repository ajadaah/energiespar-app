package de.hska.rbmk.zaehlerstand;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import de.hska.rbmk.Constants;
import de.hska.rbmk.R;
import de.hska.rbmk.StartbildschirmActivity;
import de.hska.rbmk.datenVerwaltung.MeterReadingsDbAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
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
	private CheckBox erinnerungWiederholenCB;
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
		hiddenLL = (LinearLayout) findViewById(R.id.wiederholungsZeitraumLayout);
		toggleButtonErinnerung = (ToggleButton) findViewById(R.id.toggleButtonErinnerung);
		spinner_wiederholungszeitraum = (Spinner) findViewById(R.id.spinner_wiederholungszeitraum);
		naechsteErinnerungTV = (TextView) findViewById(R.id.naechsteErinnerungTV);
		
		datumEditText.setOnTouchListener(editText_OnTouchDate);
		uhrzeitEditText.setOnTouchListener(editText_OnTouchTime);
		
		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);
		
		String h = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		if (h.length() == 1)
			h = "0" + h;
		
		String min = String.valueOf(c.get(Calendar.MINUTE));
		if (min.length() == 1)
			min = "0" + min;
		
		uhrzeitString = h + ":" + min + " Uhr";
		datumString = (String) DateFormat.format("E, MMMM dd, yyyy", c);
		
		datumEditText.setKeyListener(null);
		uhrzeitEditText.setKeyListener(null);
		
		updateDisplay();
	}
	
	

	@Override
	protected void onResume() {
		// Wiederherstellung der alten Einstellungen
		spinner_wiederholungszeitraum.setSelection(prefs.getInt("spinnerWiederholungszeitraum",0));
		erinnerungWiederholenCB.setChecked(prefs.getBoolean("wiederholungAktiv",false));
		naechsteErinnerungString = prefs.getString("naechsteErinnerungString","");
		
		// Beim Wechsel der Orientierung
    	if (erinnerungWiederholenCB.isChecked())
    	{
    		hiddenLL.setVisibility(LinearLayout.VISIBLE);
    	}
    	
    	if (toggleButtonErinnerung.isChecked())
    	{
    		updateDisplay();
    	}
    	
		super.onResume();
	}



	public void onClickToggleButtonErinnerung(View v) {
    	if (toggleButtonErinnerung.isChecked())
    	{
    		if (erinnerungWiederholenCB.isChecked())
    		{
    			// set repeat alarm
    		}
    		else
    		{
    			// get a Calendar object with current time
    			Calendar erinnerungsZeit = Calendar.getInstance();
    			// add 5 seconds to the calendar object
//    			erinnerungsZeit.add(Calendar.SECOND, 5);
    			
    			// set alarm
    			erinnerungsZeit.set(mYear, mMonth, mDay, mHour, mMinute);
    			
    			// is date in the future?
    			Calendar jetzt = Calendar.getInstance();
    			
    			if (jetzt.getTimeInMillis() < erinnerungsZeit.getTimeInMillis()) {
        			Intent intent = new Intent(this, AlarmEmpfaenger.class);
        			intent.putExtra("alarm_message", "Erinnerung!");
        			// In reality, you would want to have a static variable for the request code instead of 192837
        			PendingIntent sender = PendingIntent.getBroadcast(this, Constants.ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        			// Get the AlarmManager service
        			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        			am.set(AlarmManager.RTC_WAKEUP, erinnerungsZeit.getTimeInMillis(), sender);

        	    	if (erinnerungsZeit != null)
        	    	{
        				
        		    	
        				String stunde = String.valueOf(erinnerungsZeit.get(Calendar.HOUR_OF_DAY));
        				String minute = String.valueOf(erinnerungsZeit.get(Calendar.MINUTE));
        				if (minute.length() == 1)
        					minute = "0" + minute;
        				if (stunde.length() == 1)
        					stunde = "0" + stunde;
        				
        				
        				if ((jetzt.get(Calendar.YEAR) == erinnerungsZeit.get(Calendar.YEAR)) && (jetzt.get(Calendar.MONTH) == erinnerungsZeit.get(Calendar.MONTH)) && ((jetzt.get(Calendar.DAY_OF_MONTH) == erinnerungsZeit.get(Calendar.DAY_OF_MONTH) || jetzt.get(Calendar.DAY_OF_MONTH)+1 == erinnerungsZeit.get(Calendar.DAY_OF_MONTH))))
        				{
        						if (jetzt.get(Calendar.DAY_OF_MONTH) == erinnerungsZeit.get(Calendar.DAY_OF_MONTH))
        						{
        							naechsteErinnerungString = "heute um " + stunde + ":" + minute + " Uhr";
        						}
        						else if (jetzt.get(Calendar.DAY_OF_MONTH)+1 == erinnerungsZeit.get(Calendar.DAY_OF_MONTH))
        						{
        							naechsteErinnerungString = "morgen um " + stunde + ":" + minute + " Uhr";
        						}
        				}
        				else
        				{
        					naechsteErinnerungString = DateFormat.format("E, MMMM dd, yyyy", erinnerungsZeit).toString() + " " + stunde + ":" + minute + " Uhr";
        				}
        	    	}
    			}
    			else // Datum liegt in der Vergangenheit
    			{
    				// Vibrate
    				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    				vibrator.vibrate(300);

    				// Toast
    				Toast.makeText(getApplicationContext(),
    						"Fehler: Datum muss in der Zukunft liegen",
    						Toast.LENGTH_LONG).show();
    				
    				toggleButtonErinnerung.setChecked(false);
    			}
    			

    		}
    	}
    	else
    	{
    		// remove alarm
    		naechsteErinnerungString = "";
    	}
    	
    	updateDisplay();
	}


	public void onCheckBoxClickWiederholung(View v) {
    	if (!erinnerungWiederholenCB.isChecked())
    	{
    		hiddenLL.setVisibility(LinearLayout.GONE);
    	}
    	else {
    		hiddenLL.setVisibility(LinearLayout.VISIBLE);
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
    		updateDisplay();
    	}
    };



	@Override
	protected void onPause() {
		int selectedPosition = spinner_wiederholungszeitraum.getSelectedItemPosition();
		editor.putInt("spinnerWiederholungszeitraum", selectedPosition);
		editor.putBoolean("wiederholungAktiv", erinnerungWiederholenCB.isChecked());
		editor.putString("naechsteErinnerungString", naechsteErinnerungString);
		editor.commit();
		super.onPause();
	}
    
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
    
}
