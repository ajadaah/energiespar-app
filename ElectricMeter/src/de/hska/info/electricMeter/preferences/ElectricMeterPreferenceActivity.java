package de.hska.info.electricMeter.preferences;

import de.hska.info.electricMeter.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ElectricMeterPreferenceActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.em_preference);
	}

}
