package de.hska.rbmk;

import de.hska.rbmk.verbrauchsStatistik.*;
import de.hska.rbmk.zaehlerstand.*;
import de.hska.rbmk.verbrauchsrechner.*;
import de.hska.rbmk.geraetevergleich.*;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

public class StartbildschirmActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startbildschirm);
        
	    ActionBar actionBar = getActionBar();
	    actionBar.setTitle(R.string.app_name);
    }
    
    public void zaehlerstandErfassen(View v) {
    	final Intent i = new Intent(this, ZaehlerUebersichtActivity.class);
    	startActivity(i);
    }
    
    public void verbrauchsstatistik(View v) {
    	final Intent i = new Intent(this, VerbrauchsStatistikMainActivity.class);
    	startActivity(i);
    }

    public void verbrauchsrechner(View v) {
    	final Intent i = new Intent(this, VerbrauchsrechnerActivity.class);
    	startActivity(i);
    }

    public void geraetevergleich(View v) {
    	final Intent i = new Intent(this, GeraetevergleichActivtiy.class);
    	startActivity(i);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_einstellungen:
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.startbildschirm_menu, menu);
	    return true;
	}
}