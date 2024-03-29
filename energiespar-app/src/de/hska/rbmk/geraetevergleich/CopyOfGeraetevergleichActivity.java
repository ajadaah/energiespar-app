package de.hska.rbmk.geraetevergleich;


import java.text.DecimalFormat;
import de.hska.rbmk.datenVerwaltung.*;
import de.hska.rbmk.verbrauchsrechner.AuswertungWMActivity;
import de.hska.rbmk.Constants;
import de.hska.rbmk.StartbildschirmActivity;
import de.hska.rbmk.R;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;


public class CopyOfGeraetevergleichActivity extends ListActivity {
    

	private SQLiteDatabase db;
//	private MeterNumbersOpenHelper mHelper;
	private DbAdapter dbAdapter;
    
	private static final int CONTEXT_OPEN_BROWSER = 1;

	private long contextSelection = -1;
	
	public DecimalFormat f = new DecimalFormat("#0.00");
	
	SharedPreferences.Editor editor;
	SharedPreferences prefs;
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.geraetevergleich_wm);
		
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(R.string.title_geraetevergleich);
	    
		registerForContextMenu(getListView());
		
		dbAdapter = new DbAdapter(this);
		
	    prefs = getSharedPreferences(Constants.SHARED_PREFERENCES,MODE_PRIVATE);
	    editor = getSharedPreferences(Constants.SHARED_PREFERENCES,MODE_PRIVATE).edit();
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
	    		auswahl.setSelection(prefs.getInt("gv_sortierungs_filter",0));
	    		
	        	Builder builder = new Builder(this);
	        	builder
	                .setIcon(R.drawable.ic_sort)
	                .setTitle(getString(R.string.menu_filter))
	                .setView(textEntryView)
	                .setPositiveButton(R.string.dialog_hinzufuegen, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                		int selectedPosition = auswahl.getSelectedItemPosition();
	                		editor.putInt("gv_sortierungs_filter", selectedPosition);
	                		editor.commit();
	        				loadData();
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
		
		int jahreseinsaetze = 244;
		float stromkosten = 0.25f;
		
		// TODO: eigenes ger�t mit sharedpreference ermitteln
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
	}
	
	private void loadData() {
		int filterIndex = prefs.getInt("gv_sortierungs_filter",0);
		
		String[] filterText = this.getResources().getStringArray(R.array.SortierVerfahrenAuswahl);
		
		Cursor geraeteCursor = db.query(
				DbAdapter.TABLE_WM_NAME,			// Tabellenname
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
				null, 								// WHERE-Argumente (f�r "?")
				null, 								// GROUP BY
				null, 								// HAVING
				filterText[filterIndex]				// ORDER BY
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