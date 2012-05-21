package de.hska.rbmk.zaehlerstand;

import de.hska.rbmk.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class zaehlerStandErfassen extends Activity {
	ListView listView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zaehler_stand_erfassen);
    }

}