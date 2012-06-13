package de.hska.rbmk.verbrauchsrechner;

import de.hska.rbmk.R;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

public class AuswertungWMActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rechner_auswertung_wm);
		
		String[] geraeteListe = getResources().getStringArray(R.array.GeraeteListe);
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(geraeteListe[1]); // 0 = Kühlschränke, 1 = Waschmaschinen, 2 = Spülmaschinen
	    actionBar.setIcon(R.drawable.ic_calc_washer);
	}
}
