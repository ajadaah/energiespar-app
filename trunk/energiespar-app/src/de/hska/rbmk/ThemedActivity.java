package de.hska.rbmk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

public class ThemedActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        SharedPreferences einstellungen = EinstellungenActivity.getSettings(this);
        String themeName = einstellungen.getString("ausgewaehltes_theme", getResources().getString(R.string.einstellungen_default_theme));
        int themeResource = getResources().getIdentifier(themeName, "style", getPackageName());
        this.setTheme(themeResource);
	}
}
