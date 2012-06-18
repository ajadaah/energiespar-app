package de.hska.rbmk;

import de.hska.rbmk.verbrauchsStatistik.*;
import de.hska.rbmk.zaehlerstand.*;
import de.hska.rbmk.verbrauchsrechner.*;
import de.hska.rbmk.geraetevergleich.*;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class StartbildschirmActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
              
        int display_mode = getResources().getConfiguration().orientation;

        if (display_mode == 1) {
            setContentView(R.layout.startbildschirm);
        } else {
            setContentView(R.layout.startbildschirm_landscape);
        }  
        
	    ActionBar actionBar = getActionBar();
	    actionBar.setTitle(R.string.app_name);
    }
    
    public void zaehlerstandErfassen(View v) {
    	final Intent i = new Intent(this, ZaehlerstandErfassenActivity.class);
    	startActivity(i);
    }
    
    public void verbrauchsstatistik(View v) {
    	final Intent i = new Intent(this, VerbrauchsStatistikMainActivity.class);
    	startActivity(i);
    }

    public void verbrauchsrechner(View v) {
    	final Intent i = new Intent(this, WaschmaschinenActivity.class);
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
	        	startActivity(new Intent(this, EinstellungenActivity.class));
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