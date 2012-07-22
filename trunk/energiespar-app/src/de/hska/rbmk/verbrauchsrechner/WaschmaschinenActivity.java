package de.hska.rbmk.verbrauchsrechner;

import de.hska.rbmk.Constants;
import de.hska.rbmk.R;
import de.hska.rbmk.StartbildschirmActivity;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class WaschmaschinenActivity extends Activity {
	CheckBox cbEigenesGeraet;
	LinearLayout hiddenLL;
	
	Spinner 
		spinner_g1_stromverbrauch,
		spinner_g1_wasserverbrauch,
		spinner_g2_stromverbrauch,
		spinner_g2_wasserverbrauch;
	
	EditText 
		edittext_g1_anschaffungspreis,
		edittext_g2_anschaffungspreis,
		edittext_jahreinsaetze,
		edittext_stromkosten;
	
	SharedPreferences.Editor editor;
	SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rechner_waschmaschinen);
		
		String[] geraeteListe = getResources().getStringArray(R.array.GeraeteListe);
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(geraeteListe[1]); // 0 = Kühlschränke, 1 = Waschmaschinen, 2 = Spülmaschinen
	    actionBar.setIcon(R.drawable.ic_calc_washer);
	   
	    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	    
		prefs = getSharedPreferences(Constants.SHARED_PREFERENCES,MODE_PRIVATE);
		editor = getSharedPreferences(Constants.SHARED_PREFERENCES,MODE_PRIVATE).edit();
	    
	    // finde alle Eingabewerte
	    cbEigenesGeraet = (CheckBox) findViewById(R.id.cbEigenesGeraet);
	    hiddenLL = (LinearLayout) findViewById(R.id.calcLayoutToHide);
		spinner_g1_stromverbrauch = (Spinner) findViewById(R.id.rechner_wm_g1_stromverbrauch);
		spinner_g1_wasserverbrauch = (Spinner) findViewById(R.id.rechner_wm_g1_wasserverbrauch);
		spinner_g2_stromverbrauch = (Spinner) findViewById(R.id.rechner_wm_g2_stromverbrauch);
		spinner_g2_wasserverbrauch = (Spinner) findViewById(R.id.rechner_wm_g2_wasserverbrauch);
		edittext_g1_anschaffungspreis = (EditText) findViewById(R.id.rechner_wm_g1_anschaffungspreis);
		edittext_g2_anschaffungspreis = (EditText) findViewById(R.id.rechner_wm_g2_anschaffungspreis);
		edittext_jahreinsaetze = (EditText) findViewById(R.id.rechner_wm_jahreseinsaetze);
		edittext_stromkosten = (EditText) findViewById(R.id.rechner_wm_stromkosten);
	}
	
    public void onCheckBoxClickEigenesGeraet(View v) {
    	if (cbEigenesGeraet.isChecked())
    	{    		
    		boolean meineWmVorhanden = prefs.getBoolean("meineWmVorhanden",false);
    		if (!meineWmVorhanden) {
    			cbEigenesGeraet.setChecked(false);
    			Toast.makeText(this, getString(R.string.auswertung_wm_keingeraet), Toast.LENGTH_SHORT).show();
    		}
    		else {	
    		hiddenLL.setVisibility(LinearLayout.GONE);
    		}
    	}
    	else {
    		hiddenLL.setVisibility(LinearLayout.VISIBLE);
    	}
    }
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		{
			MenuInflater inflater = new MenuInflater(this);
			inflater.inflate(R.menu.rechner_typ_auswahl, menu);
		}

		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	    	{
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, StartbildschirmActivity.class);
//	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	    	}
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
    public void onButtonClick(View v) {
    	
    	float g1_anschaffungspreis = Float.valueOf(edittext_g1_anschaffungspreis.getText().toString());
    	float g2_anschaffungspreis;
    	
    	if (cbEigenesGeraet.isChecked())
    		g2_anschaffungspreis = 0;
    	else
    		g2_anschaffungspreis = Float.valueOf(edittext_g2_anschaffungspreis.getText().toString());
    	
    	if (g1_anschaffungspreis > g2_anschaffungspreis)
    	{
	    	Intent ausrechnen = new Intent(this, AuswertungWMActivity.class);
		
	    	if (cbEigenesGeraet.isChecked()) // // vergleiche mit Spezifikation aus Datensatz
	    	{
	    		int meineWmWasserverbrauch = prefs.getInt("meineWmWasserverbrauch",0);
	    		float meineWmStromverbrauch = prefs.getFloat("meineWmStromverbrauch",0.0f);
	    		
	        	ausrechnen.putExtra("g2_stromverbrauch", meineWmStromverbrauch);
	        	ausrechnen.putExtra("g2_wasserverbrauch", meineWmWasserverbrauch);
	        	ausrechnen.putExtra("g2_anschaffungspreis", Float.valueOf("0"));	
	    	}
	    	else // vergleiche mit Eingabe für Gerät 2
	    	{
	        	ausrechnen.putExtra("g2_stromverbrauch", Float.valueOf(spinner_g2_stromverbrauch.getSelectedItem().toString()));
	        	ausrechnen.putExtra("g2_wasserverbrauch", Integer.valueOf(spinner_g2_wasserverbrauch.getSelectedItem().toString()));
	        	ausrechnen.putExtra("g2_anschaffungspreis", Float.valueOf(edittext_g2_anschaffungspreis.getText().toString()));
	    	}
	
	    	ausrechnen.putExtra("g1_stromverbrauch", Float.valueOf(spinner_g1_stromverbrauch.getSelectedItem().toString()));
	    	ausrechnen.putExtra("g1_wasserverbrauch", Integer.valueOf(spinner_g1_wasserverbrauch.getSelectedItem().toString()));
	    	ausrechnen.putExtra("g1_anschaffungspreis", Float.valueOf(edittext_g1_anschaffungspreis.getText().toString()));
	    	
	    	ausrechnen.putExtra("jahreseinsaetze", Integer.valueOf(edittext_jahreinsaetze.getText().toString()));
	    	ausrechnen.putExtra("stromkosten", Float.valueOf(edittext_stromkosten.getText().toString()));
	    	ausrechnen.putExtra("eigenesGeraet", cbEigenesGeraet.isChecked());
	
	    	startActivity(ausrechnen);
    	}
    	else
    	{
    		Toast.makeText(this, getString(R.string.auswertung_wm_text_unmoeglich), Toast.LENGTH_LONG).show();
    	}
    }
	

}