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

import android.os.Bundle;  
import android.support.v4.app.Fragment;  
import android.support.v4.app.FragmentActivity;  
import android.support.v4.app.FragmentManager;  
import android.support.v4.app.FragmentPagerAdapter;  
import android.support.v4.view.ViewPager;  

public class GeraetevergleichActivity extends FragmentActivity {  

	private static final String[] titel = new String [] { "Waschmaschinen", "Kühlschränke", "Spülmaschinen" };

	private static final int NUMBER_OF_PAGES = 3;

	SharedPreferences.Editor editor;
	SharedPreferences prefs;

	private ViewPager mViewPager;  
	private MyFragmentPagerAdapter mMyFragmentPagerAdapter;  

	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.geraetevergleich_main);  

		prefs = getSharedPreferences(Constants.SHARED_PREFERENCES,MODE_PRIVATE);
		editor = getSharedPreferences(Constants.SHARED_PREFERENCES,MODE_PRIVATE).edit();

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
}