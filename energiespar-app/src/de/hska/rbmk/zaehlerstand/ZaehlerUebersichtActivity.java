package de.hska.rbmk.zaehlerstand;


import de.hska.rbmk.Constants;
import de.hska.rbmk.StartbildschirmActivity;
import de.hska.rbmk.R.menu;
/*
import de.hska.info.electricMeter.meterSelection.MeterNumbersOpenHelper;
import de.hska.info.electricMeter.meterSelection.MeterSelectionActivity;
import de.hska.info.electricMeter.wheel.WheelActivity;
*/

import de.hska.rbmk.R;

import android.R.id;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;


public class ZaehlerUebersichtActivity extends ListActivity {
    

	private SQLiteDatabase db;
	private MeterNumbersOpenHelper mHelper;
    private static final int DIALOG_TEXT_ENTRY = 1;
    
	private static final int CONTEXT_DELETE = 1;

	private long contextSelection = -1;
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zaehler_erfassen_uebersicht);
		
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setTitle(R.string.title_zaehleruebersicht);
		
		registerForContextMenu(getListView());
		
		mHelper = new MeterNumbersOpenHelper(this);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.zaehler_uebersicht_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_zaehler_hinzufuegen:
	        	
	            LayoutInflater factory = LayoutInflater.from(this);
	            final View textEntryView = factory.inflate(R.layout.dialog_zaehler_hinzugfuegen, null);
	            final EditText et = (EditText) textEntryView.findViewById(R.id.eingetippte_zaehlernummer);
	            final Spinner auswahl = (Spinner) textEntryView.findViewById(R.id.zaehlerTypAuswahl);
	            
	        	Builder builder = new Builder(this);
	        	builder
	                .setIcon(android.R.drawable.ic_input_add)
	                .setTitle(getString(R.string.menu_zaehlerHinzufuegen))
	                .setView(textEntryView)
	                .setPositiveButton(R.string.dialog_hinzufuegen, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	        				ContentValues values = new ContentValues();
	        				values.put(MeterNumbersOpenHelper.KEY_NUMBER, et.getText().toString());
	        				values.put(MeterNumbersOpenHelper.KEY_METERTYPE, auswahl.getSelectedItemPosition());	    
	        				db.insert(MeterNumbersOpenHelper.TABLE_METERNUMBERS_NAME, null, values);
	        				loadData();
	                    }
	                })
	                .show();
	            return true;
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, StartbildschirmActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    contextSelection = info.id;
	    
	    menu.setHeaderTitle(getString(R.string.aktionen));

	    //menu.setHeaderTitle(ctx_menu_meternumber);   
	    menu.add(0, CONTEXT_DELETE, 0, getString(R.string.loeschen));
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case CONTEXT_DELETE:
			db.delete(MeterNumbersOpenHelper.TABLE_METERNUMBERS_NAME, MeterNumbersOpenHelper.KEY_ID + "=" + contextSelection, null);
			loadData();
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
//		if(v instanceof TextView) {
//			String s = ((TextView)v).getText().toString();
			TextView tv_number = (TextView) v.findViewById(R.id.list_zaehlernummer);
			ImageView iv_type = (ImageView) v.findViewById(R.id.list_zaehlertyp_icon);
			try {
				int number = Integer.parseInt(tv_number.getText().toString());
				int type = Integer.parseInt(iv_type.getContentDescription().toString());
				
//				Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
//				intent.putExtra(Constants.METERNUMBER, number);
//				startActivity(intent);
				Intent manualInputIntent = new Intent(getApplicationContext(), ZaehlerStandErfassenActivity.class);
				manualInputIntent.putExtra(Constants.METERVALUE, "0");
				manualInputIntent.putExtra(Constants.METERNUMBER, number);
				manualInputIntent.putExtra(Constants.METERTYPE, type);
				startActivityForResult(manualInputIntent, 0);
				
			} catch (NumberFormatException e) {
				// TODO: handle exception
			}
//		}
		super.onListItemClick(l, v, position, id);
	}

	
	@Override
	protected void onPause() {
		db.close();
		super.onPause();
	}

	@Override
	protected void onResume() {
		db = mHelper.getWritableDatabase();
		loadData();

		super.onResume();
	}
	
	private void loadData() {
		Cursor zaehlerCursor = db.query(
				MeterNumbersOpenHelper.TABLE_METERNUMBERS_NAME,							// Tabellenname
				new String[] { 						// anzuzeigende Spalten
						MeterNumbersOpenHelper.KEY_ID,
						MeterNumbersOpenHelper.KEY_NUMBER,
						MeterNumbersOpenHelper.KEY_METERTYPE
				}, 
				null, 								// WHERE (z.B. "_id = ?")
				null, 								// WHERE-Argumente (für "?")
				null, 								// GROUP BY
				null, 								// HAVING
				MeterNumbersOpenHelper.KEY_NUMBER	// ORDER BY
			);

		CursorAdapter zaehlerAdapter = new CursorAdapter(this, zaehlerCursor, false) {
			
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				LayoutInflater inflater = LayoutInflater.from(context);
				View v = inflater.inflate(R.layout.list_item, parent, false);
				bindView(v, context, cursor);
				return v;
			}
			
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				TextView zaehlerNummer = (TextView)view.findViewById(R.id.list_zaehlernummer);
				ImageView zaehlerTypIcon = (ImageView)view.findViewById(R.id.list_zaehlertyp_icon);
				int zaehlerTyp = Integer.parseInt(cursor.getString(cursor.getColumnIndex(MeterNumbersOpenHelper.KEY_METERTYPE)));

				zaehlerTypIcon.setContentDescription(String.valueOf(zaehlerTyp));
				
				switch (zaehlerTyp)
				{
				case 0: { 
					zaehlerTypIcon.setImageResource(R.drawable.ic_type_electricity);
					break; 
					}
				case 1: { 
					zaehlerTypIcon.setImageResource(R.drawable.ic_type_water);
					break; 
					}
				case 2: { 
					zaehlerTypIcon.setImageResource(R.drawable.ic_type_gas);
					break; 
					}
				default: { 
					zaehlerTypIcon.setImageResource(R.drawable.ic_type_electricity);
					break; 
					}
				}

				zaehlerNummer.setText(cursor.getString(cursor.getColumnIndex(MeterNumbersOpenHelper.KEY_NUMBER)));
			}
		};
		/*
		startManagingCursor(zaehlerCursor);
			
		SimpleCursorAdapter adapter =
			new SimpleCursorAdapter(this, 
					R.layout.list_item, 
					zaehlerCursor, 
					new String[] {MeterNumbersOpenHelper.KEY_NUMBER, MeterNumbersOpenHelper.KEY_METERTYPE, MeterNumbersOpenHelper.KEY_ID},
					new int[] {
						R.id.list_zaehlernummer, R.id.temp_zaehlertyp
					}
			);
			*/
		
		setListAdapter(zaehlerAdapter);
	}
}