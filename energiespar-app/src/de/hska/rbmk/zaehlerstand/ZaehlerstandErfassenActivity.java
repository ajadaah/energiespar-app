package de.hska.rbmk.zaehlerstand;


import java.util.Date;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import de.hska.rbmk.datenVerwaltung.*;
import de.hska.rbmk.StartbildschirmActivity;
import de.hska.rbmk.R;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class ZaehlerstandErfassenActivity extends ListActivity {
    

	private SQLiteDatabase db;
//	private MeterNumbersOpenHelper mHelper;
	private DbAdapter dbAdapter;
    
	private static final int CONTEXT_DELETE = 1;

	private long contextSelection = -1;
	
	private boolean wheelScrolled = false;
	private boolean valueChanged = false;
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.zaehler_erfassen_uebersicht);
		
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(R.string.title_zaehleruebersicht);
		
		registerForContextMenu(getListView());
		
		dbAdapter = new DbAdapter(this);
		
//		mHelper = new MeterNumbersOpenHelper(this);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.zaehler_uebersicht_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case R.id.menu_erinnerung_hinzufuegen:
		    {
		    	// app icon in action bar clicked; go home
		    	Intent intent = new Intent(this, ZaehlerErinnerungActivity.class);
		    	startActivity(intent);
		    	return true;
		    }
	        case R.id.menu_zaehler_hinzufuegen:
	        {
	            LayoutInflater factory = LayoutInflater.from(this);
	            final View textEntryView = factory.inflate(R.layout.dialog_zaehler_hinzugfuegen, null);
	            final EditText et = (EditText) textEntryView.findViewById(R.id.eingetippte_zaehlernummer);
	            final Spinner auswahl = (Spinner) textEntryView.findViewById(R.id.zaehlerTypAuswahl);
	            
	        	Builder builder = new Builder(this);
	        	builder
	                .setIcon(android.R.drawable.ic_input_add)
	                .setTitle(getString(R.string.menu_zaehlerHinzufuegen))
	                .setView(textEntryView)
	                .setPositiveButton(R.string.dialog_hinzufuegen, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                    	if (!et.getText().toString().isEmpty()) {
		        				ContentValues values = new ContentValues();
		        				values.put(DbAdapter.KEY_NUMBER, et.getText().toString());
		        				values.put(DbAdapter.KEY_METERTYPE, auswahl.getSelectedItemPosition());	    
		        				db.insert(DbAdapter.TABLE_METERNUMBERS_NAME, null, values);
		        				loadData();
	                    	}
	                    	else
	                    	{
                				// Vibrate
                				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                				v.vibrate(300);

                				// Toast
                				Toast.makeText(getApplicationContext(),
                						"Fehler: Keine Zählernummer eingegeben",
                						Toast.LENGTH_LONG).show();
	                    	}
	                    }
	                })
	                .show();
	            return true;
	        }
	        case android.R.id.home:
	        {
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, StartbildschirmActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        }
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    contextSelection = info.id;
	    
	    menu.setHeaderTitle(getString(R.string.aktionen));

	    //menu.setHeaderTitle(ctx_menu_meternumber);   
	    menu.add(0, CONTEXT_DELETE, 0, getString(R.string.loeschen));
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case CONTEXT_DELETE:
			db.delete(DbAdapter.TABLE_METERNUMBERS_NAME, DbAdapter.KEY_ID + "=" + contextSelection, null);
			loadData();
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		TextView tv_number = (TextView) v.findViewById(R.id.list_zaehlernummer);
		ImageView iv_type = (ImageView) v.findViewById(R.id.list_zaehlertyp_icon);
		erfasseNeuenZaehlerwertDialog(tv_number, iv_type);

		super.onListItemClick(l, v, position, id);
	}

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
    
	@Override
	protected void onPause() {
		dbAdapter.close();
		super.onPause();
	}

	@Override
	protected void onResume() {
		dbAdapter.open();
		db = dbAdapter.getDb();
		loadData();

		super.onResume();
	}
	
	private void erfasseNeuenZaehlerwertDialog(final TextView tv_number, final ImageView iv_type)
	{
		final MeterType zaehlerArt = MeterType.values()[Integer.parseInt(String.valueOf(iv_type.getContentDescription()))];
		final int zaehlerNummer = Integer.parseInt(String.valueOf(tv_number.getText()));
		
		DbAdapter dbAdapter = new DbAdapter(getApplicationContext());
		dbAdapter.open();
		final long letzterWert = dbAdapter.getLastMeterReadingValueForMeterNumber(zaehlerNummer);
		dbAdapter.close();		
		
		final WheelView w1, w2, w3, w4, w5, w6, w7;
		
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.dialog_zaehlerstand_erfassen, null);
        
        w1 = (WheelView) textEntryView.findViewById(R.id.w_1);
        w2 = (WheelView) textEntryView.findViewById(R.id.w_2);
        w3 = (WheelView) textEntryView.findViewById(R.id.w_3);
        w4 = (WheelView) textEntryView.findViewById(R.id.w_4);
        w5 = (WheelView) textEntryView.findViewById(R.id.w_5);
        w6 = (WheelView) textEntryView.findViewById(R.id.w_6);
        w7 = (WheelView) textEntryView.findViewById(R.id.w_7);
        
        String letzterWertString = String.valueOf(letzterWert);
        //add leading zeros
        while(letzterWertString.length() < 7) {
        	letzterWertString = "0" + letzterWertString;
        }
        //TODO affirm that length is 7
    	initWheel(w1, Character.getNumericValue(letzterWertString.charAt(0)), false);
    	initWheel(w2, Character.getNumericValue(letzterWertString.charAt(1)), false);
    	initWheel(w3, Character.getNumericValue(letzterWertString.charAt(2)), false);
    	initWheel(w4, Character.getNumericValue(letzterWertString.charAt(3)), false);
    	initWheel(w5, Character.getNumericValue(letzterWertString.charAt(4)), false);
    	initWheel(w6, Character.getNumericValue(letzterWertString.charAt(5)), false);
    	initWheel(w7, Character.getNumericValue(letzterWertString.charAt(6)), true);
        
    	Builder builder = new Builder(this);
    	builder
            .setIcon(iv_type.getDrawable())
            .setTitle(getString(R.string.title_zaehlerstanderfassen))
            .setView(textEntryView)
            .setPositiveButton(R.string.menu_zaehlerstand_speichern, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	int zaehlerWert = 
                				w1.getCurrentItem() * 1000000 +
                				w2.getCurrentItem() * 100000 +
                				w3.getCurrentItem() * 10000 +
                				w4.getCurrentItem() * 1000 +
                				w5.getCurrentItem() * 100 +
                				w6.getCurrentItem() * 10 +
                				w7.getCurrentItem() * 1;
                			
                			if (zaehlerWert >= letzterWert) {
                				DbAdapter mRDBA = new DbAdapter(getApplicationContext());
                	        	
                	        	Date timeStamp = new Date(System.currentTimeMillis());
                				
                				mRDBA.open();
                				mRDBA.addReading("", timeStamp, zaehlerNummer, zaehlerWert , zaehlerArt, valueChanged, false);
                				mRDBA.close();
                				
                				//TODO better close activity

                				loadData();
                				
//                				TODO: Sync
//                				Intent serviceIntent = new Intent(getApplicationContext(), SynchronizationService.class);
//                	        	startService(serviceIntent);
//                				Intent intent = new Intent(view.getContext(), ElectricMeterActivity.class);
//                				startActivityForResult(intent, 0);
                			}
                			else { // Der Wert ist kleiner als der zuletzt erfasste Zählerstand
                				
                				// Vibrate
                				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                				v.vibrate(300);

                				// Toast
                				Toast.makeText(getApplicationContext(),
                						"Fehler: Der gewählte Wert " + zaehlerWert + 
                						" ist kleiner als der zuletzt erfasste Wert " +	letzterWert,
                						Toast.LENGTH_LONG).show();
                			}

            }})
            .show();
	}
	
	private void loadData() {
		Cursor zaehlerCursor = db.query(
				DbAdapter.TABLE_METERNUMBERS_NAME,							// Tabellenname
				new String[] { 						// anzuzeigende Spalten
						DbAdapter.KEY_ID,
						DbAdapter.KEY_NUMBER,
						DbAdapter.KEY_METERTYPE
				}, 
				null, 								// WHERE (z.B. "_id = ?")
				null, 								// WHERE-Argumente (für "?")
				null, 								// GROUP BY
				null, 								// HAVING
				DbAdapter.KEY_NUMBER	// ORDER BY
			);

		CursorAdapter zaehlerAdapter = new CursorAdapter(this, zaehlerCursor, false) {
			
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				LayoutInflater inflater = LayoutInflater.from(context);
				View v = inflater.inflate(R.layout.list_item, parent, false);
				bindView(v, context, cursor);
				return v;
			}
			
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				TextView zaehlerNummer = (TextView)view.findViewById(R.id.list_zaehlernummer);
				TextView zaehlerLetzterStand = (TextView)view.findViewById(R.id.list_letzterZaehlerstand);
				TextView zaehlerLetztesUpdate = (TextView)view.findViewById(R.id.list_letztesUpdate);
				ImageView zaehlerTypIcon = (ImageView)view.findViewById(R.id.list_zaehlertyp_icon);

				String zaehlerNummerText = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_NUMBER));
				int zaehlerTyp = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_METERTYPE));


				DbAdapter mRDBA = new DbAdapter(getApplicationContext());

				mRDBA.open();
				long letzterStand = mRDBA.getLastMeterReadingValueForMeterNumber(Integer.parseInt(zaehlerNummerText));
				long letztesUpdate = mRDBA.getLastMeterReadingDateForMeterNumber(Integer.parseInt(zaehlerNummerText));
				mRDBA.close();

				MeterType zaehlerArt = MeterType.values()[zaehlerTyp];

				switch (zaehlerArt)
				{
				case ELECTRICITY: {
					zaehlerTypIcon.setImageResource(R.drawable.ic_type_electricity);
					break; 
				}
				case WATER: {
					zaehlerTypIcon.setImageResource(R.drawable.ic_type_water);
					break; 
				}
				case GAS: {
					zaehlerTypIcon.setImageResource(R.drawable.ic_type_gas);
					break; 
				}
				}
				zaehlerTypIcon.setContentDescription(String.valueOf(zaehlerTyp));

				zaehlerNummer.setText(zaehlerNummerText);


				if (letztesUpdate == 0)
				{
					zaehlerLetzterStand.setText(R.string.list_item_keine_werte_erfasst);
					zaehlerLetztesUpdate.setText(R.string.list_item_blank);
				}
				else
				{
					String letztesUpdateText = (String) DateFormat.format("dd.MM.yyyy kk:mm", letztesUpdate);
					zaehlerLetztesUpdate.setText(getString(R.string.list_item_letzte_ablesung).replace("%d", letztesUpdateText));
					zaehlerLetzterStand.setText(getString(R.string.list_item_stand).replace("%v", String.valueOf(letzterStand)));
				}


			}
		};
		
		setListAdapter(zaehlerAdapter);
	}
}