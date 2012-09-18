package de.hska.rbmk.geraetevergleich;

import java.text.DecimalFormat;

import de.hska.rbmk.Constants;
import de.hska.rbmk.R;
import de.hska.rbmk.datenVerwaltung.DbAdapter;
import de.hska.rbmk.verbrauchsrechner.AuswertungWMActivity;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;  
import android.support.v4.app.Fragment;  
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;  
import android.view.MenuItem;
import android.view.View;  
import android.view.ViewGroup;  
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;  
import android.widget.Toast;

public class wmFragment extends ListFragment {  


	private SQLiteDatabase db;
	private DbAdapter dbAdapter;
	View view;
	public DecimalFormat f = new DecimalFormat("#0.00");
	
	private static final int CONTEXT_OPEN_BROWSER = 0;

	private long contextSelection = -1;
	
	SharedPreferences.Editor editor;
	SharedPreferences prefs;
	
	public static wmFragment newInstance(String title) {

     wmFragment pageFragment = new wmFragment();
//     Bundle bundle = new Bundle();
//     bundle.putString("title", title);
//     pageFragment.setArguments(bundle);
     return pageFragment;
 }

 @Override  
 public void onCreate(Bundle savedInstanceState) {  
     super.onCreate(savedInstanceState);  
     
	    prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES,getActivity().MODE_PRIVATE);
	    editor = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES,getActivity().MODE_PRIVATE).edit();
		
		dbAdapter = new DbAdapter(getActivity());
 }  

 @Override  
 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  

         inflater.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
         inflater.inflate(R.layout.geraetevergleich_wm, container);
         return super.onCreateView(inflater, container, savedInstanceState);
 }
 
 @Override
 public void onActivityCreated(Bundle savedInstanceState) {
     super.onActivityCreated(savedInstanceState);
     
		registerForContextMenu(getListView());	
     
		int filterIndex = prefs.getInt("gv_sortierungs_filter",0);
		
		String[] filterText = this.getResources().getStringArray(R.array.SortierVerfahrenAuswahl);
		
		
		dbAdapter.open();
		db = dbAdapter.getDb();
		
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
				null, 								// WHERE-Argumente (für "?")
				null, 								// GROUP BY
				null, 								// HAVING
				filterText[filterIndex]				// ORDER BY
			);

		CursorAdapter geraeteAdapter = new CursorAdapter(getActivity().getBaseContext(), geraeteCursor, false) {
			
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
		
		dbAdapter.close();
 }
     
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case CONTEXT_OPEN_BROWSER:
			
			String auswahl = String.valueOf(contextSelection);
			
			dbAdapter.open();
			db = dbAdapter.getDb();
			
	    	String query = "SELECT "+DbAdapter.KEY_HERSTELLER+","+DbAdapter.KEY_MODELL+" FROM "+DbAdapter.TABLE_WM_NAME+" WHERE _id = ?";
	    	Cursor queryCursor = db.rawQuery(query, new String[] { auswahl });
	    	queryCursor.moveToFirst();
	    	
			String suchString = 
					queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_HERSTELLER))
					+ "+" +
					queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_MODELL));
			
	    	queryCursor.close();
	    	dbAdapter.close();
	    	
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse("http://www.google.com/search?as_q=" + suchString));
			startActivity(i);
			
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		TextView tv_strichcode = (TextView) v.findViewById(R.id.gv_wm_list_item_strichcode);
		geraetAuswerten(tv_strichcode.getText().toString());

		super.onListItemClick(l, v, position, id);
	}
	
	private void geraetAuswerten(final String strichcode)
	{
		boolean meineWmVorhanden = prefs.getBoolean("meineWmVorhanden",false);
		
		if (meineWmVorhanden) 
		{
			dbAdapter.open();
			db = dbAdapter.getDb();
	    	String query = "SELECT "+DbAdapter.KEY_STROMVERBRAUCH+","+DbAdapter.KEY_WASSERVERBRAUCH+","+DbAdapter.KEY_PREIS+" FROM "+DbAdapter.TABLE_WM_NAME+" WHERE "+DbAdapter.KEY_STRICHCODE+" = ?";
	    	Cursor queryCursor = db.rawQuery(query, new String[] { strichcode });
	    	queryCursor.moveToFirst();
	    	
			String preis = queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_PREIS));
			String stromverbrauch = queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_STROMVERBRAUCH));
			String wasserverbrauch = queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_WASSERVERBRAUCH));
			
	    	queryCursor.close();
	    	dbAdapter.close();
			
			Intent ausrechnen = new Intent(getActivity(), AuswertungWMActivity.class);
			
			int jahreseinsaetze = 244;
			float stromkosten = 0.25f;
			
			int meineWmWasserverbrauch = prefs.getInt("meineWmWasserverbrauch",0);
			float meineWmStromverbrauch = prefs.getFloat("meineWmStromverbrauch",0.0f);
			
	    	ausrechnen.putExtra("g2_stromverbrauch", meineWmStromverbrauch);
	    	ausrechnen.putExtra("g2_wasserverbrauch", meineWmWasserverbrauch);
	    	ausrechnen.putExtra("g2_anschaffungspreis", Float.valueOf("0"));
	    	
	    	ausrechnen.putExtra("g1_stromverbrauch", (float)(Float.valueOf(stromverbrauch)/100.0f));
	    	ausrechnen.putExtra("g1_wasserverbrauch", Integer.valueOf(wasserverbrauch));
	    	ausrechnen.putExtra("g1_anschaffungspreis", (float)(Float.valueOf(preis)/100.0f));
	    	
	    	ausrechnen.putExtra("jahreseinsaetze", jahreseinsaetze);
	    	ausrechnen.putExtra("stromkosten", stromkosten);
	    	
	    	ausrechnen.putExtra("eigenesGeraet", true);
	    	
	    	startActivity(ausrechnen);
		}
		else
		{
			Toast.makeText(getActivity(), "Ein eigenes Gerät muss zuerst angelegt werden.", Toast.LENGTH_LONG).show();
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
}  