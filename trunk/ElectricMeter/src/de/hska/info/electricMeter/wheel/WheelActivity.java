package de.hska.info.electricMeter.wheel;

import java.util.Date;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import de.hska.info.electricMeter.Constants;
import de.hska.info.electricMeter.ElectricMeterActivity;
import de.hska.info.electricMeter.R;
import de.hska.info.electricMeter.dataStorage.MeterReadingsDbAdapter;
import de.hska.info.electricMeter.dataStorage.MeterType;
import de.hska.info.electricMeter.syncService.SynchronizationService;

public class WheelActivity extends Activity {
	
	//flags
	private boolean wheelScrolled = false;
	private boolean valueChanged = false;
	
	private int meterNumber = -1;
	
	private long lastSavedValuefromDB;
	
	private WheelView w1;
	private WheelView w2;
	private WheelView w3;
	private WheelView w4;
	private WheelView w5;
	private WheelView w6;
	private WheelView w7;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wheel);

        
        meterNumber = getIntent().getExtras().getInt(
				Constants.METERNUMBER);
        
        MeterReadingsDbAdapter dbAdapter = new MeterReadingsDbAdapter(getApplicationContext());
        dbAdapter.open();
        lastSavedValuefromDB = dbAdapter.getLastMeterReadingValueForMeterNumber(meterNumber);
        TextView lastValueDisplay = (TextView) this.findViewById(R.id.lastSavedValueDisplay);
        lastValueDisplay.setText(this.getString(R.string.last_saved_Value) + lastSavedValuefromDB);
        dbAdapter.close();
        
        String string = getIntent().getExtras().getString(
				Constants.METERVALUE);
        
        Log.i("OCR_RECOGNIZED_VALUE", string);
        
        //add leading zeros
        while(string.length() < 7) {
        	string = "0" + string;
        }


        w1 = (WheelView) findViewById(R.id.w_1);
        w2 = (WheelView) findViewById(R.id.w_2);
        w3 = (WheelView) findViewById(R.id.w_3);
        w4 = (WheelView) findViewById(R.id.w_4);
        w5 = (WheelView) findViewById(R.id.w_5);
        w6 = (WheelView) findViewById(R.id.w_6);
        w7 = (WheelView) findViewById(R.id.w_7);
        
        //TODO affirm that length is 7
        
    	initWheel(w1, Character.getNumericValue(string.charAt(0)), false);
    	initWheel(w2, Character.getNumericValue(string.charAt(1)), false);
    	initWheel(w3, Character.getNumericValue(string.charAt(2)), false);
    	initWheel(w4, Character.getNumericValue(string.charAt(3)), false);
    	initWheel(w5, Character.getNumericValue(string.charAt(4)), false);
    	initWheel(w6, Character.getNumericValue(string.charAt(5)), false);
    	initWheel(w7, Character.getNumericValue(string.charAt(6)), true);

    	
        Button sendButton = (Button) findViewById(R.id.btn_send);
        sendButton.setOnClickListener(onSendButtonClickListener);

        Button backButton = (Button) findViewById(R.id.btn_back);
        backButton.setOnClickListener(onBackButtonClickListener);
    }
    
    
    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
            wheelScrolled = true;
        }
        public void onScrollingFinished(WheelView wheel) {
            wheelScrolled = false;
            //do action
        }
    };
    

    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
    	
		public void onChanged(WheelView wheel, int oldValue, int newValue) {
            if (!wheelScrolled) {
            	valueChanged = true;
            }
        }
    };
    
    private OnClickListener onSendButtonClickListener = new OnClickListener() {
		
		public void onClick(View view) {
			int meterValue = 
				w1.getCurrentItem() * 1000000 +
				w2.getCurrentItem() * 100000 +
				w3.getCurrentItem() * 10000 +
				w4.getCurrentItem() * 1000 +
				w5.getCurrentItem() * 100 +
				w6.getCurrentItem() * 10 +
				w7.getCurrentItem() * 1 ;
			
			if (meterValue >= lastSavedValuefromDB) {
				MeterReadingsDbAdapter mRDBA = new MeterReadingsDbAdapter(getApplicationContext());
	        	
	        	Date timeStamp = new Date(System.currentTimeMillis());				
				
				mRDBA.open();
				mRDBA.addReading("", timeStamp, meterNumber, meterValue , MeterType.ELECTRICITY, valueChanged, false);
				mRDBA.close();
				
				//TODO better close activity

				Intent serviceIntent = new Intent(getApplicationContext(), SynchronizationService.class);
	        	startService(serviceIntent);
				Intent intent = new Intent(view.getContext(), ElectricMeterActivity.class);
				startActivityForResult(intent, 0);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
				builder.setTitle("Eingegebener Wert zu klein!")
						.setMessage("Der gewählte Wert " + meterValue + " ist kleiner als der zuletzt erfasste Wert " 
						+ lastSavedValuefromDB + ", er muss aber mindestens gleich hoch sein! Bitte korrigieren.")
				       .setCancelable(false)
				       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}
        				
		}
	};

    
    private OnClickListener onBackButtonClickListener = new OnClickListener() {
		
		public void onClick(View view) {
			//remove activity from activity stack (same action as android-back-button)
			WheelActivity.this.finish();
		}
    };

   /**
     * Initializes wheel
     * @param id the wheel widget Id
     * @param value the initial value
     */
    private void initWheel(WheelView wheel, int digit, boolean isRed) {
        NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(this, 0, 9);
        numericWheelAdapter.setTextSize(35);
        wheel.setViewAdapter(numericWheelAdapter);
        wheel.setVisibleItems(3);

        wheel.setCurrentItem(digit);
        if(isRed) {
        	wheel.setBackgroundResource(R.drawable.wheel_bg_red);
        } else {
        	wheel.setBackgroundResource(R.drawable.wheel_bg);
        }
        
        wheel.addChangingListener(changedListener);
        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setInterpolator(new AnticipateOvershootInterpolator());
    }
}