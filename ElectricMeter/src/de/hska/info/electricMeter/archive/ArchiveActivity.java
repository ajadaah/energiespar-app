package de.hska.info.electricMeter.archive;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import android.app.ExpandableListActivity;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import de.hska.info.electricMeter.R;
import de.hska.info.electricMeter.dataStorage.MeterReadingEntry;
import de.hska.info.electricMeter.dataStorage.MeterReadingsDbAdapter;
import de.hska.info.electricMeter.dataStorage.MeterType;

public class ArchiveActivity extends ExpandableListActivity {

    private static final String logContext = "ArchiveActivity";
    
	ExpandableListAdapter mAdapter;
    Resources res;
	private MeterReadingsDbAdapter mDbHelper;

	private Cursor mReadingsCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archive);
        res = getResources();
        
        mDbHelper = new MeterReadingsDbAdapter(this);
		Log.d(logContext, "database helper fetched.");
		mDbHelper.open();
		Log.d(logContext, "database helper opened access to the database.");
		mReadingsCursor = mDbHelper.getAllMeterReadings();
		Log.d(logContext, "loaded " + mReadingsCursor.getCount() + " unsynchronized meter reading entries from the database, currently at position " + mReadingsCursor.getPosition() + ".");
		mReadingsCursor.moveToFirst();
		List<MeterReadingEntry> meterReadingEntryList = new ArrayList<MeterReadingEntry>();
		Set<Integer> yearSet = new HashSet<Integer>();
		boolean moreAvailable = mReadingsCursor.getCount() > 0;
		while (moreAvailable) {
			int rowID = -1;// not needed here, was: mReadingsCursor.getInt(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_ROWID));
			String connectionInfo = "";// not needed here, was: mReadingsCursor.getString(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_CONNECTIONINFO));
		    Date timestamp = new Date(new Long(mReadingsCursor.getString(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_TIMESTAMP))));
		    int meterNumber = mReadingsCursor.getInt(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERNUMBER));
		    int meterValue = mReadingsCursor.getInt(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERVALUE));;
		    int meterType = -1;// not needed here, was: mReadingsCursor.getInt(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERTYPE));;
		    String isMeterValueRevised = "";// not needed here, was: mReadingsCursor.getString(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERVALUEREVISED)); 
		    String isSynchronized = "";// not needed here, was: mReadingsCursor.getString(mReadingsCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_SYNCHRONIZED));
		    meterReadingEntryList.add(new MeterReadingEntry(rowID, connectionInfo, timestamp, meterNumber, meterValue, MeterType.get(meterType), Boolean.getBoolean(isMeterValueRevised), Boolean.getBoolean(isSynchronized)));
		    yearSet.add(timestamp.getYear() + 1900);
		    moreAvailable = !mReadingsCursor.isLast();
			mReadingsCursor.moveToNext();
		}
		mReadingsCursor.close();
		mDbHelper.close();
        
        // Set up our adapter
        mAdapter = new SimpleExpandableListAdapter(
				this,
				createGroupList(yearSet), 				// Creating group List.
				R.layout.arch_group_row,				// Group item layout XML.			
				new String[] { res.getString(R.string.group_label_key) },	// the key of group item.
				new int[] { R.id.row_name },	// ID of each group item.-Data under the key goes into this TextView.					
				createChildList(yearSet, meterReadingEntryList),				// childData describes second-level entries.
				R.layout.arch_child_row,				// Layout for sub-level entries(second level).
				new String[] {res.getString(R.string.child_label_key), res.getString(R.string.child_number_key), res.getString(R.string.child_value_key)},		// Keys in childData maps to display.
				new int[] { R.id.label, R.id.grp_childNumber, R.id.grp_child}		// Data under the keys above go into these TextViews.
        );
        setListAdapter(mAdapter);

        ExpandableListView expListView = getExpandableListView();
        if (mAdapter.getGroupCount() > 0) {
        	expListView.expandGroup(0);
        }
        if(mAdapter.getGroupCount() > 2) {
        	expListView.expandGroup(1);
        }
    }
    
	/* creating list for group */
	private List<HashMap<String, String>> createGroupList(Set<Integer> yearSet) {
	  	  ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
	  	  /*for( int i = 0 ; i < 4 ; ++i ) { 
	  		HashMap<String, String> m = new HashMap<String, String>();
	  	    int year = 2011;
	  		m.put( res.getString(R.string.group_label_key), String.valueOf(year - i) );
	  		result.add( m );
	  	  }*/
	  	for (Integer yearValue : yearSet) {
	  		HashMap<String, String> m = new HashMap<String, String>();
	  		m.put( res.getString(R.string.group_label_key), String.valueOf(yearValue.intValue()));
	  		result.add( m );
		}
	  	  return result;
    }
    
	/* creating list for children */
	private List<ArrayList<HashMap<String, String>>> createChildList(Set<Integer> yearSet, List<MeterReadingEntry> meterReadingEntryList) {
		ArrayList<ArrayList<HashMap<String, String>>> result = new ArrayList<ArrayList<HashMap<String, String>>>();
		java.text.DateFormat dateFormatter = java.text.DateFormat.getDateInstance();
		/*for (int i = 0; i < 4; ++i) {
			ArrayList<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();
			
			//START child 1
			HashMap<String, String> child = new HashMap<String, String>();

			child.put(res.getString(R.string.child_label_key),
					res.getString(R.string.child_label_metervalue));

			int meterValue = 123456;
			child.put(res.getString(R.string.child_value_key),
					String.valueOf(meterValue));

			secList.add(child);
			//END child 1
			
			result.add(secList);
		}*/
		for (Integer yearValue : yearSet) {
			ArrayList<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> localHeader = new HashMap<String, String>();

			localHeader.put(res.getString(R.string.child_label_key), res.getString(R.string.child_label_date));
			localHeader.put(res.getString(R.string.child_number_key), res.getString(R.string.child_label_meternumber));
			localHeader.put(res.getString(R.string.child_value_key), res.getString(R.string.child_label_metervalue));
			secList.add(localHeader);
			for (MeterReadingEntry meterReadingEntry : meterReadingEntryList) {
				HashMap<String, String> child = new HashMap<String, String>();

				child.put(res.getString(R.string.child_label_key), dateFormatter.format(meterReadingEntry.getTimestamp()));
				child.put(res.getString(R.string.child_number_key), String.valueOf(meterReadingEntry.getMeterNumber()));
				child.put(res.getString(R.string.child_value_key), String.valueOf(meterReadingEntry.getMeterValue()));

				secList.add(child);
			}
			result.add(secList);
		}
		
		return result;
	}
}