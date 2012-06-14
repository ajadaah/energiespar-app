package de.hska.rbmk.zaehlerstand;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import de.hska.rbmk.R;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.ToggleButton;


public class ZaehlerErinnerungActivity extends Activity {

	private EditText datumEditText, uhrzeitEditText;
	private CheckBox erinnerungWiederholenCB;
	private LinearLayout hiddenLL;
	private ToggleButton toggleButtonErinnerung;
	private Spinner spinner_wiederholungszeitraum;

	private int mYear;
	private int mMonth;
	private int mDay;
	
	private int mHour;
	private int mMinute;
	
	private String datumString, uhrzeitString;

	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;
	
	SharedPreferences.Editor editor;
	SharedPreferences prefs;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zaehler_erinnerung);

		editor = getPreferences(0).edit();
		prefs = getPreferences(0);
		
		// capture our View elements
		datumEditText = (EditText) findViewById(R.id.editText1);
		uhrzeitEditText = (EditText) findViewById(R.id.editText2);
		erinnerungWiederholenCB = (CheckBox) findViewById(R.id.erinnerungWiederholenCB);
		hiddenLL = (LinearLayout) findViewById(R.id.wiederholungsZeitraumLayout);
		toggleButtonErinnerung = (ToggleButton) findViewById(R.id.toggleButtonErinnerung);
		spinner_wiederholungszeitraum = (Spinner) findViewById(R.id.spinner_wiederholungszeitraum);
		
		datumEditText.setOnTouchListener(editText_OnTouchDate);
		uhrzeitEditText.setOnTouchListener(editText_OnTouchTime);
		
		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		
		
		datumString = (String) DateFormat.format("E, MMMM dd, yyyy", Calendar.getInstance());
		uhrzeitString = (String) DateFormat.format("hh:mm", Calendar.getInstance());
		
		
		datumEditText.setKeyListener(null);
		uhrzeitEditText.setKeyListener(null);

		
		// display the current date
		updateDisplay();
	}
	
	

	@Override
	protected void onResume() {
		// Wiederherstellung der alten Einstellungen
		spinner_wiederholungszeitraum.setSelection(prefs.getInt("spinnerWiederholungszeitraum",0));
		erinnerungWiederholenCB.setChecked(prefs.getBoolean("wiederholungAktiv",false));
		
		// Beim Wechsel der Orientierung
    	if (erinnerungWiederholenCB.isChecked())
    	{
    		hiddenLL.setVisibility(LinearLayout.VISIBLE);
    	}
    	
		super.onResume();
	}



	public void onClickToggleButtonErinnerung(View v) {
    	if (toggleButtonErinnerung.isChecked())
    	{
    		// set alarm
    	}
    	else
    	{
    		// remove alarm
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
	                mTimeSetListener, mHour, mMinute, false);
	    }
	    return null;
	}
	
    // updates the date we display in the TextView
    private void updateDisplay() {
		datumEditText.setText(datumString);
		uhrzeitEditText.setText(uhrzeitString);
    }
    
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
    	public void onDateSet(DatePicker view, int year, 
    			int monthOfYear, int dayOfMonth) {
    		
    		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("CEST"));
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
    		
    		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("CEST"));
    		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
    		
    		uhrzeitString = DateFormat.format("hh:mm a", cal).toString();
    		
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
		editor.commit();
		super.onPause();
	}
    
    
}
