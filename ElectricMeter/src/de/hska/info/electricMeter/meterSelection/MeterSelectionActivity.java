package de.hska.info.electricMeter.meterSelection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import de.hska.info.electricMeter.Constants;
import de.hska.info.electricMeter.R;
import de.hska.info.electricMeter.camera.CameraActivity;

public class MeterSelectionActivity extends ListActivity {
    

	private SQLiteDatabase db;
	private MeterNumbersOpenHelper mHelper;
    private static final int DIALOG_TEXT_ENTRY = 1;
    
	private static final int CONTEXT_DELETE = 1;

	private long contextSelection = -1;
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meter_selection);
		
		registerForContextMenu(getListView());
		
		mHelper = new MeterNumbersOpenHelper(this);
		
		Button addButton = (Button) findViewById(R.id.add_meter_button);
		
		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
               showDialog(DIALOG_TEXT_ENTRY);
			}
		});
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    contextSelection = info.id;
	    
	    menu.setHeaderTitle(getString(R.string.actions));

		//menu.setHeaderTitle(ctx_menu_meternumber);   
	    menu.add(0, CONTEXT_DELETE, 0, getString(R.string.delete));
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
		if(v instanceof TextView) {
			String s = ((TextView)v).getText().toString();
			try {
				int number = Integer.parseInt(s);
				Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
				intent.putExtra(Constants.METERNUMBER, number);
				startActivity(intent);
			} catch (NumberFormatException e) {
				// TODO: handle exception
			}
		}
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
		Cursor cursor = db.query(
				MeterNumbersOpenHelper.TABLE_METERNUMBERS_NAME,							// Tabellenname
				new String[] { 						// anzuzeigende Spalten
						MeterNumbersOpenHelper.KEY_ID,
						MeterNumbersOpenHelper.KEY_NUMBER
				}, 
				null, 								// WHERE (z.B. "_id = ?")
				null, 								// WHERE-Argumente (f�r "?")
				null, 								// GROUP BY
				null, 								// HAVING
				MeterNumbersOpenHelper.KEY_NUMBER	// ORDER BY
			);
		startManagingCursor(cursor);
		
		SimpleCursorAdapter adapter =
			new SimpleCursorAdapter(this, 
					android.R.layout.simple_list_item_single_choice, 
					cursor, 
					new String[] {MeterNumbersOpenHelper.KEY_NUMBER, MeterNumbersOpenHelper.KEY_ID},
					new int[] {
						android.R.id.text1
					}
			);
		setListAdapter(adapter);
	}
	
	   @Override
	    protected Dialog onCreateDialog(int id) {
	        switch (id) {
	        case DIALOG_TEXT_ENTRY:
	            // This example shows how to add a custom layout to an AlertDialog
	            LayoutInflater factory = LayoutInflater.from(this);
	            final View textEntryView = factory.inflate(R.layout.alert_dialog_meternumber_entry, null);
	            final EditText et = (EditText) textEntryView.findViewById(R.id.ed_meternumber);
	            
	            return new AlertDialog.Builder(MeterSelectionActivity.this)
	                .setIcon(android.R.drawable.ic_dialog_alert)
	                .setTitle(getString(R.string.enter_meternumber))
	                .setView(textEntryView)
	                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	        				ContentValues values = new ContentValues();
	        				values.put(MeterNumbersOpenHelper.KEY_NUMBER, et.getText().toString());
	        				db.insert(MeterNumbersOpenHelper.TABLE_METERNUMBERS_NAME, null, values);
	        				loadData();
	                       
	                    }
	                })
	                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* Do nothing */
	                    }
	                })
	                .create();
	        }
	        return null;
	    }
}