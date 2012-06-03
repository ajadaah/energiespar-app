package de.hska.rbmk;

import de.hska.rbmk.verbrauchsStatistik.*;
import de.hska.rbmk.zaehlerstand.*;
import de.hska.rbmk.verbrauchsrechner.*;
import de.hska.rbmk.geraetevergleich.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartbildschirmActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startbildschirm);
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
    
}