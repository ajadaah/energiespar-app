package de.hska.rbmk.geraeteverwaltung;

import java.text.DecimalFormat;

import de.hska.rbmk.Constants;
import de.hska.rbmk.R;
import de.hska.rbmk.statistik.Liniendiagramm;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;  
import android.support.v4.app.Fragment;  
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.Toast;

public class smVerwaltungFragment extends Fragment {  

	public DecimalFormat f = new DecimalFormat("#0.00");
	
	SharedPreferences.Editor editor;
	SharedPreferences prefs;
	
	public static smVerwaltungFragment newInstance(String title) {

     smVerwaltungFragment pageFragment = new smVerwaltungFragment();
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

	 
     View view = inflater.inflate(R.layout.gverwaltung_sm, container, false);  
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
     
     
		
	}

}  