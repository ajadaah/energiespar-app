package de.hska.rbmk.geraetevergleich;


import java.text.DecimalFormat;
import java.util.Date;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import de.hska.rbmk.datenVerwaltung.*;
import de.hska.rbmk.verbrauchsrechner.AuswertungWMActivity;
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
import android.net.Uri;
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


public class GeraetevergleichActivtiy extends ListActivity {
    

	private SQLiteDatabase db;
//	private MeterNumbersOpenHelper mHelper;
	private DbAdapter dbAdapter;
    
	private static final int CONTEXT_OPEN_BROWSER = 1;

	private long contextSelection = -1;
	
	private boolean wheelScrolled = false;
	private boolean valueChanged = false;
	
	public DecimalFormat f = new DecimalFormat("#0.00");
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.geraetevergleich_uebersicht);
		
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(R.string.title_geraetevergleich);
		
		registerForContextMenu(getListView());
		
		dbAdapter = new DbAdapter(this);
		
//		mHelper = new MeterNumbersOpenHelper(this);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.geraetevergleich_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case R.id.menu_strichcode_einlesen:
		    {
		    	// app icon in action bar clicked; go home
//		    	Intent intent = new Intent(this, ZaehlerErinnerungActivity.class);
//		    	startActivity(intent);
		    	return true;
		    }
	        case R.id.menu_filter_auswaehlen:
	        {
	            LayoutInflater factory = LayoutInflater.from(this);
	            final View textEntryView = factory.inflate(R.layout.dialog_wm_filter, null);
	            final Spinner auswahl = (Spinner) textEntryView.findViewById(R.id.sortierungsAuswahl);
	            
	        	Builder builder = new Builder(this);
	        	builder
	                .setIcon(android.R.drawable.ic_search_category_default)
	                .setTitle(getString(R.string.menu_filter))
	                .setView(textEntryView)
	                .setPositiveButton(R.string.dialog_hinzufuegen, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

//	        				ContentValues values = new ContentValues();
//	        				values.put(DbAdapter.KEY_NUMBER, et.getText().toString());
//	        				values.put(DbAdapter.KEY_METERTYPE, auswahl.getSelectedItemPosition());	    
//	        				db.insert(DbAdapter.TABLE_METERNUMBERS_NAME, null, values);
//	        				loadData();
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
	    menu.add(0, CONTEXT_OPEN_BROWSER, 0, getString(R.string.im_internet_suchen));
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case CONTEXT_OPEN_BROWSER:
			
			String auswahl = String.valueOf(contextSelection);
			
	    	String query = "SELECT "+DbAdapter.KEY_HERSTELLER+","+DbAdapter.KEY_MODELL+" FROM "+DbAdapter.TABLE_WM_NAME+" WHERE _id = ?";
	    	Cursor queryCursor = db.rawQuery(query, new String[] { auswahl });
	    	queryCursor.moveToFirst();
	    	
			String suchString = 
					queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_HERSTELLER))
					+ "+" +
					queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_MODELL));
			
	    	queryCursor.close();
	    	
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse("http://www.google.com/search?as_q=" + suchString));
			startActivity(i);
			
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		TextView tv_strichcode = (TextView) v.findViewById(R.id.gv_wm_list_item_strichcode);
		geraetAuswerten(tv_strichcode.getText().toString());

		super.onListItemClick(l, v, position, id);
	}
    
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
	
	private void geraetAuswerten(final String strichcode)
	{		
    	String query = "SELECT "+DbAdapter.KEY_STROMVERBRAUCH+","+DbAdapter.KEY_WASSERVERBRAUCH+","+DbAdapter.KEY_PREIS+" FROM "+DbAdapter.TABLE_WM_NAME+" WHERE "+DbAdapter.KEY_STRICHCODE+" = ?";
    	Cursor queryCursor = db.rawQuery(query, new String[] { strichcode });
    	queryCursor.moveToFirst();
    	
		String preis = queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_PREIS));
		String stromverbrauch = queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_STROMVERBRAUCH));
		String wasserverbrauch = queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_WASSERVERBRAUCH));
		
    	queryCursor.close();
		
		Intent ausrechnen = new Intent(this, AuswertungWMActivity.class);
		
		/*
		 * 
		 * 	        	ausrechnen.putExtra("g2_stromverbrauch", Float.valueOf(spinner_g2_stromverbrauch.getSelectedItem().toString()));
	        	ausrechnen.putExtra("g2_wasserverbrauch", Integer.valueOf(spinner_g2_wasserverbrauch.getSelectedItem().toString()));
	        	ausrechnen.putExtra("g2_anschaffungspreis", Float.valueOf(edittext_g2_anschaffungspreis.getText().toString()));
	    	}
	
	    	ausrechnen.putExtra("g1_stromverbrauch", Float.valueOf(spinner_g1_stromverbrauch.getSelectedItem().toString()));
	    	ausrechnen.putExtra("g1_wasserverbrauch", Integer.valueOf(spinner_g1_wasserverbrauch.getSelectedItem().toString()));
	    	ausrechnen.putExtra("g1_anschaffungspreis", Float.valueOf(edittext_g1_anschaffungspreis.getText().toString()));
	    	
	    	ausrechnen.putExtra("jahreseinsaetze", Integer.valueOf(edittext_jahreinsaetze.getText().toString()));
	    	ausrechnen.putExtra("stromkosten", Float.valueOf(edittext_stromkosten.getText().toString()));
	    	ausrechnen.putExtra("eigenesGeraet", cbEigenesGeraet.isChecked());
	    	
		 */
		
		int jahreseinsaetze = 244;
		float stromkosten = 0.25f;
		
		// TODO: eigenes gerät mit sharedpreference ermitteln
    	ausrechnen.putExtra("g2_stromverbrauch", Float.valueOf("1.2"));
    	ausrechnen.putExtra("g2_wasserverbrauch", Integer.valueOf("67"));
    	ausrechnen.putExtra("g2_anschaffungspreis", Float.valueOf("0"));
    	
    	ausrechnen.putExtra("g1_stromverbrauch", (float)(Float.valueOf(stromverbrauch)/100.0f));
    	ausrechnen.putExtra("g1_wasserverbrauch", Integer.valueOf(wasserverbrauch));
    	ausrechnen.putExtra("g1_anschaffungspreis", (float)(Float.valueOf(preis)/100.0f));
    	
    	ausrechnen.putExtra("jahreseinsaetze", jahreseinsaetze);
    	ausrechnen.putExtra("stromkosten", stromkosten);
    	
    	ausrechnen.putExtra("eigenesGeraet", true);
    	
    	startActivity(ausrechnen);
		
		
