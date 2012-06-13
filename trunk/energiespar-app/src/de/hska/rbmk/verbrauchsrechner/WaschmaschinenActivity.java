package de.hska.rbmk.verbrauchsrechner;

import de.hska.rbmk.R;
import de.hska.rbmk.StartbildschirmActivity;
import de.hska.rbmk.ThemedActivity;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WaschmaschinenActivity extends Activity {
	CheckBox cbEigenesGeraet;
	LinearLayout hiddenLL;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rechner_waschmaschinen);
		
		String[] geraeteListe = getResources().getStringArray(R.array.GeraeteListe);
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(geraeteListe[1]); // 0 = Kühlschränke, 1 = Waschmaschinen, 2 = Spülmaschinen
	    actionBar.setIcon(R.drawable.ic_calc_washer);
	    
	    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	    
	    cbEigenesGeraet = (CheckBox) findViewById(R.id.cbEigenesGeraet);
	    
	    hiddenLL = (LinearLayout) findViewById(R.id.calcLayoutToHide);
	}
	
    public void onCheckBoxClickEigenesGeraet(View v) {
    	if (cbEigenesGeraet.isChecked())
    	{
    		hiddenLL.setVisibility(LinearLayout.GONE);
    	}
    	else {
    		hiddenLL.setVisibility(LinearLayout.VISIBLE);
    	}
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
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	    	{
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, StartbildschirmActivity.class);
//	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	    	}
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}