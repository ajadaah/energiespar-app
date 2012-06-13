package de.hska.rbmk;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;


public class EinstellungenActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setTitle(R.string.einstellungen_titel);
		getActionBar().setIcon(android.R.drawable.ic_menu_preferences);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new PrefsFragment()).commit();
	}


	public static class PrefsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.einstellungen);
		}
	}

	public static final SharedPreferences getSettings(final ContextWrapper ctx) {
		return ctx.getSharedPreferences(ctx.getPackageName() + "_preferences", MODE_PRIVATE);
	}
	
	@Override
	public void onBackPressed() {
        Intent intent = new Intent(this, StartbildschirmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
		finish();
	}

}