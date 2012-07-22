package de.hska.rbmk.verbrauchsrechner;

import java.text.DecimalFormat;

import de.hska.rbmk.R;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

public class AuswertungWMActivity extends Activity {

	DecimalFormat df = new DecimalFormat("#.##");

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
	auswertung_wm_euroErsparnis,
	auswertung_wm_empfehlungsText;

	Boolean
	eigenesGeraet;

	RatingBar
	ratingBar;

	String
	kwh_ersparnis,
	literErsparnis,
	euroErsparnis,
	jahrEinzahlMehrzahl,
	monatEinzahlMehrzahl,
	text;

	LinearLayout
	auswertungHideLayout;

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
		auswertung_wm_empfehlungsText = (TextView) findViewById(R.id.empfehlungsText);
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
			eigenesGeraet = extras.getBoolean("eigenesGeraet");
		}

		float stromersparnis = g2_stromverbrauch*jahreseinsaetze-g1_stromverbrauch*jahreseinsaetze;
		int wasserersparnis = g2_wasserverbrauch*jahreseinsaetze-g1_wasserverbrauch*jahreseinsaetze;
		float kostenersparnis = g2_stromverbrauch*jahreseinsaetze*stromkosten-g1_stromverbrauch*jahreseinsaetze*stromkosten;

		// Amortisationszeitberechnung (in Jahren)
		float amortisationszeit = ((g1_anschaffungspreis-g2_anschaffungspreis)/((g2_stromverbrauch*jahreseinsaetze*stromkosten)-(g1_stromverbrauch*jahreseinsaetze*stromkosten)));

		int jahre = (int) amortisationszeit;

		float monateDecimal = amortisationszeit - (float) jahre;

		int monate = (int) (monateDecimal * 12);

		kwh_ersparnis = getResources().getString(R.string.auswertung_wm_kwhErsparnis).replace("%kwh", String.valueOf(df.format(stromersparnis)));
		literErsparnis = getResources().getString(R.string.auswertung_wm_literErsparnis).replace("%l", String.valueOf(wasserersparnis));
		euroErsparnis = getResources().getString(R.string.auswertung_wm_euroErsparnis).replace("%euro", String.valueOf(df.format(kostenersparnis)));

		if (jahre == 1)
			jahrEinzahlMehrzahl = "Jahr";
		else
			jahrEinzahlMehrzahl = "Jahre";

		if (monate == 1)
			monatEinzahlMehrzahl = "Monat";
		else
			monatEinzahlMehrzahl = "Monate";

		text = 
				String.valueOf(jahre)
				+ " " + 
				jahrEinzahlMehrzahl
				+ " und " +
				String.valueOf(monate)
				+ " " +
				monatEinzahlMehrzahl
				+ " ";

		if (eigenesGeraet)
			text += getString(R.string.auswertung_wm_text_eigenes);
		else
			text += getString(R.string.auswertung_wm_text_anderes);

		auswertung_wm_kwhErsparnis.setText(kwh_ersparnis);
		auswertung_wm_literErsparnis.setText(literErsparnis);
		auswertung_wm_euroErsparnis.setText(euroErsparnis);
		auswertung_wm_text.setText(text);
		
		if (jahre <= 5) {
			auswertung_wm_empfehlungsText.setText(getResources().getString(R.string.auswertung_text_empf_positiv));
		}
		else if ((jahre > 5) && (jahre <= 10)) {
			auswertung_wm_empfehlungsText.setText(getResources().getString(R.string.auswertung_text_empf_mittel));
		}
		else {
			auswertung_wm_empfehlungsText.setText(getResources().getString(R.string.auswertung_text_empf_negativ));
		}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        {
	            // app icon in action bar clicked; go back
	        	finish();
	            return true;
	        }
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onClickButtonInfo(View v)
	{
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.dialog_kwh_info, null);
        
    	Builder builder = new Builder(this);
    	builder
            .setIcon(android.R.drawable.ic_dialog_info)
            .setTitle(getString(R.string.dialog_kwh_info_titel))
            .setView(textEntryView)
            .setNeutralButton("Schließen", null)
            .show();
	}
	
}
