package de.hska.rbmk.geraeteverwaltung;

import java.text.DecimalFormat;

import de.hska.rbmk.Constants;
import de.hska.rbmk.R;
import de.hska.rbmk.statistik.Liniendiagramm;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;  
import android.os.Vibrator;
import android.support.v4.app.Fragment;  
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class ksVerwaltungFragment extends Fragment {  

	SharedPreferences.Editor editor;
	SharedPreferences prefs;
	
	public static ksVerwaltungFragment newInstance(String title) {

     ksVerwaltungFragment pageFragment = new ksVerwaltungFragment();

     return pageFragment;
 }

	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  

	}  

	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  


		View view = inflater.inflate(R.layout.gverwaltung_ks, container, false);  

		return view;  

	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

}  