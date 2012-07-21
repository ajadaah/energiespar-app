package de.hska.rbmk.geraeteverwaltung;

import de.hska.rbmk.R;
import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.view.ViewPager;
import de.hska.rbmk.Constants;
import de.hska.rbmk.StartbildschirmActivity;
import de.hska.rbmk.datenVerwaltung.DbAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v4.app.Fragment;  
import android.support.v4.app.FragmentActivity;  
import android.support.v4.app.FragmentManager;  
import android.support.v4.app.FragmentPagerAdapter;  

public class GeraeteverwaltungActivity extends FragmentActivity {  

	private static final String[] titel = new String [] { "Waschmaschine", "Kühlschrank", "Spülmaschine" };

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
		actionBar.setTitle(R.string.title_geraeteverwaltung);

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
			case 0: return new wmVerwaltungFragment().newInstance("titel");
			case 1: return new ksVerwaltungFragment().newInstance("titel");
			case 2: return new smVerwaltungFragment().newInstance("titel");
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

//	public void editButtonClick(View v) {
//		switch (mViewPager.getCurrentItem()) {
//		case 0: { editWaschmaschine(); break; }
//		case 1: break;
//		case 2: break;
//		}
//	}

	

	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		MenuInflater inflater = getMenuInflater();
	//		inflater.inflate(R.menu.geraetevergleich_menu, menu);
	//		return true;
	//	}

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