package de.hska.rbmk.datenVerwaltung;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MeterReadingsDbAdapter {
	
	private static final String TAG = "MeterReadingsDbAdapter";
	private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String DATABASE_NAME = "AppDatenbank.db";
    private static final String DATABASE_TABLE_METERREADINGS = "meterReadings";
    private static final int DATABASE_VERSION = 2;
    
    public static final String KEY_ROWID = "_id";
    public static final String KEY_CONNECTIONINFO = "connectionInfo";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_METERNUMBER = "meterNumber";
    public static final String KEY_METERVALUE = "meterValue";
    public static final String KEY_METERTYPE = "meterType";
    public static final String KEY_METERVALUEREVISED = "meterValueRevised"; 
    public static final String KEY_SYNCHRONIZED = "synchronized";
    
	public static final String TABLE_METERNUMBERS_NAME = "meternumbers";
    
	public static final String KEY_ID = "_id";
	public static final String KEY_NUMBER = "number";
	public static final String KEY_LASTVALUE = "letzterWert";
	public static final String KEY_LASTUPDATE = "letztesUpdate";
	
	
	private static final String TABLE_METERNUMBERS_CREATE = 
    		"CREATE TABLE " + TABLE_METERNUMBERS_NAME + "(" +
    				KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " +
    				KEY_NUMBER + " TEXT NOT NULL, " +
    				KEY_METERTYPE + " TEXT NOT NULL," +
    				KEY_LASTVALUE + " INTEGER DEFAULT 0," +
    				KEY_LASTUPDATE + " DATETIME" +
    				")";
    
    /**
     * Database creation sql statement
     */
    private static final String TABLE_METERREADINGS_CREATE =
        "create table " + DATABASE_TABLE_METERREADINGS + " (" + KEY_ROWID + " integer primary key autoincrement, "
        + KEY_CONNECTIONINFO + " text not null, " + KEY_TIMESTAMP + " datetime not null, "
        + KEY_METERNUMBER + " integer not null, " + KEY_METERVALUE + " integer not null, "
        + KEY_METERTYPE + " integer not null, " + KEY_METERVALUEREVISED + " boolean not null, " // TODO add enum table for meter type
        + KEY_SYNCHRONIZED + " boolean not null);";

    private final Context mCtx;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
        public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_METERNUMBERS_CREATE);
            db.execSQL(TABLE_METERREADINGS_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_METERREADINGS + ";"); // TODO implement update handling that preserves the old values
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_METERNUMBERS_NAME + ";");
            onCreate(db);
        }
		
	}
	
	/**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
	public MeterReadingsDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}
	
	/**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public MeterReadingsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
    
    public SQLiteDatabase getDb() {
    	return mDbHelper.getWritableDatabase();
    }
    
    /**
     * db access methods
     */
    
    /**
     * Add a new meter reading. If the meter reading is successfully added return
     * the new rowId for that meter reading, otherwise return a -1 to indicate failure.
     * 
     * @param connectionInfo the connection information for that meter reading
     * @param timeStamp the time stamp of the meter reading
     * @param meterNumber the registration number of the meter device of the meter reading
     * @param meterValue the value of the meter reading, rounded to the nearest integer number
     * @param meterType the type of the meter device of the meter reading
     * @param isMeterValueRevised whether or not the meter reading has been revised by the user
     * @param isSynchronized whether or not the meter reading has yet been synchronized (usually false)
     * @return rowId or -1 if failed
     */
    public long addReading(String connectionInfo, Date timeStamp, int meterNumber, 
    		int meterValue, MeterType meterType, boolean isMeterValueRevised, boolean isSynchronized) {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CONNECTIONINFO, connectionInfo);
        initialValues.put(KEY_TIMESTAMP, timeStamp.getTime());
        initialValues.put(KEY_METERNUMBER, meterNumber);
        initialValues.put(KEY_METERVALUE, meterValue);
        initialValues.put(KEY_METERTYPE, meterType.getValue());
        initialValues.put(KEY_METERVALUEREVISED, isMeterValueRevised); 
        initialValues.put(KEY_SYNCHRONIZED, isSynchronized);

        Log.d("MeterReadingsDbAdapter", "storing meter reading " + initialValues.toString());
        return mDb.insert(DATABASE_TABLE_METERREADINGS, null, initialValues);
    }
    
    /**
     * Return a Cursor over the list of all unsynchronized meter readings in the database
     * 
     * @return Cursor over all meter readings that have not yet been synchronized
     */
    public Cursor getUnsynchronizedMeterReadings() {
    	return mDb.query(DATABASE_TABLE_METERREADINGS, new String[] {KEY_ROWID, 
    			KEY_CONNECTIONINFO, KEY_TIMESTAMP, KEY_METERNUMBER, KEY_METERVALUE,
    			KEY_METERTYPE, KEY_METERVALUEREVISED, KEY_SYNCHRONIZED},
    			KEY_SYNCHRONIZED + " = '0'", null, null, null, null);
    }
    
    /**
     * Return a long value of the last meter reading value for the given meter number.
     * @param meterNumber the number of the meter for which the last entry should be returned
     * 
     * @return long value of the last meter reading for that meter number
     */
    public long getLastMeterReadingValueForMeterNumber(int meterNumber) {
    	String query = "SELECT MAX(" + KEY_METERVALUE + ") AS " + KEY_METERVALUE +"Max FROM " + DATABASE_TABLE_METERREADINGS + " WHERE " + KEY_METERNUMBER + " = ?";
    	Cursor queryCursor = mDb.rawQuery(query, new String[] {String.valueOf(meterNumber)});
    	queryCursor.moveToFirst();
    	long retValue = queryCursor.getLong(queryCursor.getColumnIndex(KEY_METERVALUE + "Max"));
    	queryCursor.close();
    	return retValue;
    }
    
    /**
     * Return the most current MeterReadingEntry in the database, or null if the database is empty.
     * 
     * @return MeterReadingEntry 
     */
    public MeterReadingEntry getLatestMeterReading() {
    	
    	Cursor queryCursor =  mDb.query(DATABASE_TABLE_METERREADINGS, new String[] {KEY_ROWID, 
    			KEY_CONNECTIONINFO, KEY_TIMESTAMP, KEY_METERNUMBER, KEY_METERVALUE,
    			KEY_METERTYPE, KEY_METERVALUEREVISED, KEY_SYNCHRONIZED}, null, null, null, null, KEY_TIMESTAMP + " DESC", "1");
    	MeterReadingEntry retValue = null;
    	if (queryCursor.getCount() != 0) {
			queryCursor.moveToFirst();
	    	retValue = new MeterReadingEntry(queryCursor.getInt(queryCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_ROWID)),
	    			queryCursor.getString(queryCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_CONNECTIONINFO)), 
	    			new Date(new Long(queryCursor.getString(queryCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_TIMESTAMP)))), 
	    			queryCursor.getInt(queryCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERNUMBER)), 
	    			queryCursor.getInt(queryCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERVALUE)), 
	    			MeterType.get(queryCursor.getInt(queryCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERTYPE))), 
	    			Boolean.getBoolean(queryCursor.getString(queryCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_METERVALUEREVISED))), 
	    			Boolean.getBoolean(queryCursor.getString(queryCursor.getColumnIndexOrThrow(MeterReadingsDbAdapter.KEY_SYNCHRONIZED))));
    	}
    	queryCursor.close();
    	return retValue;
    }
    
    /**
     * Return a Cursor over the list of all meter readings in the database
     * 
     * @return Cursor over all meter readings
     */
    public Cursor getAllMeterReadings() {
    	return mDb.query(DATABASE_TABLE_METERREADINGS, new String[] {KEY_ROWID, 
    			KEY_CONNECTIONINFO, KEY_TIMESTAMP, KEY_METERNUMBER, KEY_METERVALUE,
    			KEY_METERTYPE, KEY_METERVALUEREVISED, KEY_SYNCHRONIZED}, null, null, null, null, null);
    }
    
    /**
     * Update the meter reading using the details provided. The meter reading
     * to be updated is specified using the rowId, and it is altered to use the 
     * synchronizedStatus value passed in
     * 
     * @param rowId id of meter reading to update
     * @param isSynchronized value to set meter reading synchronized attribute to
     * @return true if the meter reading was successfully updated, false otherwise
     */
    public boolean updateSynchronizedStatus(long rowId, boolean isSynchronized) {
    	ContentValues args = new ContentValues();
        args.put(KEY_SYNCHRONIZED, isSynchronized);

        return mDb.update(DATABASE_TABLE_METERREADINGS, args, KEY_ROWID + "=" + rowId, null) > 0; // as the return value is checked against the primary key, it can only be either 1 or 0
    }

}
