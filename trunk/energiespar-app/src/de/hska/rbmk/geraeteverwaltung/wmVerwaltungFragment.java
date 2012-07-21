package de.hska.rbmk.geraeteverwaltung;

import java.text.DecimalFormat;

import de.hska.rbmk.Constants;
import de.hska.rbmk.R;
import de.hska.rbmk.UmweltRating;
import de.hska.rbmk.datenVerwaltung.DbAdapter;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class wmVerwaltungFragment extends Fragment {  

	public DecimalFormat f = new DecimalFormat("#0.00");
	
	public DecimalFormat uw = new DecimalFormat("0.0");

	SharedPreferences.Editor editor;
	SharedPreferences prefs;

	public static wmVerwaltungFragment newInstance(String title) {

		wmVerwaltungFragment pageFragment = new wmVerwaltungFragment();
		//     Bundle bundle = new Bundle();
		//     bundle.putString("title", title);
		//     pageFragment.setArguments(bundle);
		return pageFragment;
	}

	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  

		prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES,getActivity().MODE_PRIVATE);
		editor = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES,getActivity().MODE_PRIVATE).edit();
	}  

	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  


		View view = inflater.inflate(R.layout.gverwaltung_wm, container, false);  
		//     TextView textView = (TextView) view.findViewById(R.id.textView1);  
		//     textView.setText(getArguments().getString("title"));
		return view;  

		//         inflater.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		//         inflater.inflate(R.layout.gverwaltung_wm, container);
		//         return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final ImageButton button = (ImageButton) getActivity().findViewById(R.id.editButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editWaschmaschine();
            }
        });
		
		refreshView();
			

	}

	public void refreshView() {
		int meineWmFuellmenge = prefs.getInt("meineWmFuellmenge",0);
		int meineWmWasserverbrauch = prefs.getInt("meineWmWasserverbrauch",0);
		float meineWmStromverbrauch = prefs.getFloat("meineWmStromverbrauch",0.0f);
		boolean meineWmVorhanden = prefs.getBoolean("meineWmVorhanden",false);
		
		TextView checkText = (TextView)getActivity().findViewById(R.id.checkText);
		LinearLayout hideLL = (LinearLayout)getActivity().findViewById(R.id.hideMe); 
		
		ImageView checkBild = (ImageView)getActivity().findViewById(R.id.checkImage);
		
		TextView meineWmWasser = (TextView)getActivity().findViewById(R.id.meineWmWasser);
		TextView meineWmStrom = (TextView)getActivity().findViewById(R.id.meineWmStrom);
		TextView meineWmFM = (TextView)getActivity().findViewById(R.id.meineWmFuellmenge);
		TextView meineWmUmweltrating = (TextView)getActivity().findViewById(R.id.textViewUmweltrating);
		RatingBar ratingBar = (RatingBar) getActivity().findViewById(R.id.sterneWM);
		
		if (meineWmVorhanden) {
			checkText.setText("Gerät erfasst");
			hideLL.setVisibility(LinearLayout.VISIBLE);
			checkBild.setImageResource(R.drawable.ic_checked);
			meineWmWasser.setText(getResources().getString(R.string.meineWmWasserverbrauch).replace("%r", String.valueOf(meineWmWasserverbrauch)));
			meineWmStrom.setText(getResources().getString(R.string.meineWmStromverbrauch).replace("%r", String.valueOf(f.format(meineWmStromverbrauch))));
			meineWmFM.setText(getResources().getString(R.string.meineWmFuellmenge).replace("%r", String.valueOf(meineWmFuellmenge)));
			
			
			float kwh_pro_kilo = meineWmStromverbrauch/meineWmFuellmenge;
			float umweltRating = (kwh_pro_kilo-(1.0f/10.0f))/(1.0f/35.0f);
			
			String umweltNote;
			if (umweltRating < 2.0f)
				umweltNote = "sehr gut";
			else if (umweltRating < 3.0f)
				umweltNote = "gut";
			else if (umweltRating < 4.0f)
				umweltNote = "befriedigend";
			else if (umweltRating < 5.0f)
				umweltNote = "ausreichend";
			else
				umweltNote = "ungenügend";
			
			if (umweltRating < 1.0f)
				umweltRating = 1.0f;
			
			if (umweltRating > 6.0f)
				umweltRating = 6.0f;
			
			float umweltMeterStand = 7.0f - umweltRating;
			
			String umweltText = umweltNote + " (" + String.valueOf(uw.format(umweltRating)) + ")";
			
			ratingBar.setRating(umweltMeterStand);
			ratingBar.setFocusable(false);
			
			meineWmUmweltrating.setText(umweltText);
//			A 	unter 0,19 kWh/kg
//			B 	0,19 – 0,23 kWh/kg
//			C 	0,24 – 0,27 kWh/kg
//			D 	0,28 – 0,31 kWh/kg
//			E 	0,32 – 0,35 kWh/kg
//			F 	0,36 – 0,39 kWh/kg
//			G	über 0,39 kWh/kg

		}
		else {
			checkText.setText("Gerät nicht erfasst");
			hideLL.setVisibility(LinearLayout.GONE);
			checkBild.setImageResource(R.drawable.ic_unchecked);
		}
	}
	
	public void editWaschmaschine() {
		LayoutInflater factory = LayoutInflater.from(getActivity());
		final View textEntryView = factory.inflate(R.layout.dialog_wm_edit, null);
		final EditText et_wasser = (EditText) textEntryView.findViewById(R.id.eingetippter_wasserverbrauch);
		final EditText et_strom = (EditText) textEntryView.findViewById(R.id.eingetippter_stromverbrauch);
		final EditText et_fuellmenge = (EditText) textEntryView.findViewById(R.id.eingetippte_fuellmenge);

		int meineWmWasserverbrauch = prefs.getInt("meineWmWasserverbrauch",0);
		int meineWmFuellmenge = prefs.getInt("meineWmFuellmenge",0);
		float meineWmStromverbrauch = prefs.getFloat("meineWmStromverbrauch",0.0f);
		boolean meineWmVorhanden = prefs.getBoolean("meineWmVorhanden",false);

		if (meineWmVorhanden) 
		{
			et_wasser.setText(String.valueOf(meineWmWasserverbrauch));
			et_strom.setText(String.valueOf(meineWmStromverbrauch));
			et_fuellmenge.setText(String.valueOf(meineWmFuellmenge));
		}

		Builder builder = new Builder(getActivity());
		builder
		.setIcon(android.R.drawable.ic_menu_edit)
		.setTitle("Werte editieren")
		.setView(textEntryView)
		.setPositiveButton(R.string.dialog_speichern, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				if (!et_wasser.getText().toString().isEmpty() && !et_strom.getText().toString().isEmpty()) { // auf vollständigkeit prüfen
					editor.putInt("meineWmWasserverbrauch", Integer.parseInt(et_wasser.getText().toString()));
					editor.putInt("meineWmFuellmenge", Integer.parseInt(et_fuellmenge.getText().toString()));
					editor.putFloat("meineWmStromverbrauch", Float.parseFloat(et_strom.getText().toString()));
					editor.putBoolean("meineWmVorhanden", true);
					editor.commit();
					refreshView();
				}
				else
				{
					// Vibrate
					Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
					v.vibrate(300);

					// Toast
					Toast.makeText(getActivity(),
							"Fehler: Keine Werte eingegeben",
							Toast.LENGTH_LONG).show();
				}
			}
		})
		.setNegativeButton(R.string.dialog_loeschen, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				editor.putBoolean("meineWmVorhanden", false);
				editor.commit();
				refreshView();
			}
		})
		.show();
	}
	
}  