//        LayoutInflater factory = LayoutInflater.from(this);
//        final View textEntryView = factory.inflate(R.layout.dialog_zaehlerstand_erfassen, null);
//        
//    	Builder builder = new Builder(this);
//    	builder
//            .setIcon(iv_type.getDrawable())
//            .setTitle(getString(R.string.title_zaehlerstanderfassen))
//            .setView(textEntryView)
//            .setPositiveButton(R.string.menu_zaehlerstand_speichern, new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int whichButton) {
//
//
//                			
//                				DbAdapter mRDBA = new DbAdapter(getApplicationContext());
//                	        	
//                	        	Date timeStamp = new Date(System.currentTimeMillis());
//                				
//                				mRDBA.open();
////                				mRDBA.addReading("", timeStamp, zaehlerNummer, zaehlerWert , zaehlerArt, valueChanged, false);
//                				mRDBA.close();
//                				
//                				//TODO better close activity
//
//                				loadData();
//                				
////                				TODO: Sync
////                				Intent serviceIntent = new Intent(getApplicationContext(), SynchronizationService.class);
////                	        	startService(serviceIntent);
////                				Intent intent = new Intent(view.getContext(), ElectricMeterActivity.class);
////                				startActivityForResult(intent, 0);
//                			
//
//
//            }})
//            .show();
	}
	
	private void loadData() {
		Cursor geraeteCursor = db.query(
				DbAdapter.TABLE_WM_NAME,							// Tabellenname
				new String[] { 						// anzuzeigende Spalten
						DbAdapter.KEY_ROWID,
						DbAdapter.KEY_HERSTELLER,
						DbAdapter.KEY_MODELL,
						DbAdapter.KEY_PREIS,
						DbAdapter.KEY_WASSERVERBRAUCH,
						DbAdapter.KEY_STROMVERBRAUCH,
						DbAdapter.KEY_LADEVOLUMEN,
						DbAdapter.KEY_EEK,
						DbAdapter.KEY_STRICHCODE
				}, 
				null, 								// WHERE (z.B. "_id = ?")
				null, 								// WHERE-Argumente (für "?")
				null, 								// GROUP BY
				null, 								// HAVING
				DbAdapter.KEY_ROWID	// ORDER BY
			);

		CursorAdapter geraeteAdapter = new CursorAdapter(this, geraeteCursor, false) {
			
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				LayoutInflater inflater = LayoutInflater.from(context);
				View v = inflater.inflate(R.layout.gv_wm_list_item, parent, false);
				bindView(v, context, cursor);
				return v;
			}
			
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				TextView gereateTitel = (TextView)view.findViewById(R.id.gv_wm_list_geraetetitel);
				TextView ladeVolumen = (TextView)view.findViewById(R.id.gv_wm_list_item_ladevolumen);
				TextView sverbrauch = (TextView)view.findViewById(R.id.gv_wm_list_item_sverbrauch);
				TextView wverbrauch = (TextView)view.findViewById(R.id.gv_wm_list_item_wverbrauch);
				TextView eeKlasse = (TextView)view.findViewById(R.id.gv_wm_list_item_eek);
				TextView preis = (TextView)view.findViewById(R.id.gv_wm_list_item_preis);
				TextView strichcode = (TextView)view.findViewById(R.id.gv_wm_list_item_strichcode); 
				
				ImageView geraeteBild = (ImageView)view.findViewById(R.id.list_geraete_bild);

				String gereateTitelText = 
						cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_HERSTELLER))
						+ " " +
						cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_MODELL));
				
				int ladeVolumenText = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_LADEVOLUMEN));
				int sverbrauchText = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_STROMVERBRAUCH));
				int wverbrauchText = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_WASSERVERBRAUCH));
				String eekText = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_EEK));
				String strichcodeText = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_STRICHCODE));
				int preisText = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_PREIS));
				
				
				double sverbrauchDouble = (double)sverbrauchText/100;
				double preisDouble = (double)preisText/100;
				
				geraeteBild.setImageResource(context.getResources().getIdentifier("ean_" + strichcodeText, "drawable", context.getPackageName()));	
				// 	setIcon(mContext.getResources().getIdentifier(strichcodeText, "drawable", mContext.getPackageName()))				


