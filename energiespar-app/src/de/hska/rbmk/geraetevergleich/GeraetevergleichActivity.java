package de.hska.rbmk.geraetevergleich;


import java.text.DecimalFormat;

import de.hska.rbmk.Constants;
import de.hska.rbmk.StartbildschirmActivity;
import de.hska.rbmk.R;
import de.hska.rbmk.datenVerwaltung.DbAdapter;
import de.hska.rbmk.scanner.*;
import de.hska.rbmk.verbrauchsrechner.*;
import de.hska.rbmk.zaehlerstand.ZaehlerstandErfassenActivity;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;  
import android.support.v4.app.FragmentActivity;  
import android.support.v4.app.FragmentManager;  
import android.support.v4.app.FragmentPagerAdapter;  
import android.support.v4.view.ViewPager;  
import java.util.List;

public class GeraetevergleichActivity extends FragmentActivity {  

	private SQLiteDatabase db;
	private DbAdapter dbAdapter;
	View view;
	public DecimalFormat f = new DecimalFormat("#0.00");

	SharedPreferences.Editor editor;
	SharedPreferences prefs;

	private static final String[] titel = new String [] { "Waschmaschinen", "Kühlschränke", "Spülmaschinen" };
	private static final int NUMBER_OF_PAGES = 3;
	private ViewPager mViewPager;  
	private MyFragmentPagerAdapter mMyFragmentPagerAdapter;  

	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.geraetevergleich_main);  

		prefs = getSharedPreferences(Constants.SHARED_PREFERENCES,MODE_PRIVATE);
		editor = getSharedPreferences(Constants.SHARED_PREFERENCES,MODE_PRIVATE).edit();
		dbAdapter = new DbAdapter(this);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.title_geraetevergleich);

		mViewPager = (ViewPager) findViewById(R.id.viewpager);  
		mMyFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());  
		mViewPager.setAdapter(mMyFragmentPagerAdapter);  
	}  

	private static class MyFragmentPagerAdapter extends FragmentPagerAdapter {  

		public MyFragmentPagerAdapter(FragmentManager fm) {  
			super(fm);  
		}  

		@Override  
		public Fragment getItem(int index) {  
			switch (index) {
			case 0: return new wmFragment().newInstance("titel");
			case 1: return new ksFragment().newInstance("titel");
			case 2: return new smFragment().newInstance("titel");
			//and so on....
			}
			return null;
		}  

		@Override  
		public int getCount() {  

			return NUMBER_OF_PAGES;  
		}  

		@Override
		public CharSequence getPageTitle(int position) {
			return titel[position];
		}
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
			// Strichcode einlesen
			IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
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
					
					Intent intent = getIntent();
					finish();
					startActivity(intent);
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
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {  
		  switch (requestCode) {
		  case IntentIntegrator.REQUEST_CODE:
		     if (resultCode == this.RESULT_OK) {

		        IntentResult intentResult = 
		           IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

		        if (intentResult != null) {

		           String contents = intentResult.getContents();
		           String format = intentResult.getFormatName();

		           geraetAuswerten(contents);
		        } else {
		        	// Leerer Intent
		        }
		     } else if (resultCode == this.RESULT_CANCELED) {
		    	 // Benutzerabbruch
		     }
		  }
		}
	
	private void geraetAuswerten(String strichcode)
	{
		boolean meineWmVorhanden = prefs.getBoolean("meineWmVorhanden",false);

		if (meineWmVorhanden) 
		{
			dbAdapter.open();
			db = dbAdapter.getDb();
	    	String query = "SELECT "+DbAdapter.KEY_STROMVERBRAUCH+","+DbAdapter.KEY_WASSERVERBRAUCH+","+DbAdapter.KEY_PREIS+" FROM "+DbAdapter.TABLE_WM_NAME+" WHERE "+DbAdapter.KEY_STRICHCODE+" = ?";
	    	Cursor queryCursor = db.rawQuery(query, new String[] { strichcode });
	    	queryCursor.moveToFirst();
	
			if (queryCursor.getCount() > 0)
			{
				String preisText = queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_PREIS));
				String stromverbrauchText = queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_STROMVERBRAUCH));
				String wasserverbrauchText = queryCursor.getString(queryCursor.getColumnIndex(DbAdapter.KEY_WASSERVERBRAUCH));



				queryCursor.close();
				dbAdapter.close();



				int jahreseinsaetze = 244;
				float stromkosten = 0.25f;

				int meineWmWasserverbrauch = prefs.getInt("meineWmWasserverbrauch",0);
				float meineWmStromverbrauch = prefs.getFloat("meineWmStromverbrauch",0.0f);

				Intent ausrechnen = new Intent(this, AuswertungWMActivity.class);

				ausrechnen.putExtra("g2_stromverbrauch", meineWmStromverbrauch);
				ausrechnen.putExtra("g2_wasserverbrauch", meineWmWasserverbrauch);
				ausrechnen.putExtra("g2_anschaffungspreis", Float.valueOf("0"));

				ausrechnen.putExtra("g1_stromverbrauch", (float)(Float.valueOf(stromverbrauchText)/100.0f));
				ausrechnen.putExtra("g1_wasserverbrauch", Integer.valueOf(wasserverbrauchText));
				ausrechnen.putExtra("g1_anschaffungspreis", (float)(Float.valueOf(preisText)/100.0f));

				ausrechnen.putExtra("jahreseinsaetze", jahreseinsaetze);
				ausrechnen.putExtra("stromkosten", stromkosten);

				ausrechnen.putExtra("eigenesGeraet", true);

				startActivity(ausrechnen);
			}
			else {
				queryCursor.close();
				dbAdapter.close();

				Toast.makeText(this, "Gerät wurde nicht in der Datenbank gefunden.", Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			Toast.makeText(this, "Ein eigenes Gerät muss zuerst angelegt werden.", Toast.LENGTH_LONG).show();
		}
	}



}