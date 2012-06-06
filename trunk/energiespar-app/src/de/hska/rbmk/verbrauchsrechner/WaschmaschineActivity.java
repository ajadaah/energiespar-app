package de.hska.rbmk.verbrauchsrechner;

import de.hska.rbmk.R;
import de.hska.rbmk.ThemedActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class WaschmaschineActivity extends ThemedActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rechner_waschmaschine);
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
}