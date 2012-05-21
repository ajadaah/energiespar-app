package de.hska.rbmk.zaehlerstand;

import de.hska.rbmk.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class zaehlerUebersicht extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zaehler_erfassen_uebersicht);
    }
    
    public void zaehlerAuswahl(View v) {
    	final Intent i = new Intent(this, zaehlerStandErfassen.class);
    	startActivity(i);
    }
}