//				DbAdapter mRDBA = new DbAdapter(getApplicationContext());
//
//				mRDBA.open();
//				long letzterStand = mRDBA.getLastMeterReadingValueForMeterNumber(Integer.parseInt(zaehlerNummerText));
//				long letztesUpdate = mRDBA.getLastMeterReadingDateForMeterNumber(Integer.parseInt(zaehlerNummerText));
//				mRDBA.close();


//				zaehlerTypIcon.setContentDescription(String.valueOf(zaehlerTyp));
				
				gereateTitel.setText(gereateTitelText);
				ladeVolumen.setText(getString(R.string.gv_wm_list_item_ladevolumen).replace("%r", Integer.toString(ladeVolumenText)));
				wverbrauch.setText(getString(R.string.gv_wm_list_item_wverbrauch).replace("%r", Integer.toString(wverbrauchText)));
				sverbrauch.setText(getString(R.string.gv_wm_list_item_sverbrauch).replace("%r", f.format(sverbrauchDouble)));
				eeKlasse.setText(getString(R.string.gv_wm_list_item_eek).replace("%r", eekText));
				preis.setText(getString(R.string.gv_wm_list_item_preis).replace("%r", f.format(preisDouble)));
				strichcode.setText(strichcodeText);
			}
		};
		
		setListAdapter(geraeteAdapter);
	}
}