package de.hska.rbmk.verbrauchsrechner;

import de.hska.rbmk.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

public class AuswertungWMActivity extends Activity {
	float 
		g1_stromverbrauch,
		g1_anschaffungspreis,
		g2_stromverbrauch,
		g2_anschaffungspreis,
		stromkosten;
	
	int 
		jahreseinsaetze,
		g2_wasserverbrauch,
		g1_wasserverbrauch;
	
	TextView 
		auswertung_wm_text,
		auswertung_wm_literErsparnis,
		auswertung_wm_kwhErsparnis,
		auswertung_wm_euroErsparnis;
	
	RatingBar
		ratingBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rechner_auswertung_wm);
		
		String[] geraeteListe = getResources().getStringArray(R.array.GeraeteListe);
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(geraeteListe[1]); // 0 = Kühlschränke, 1 = Waschmaschinen, 2 = Spülmaschinen
	    actionBar.setIcon(R.drawable.ic_calc_washer);
	    
	    auswertung_wm_text = (TextView) findViewById(R.id.auswertung_wm_text);
	    auswertung_wm_literErsparnis = (TextView) findViewById(R.id.auswertung_wm_literErsparnis);
		auswertung_wm_kwhErsparnis = (TextView) findViewById(R.id.auswertung_wm_kwhErsparnis);
		auswertung_wm_euroErsparnis = (TextView) findViewById(R.id.auswertung_wm_euroErsparnis);
		ratingBar = (RatingBar) findViewById(R.id.sterneWM);
	    
	    Intent auswertung = this.getIntent();
	    Bundle extras = auswertung.getExtras();
	    
    	if (extras != null) {
    		g1_stromverbrauch = extras.getFloat("g1_stromverbrauch");
    		g1_wasserverbrauch = extras.getInt("g1_wasserverbrauch");
    		g1_anschaffungspreis = extras.getFloat("g1_anschaffungspreis");
    		g2_stromverbrauch = extras.getFloat("g2_stromverbrauch");
    		g2_wasserverbrauch = extras.getInt("g2_wasserverbrauch");
    		g2_anschaffungspreis = extras.getFloat("g2_anschaffungspreis");
    		jahreseinsaetze = extras.getInt("jahreseinsaetze");
    		stromkosten = extras.getFloat("stromkosten");
    	}
    	
    	float stromersparnis = g2_stromverbrauch*jahreseinsaetze-g1_stromverbrauch*jahreseinsaetze;
    	int wasserersparnis = g2_wasserverbrauch*jahreseinsaetze-g1_wasserverbrauch*jahreseinsaetze;
    	float kostenersparnis = g2_stromverbrauch*jahreseinsaetze*stromkosten-g1_stromverbrauch*jahreseinsaetze*stromkosten;
    	
    	String kwh_ersparnis = getResources().getString(R.string.auswertung_wm_kwhErsparnis).replace("%kwh", String.valueOf(stromersparnis));
    	String literErsparnis = getResources().getString(R.string.auswertung_wm_literErsparnis).replace("%l", String.valueOf(wasserersparnis));
    	String euroErsparnis = getResources().getString(R.string.auswertung_wm_euroErsparnis).replace("%euro", String.valueOf(kostenersparnis));
    	
    	auswertung_wm_kwhErsparnis.setText(kwh_ersparnis);
    	auswertung_wm_literErsparnis.setText(literErsparnis);
    	auswertung_wm_euroErsparnis.setText(euroErsparnis);
    	
    	// Amortisationszeitberechnung (in Jahren)
    	float amortisationszeit = (g1_anschaffungspreis/((g2_stromverbrauch*jahreseinsaetze*stromkosten)-(g1_stromverbrauch*jahreseinsaetze*stromkosten)));
    	
    	ratingBar.setRating(2.33F);
    	
    	
	}
	
}
