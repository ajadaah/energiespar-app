package de.hska.rbmk;

import android.preference.PreferenceActivity;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Bundle;

public class EinstellungenActivity extends PreferenceActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        
    }
    
    public static final SharedPreferences getSettings(final ContextWrapper ctx) {
    	return ctx.getSharedPreferences(ctx.getPackageName() + "_preferences", MODE_PRIVATE);
    }
   
}