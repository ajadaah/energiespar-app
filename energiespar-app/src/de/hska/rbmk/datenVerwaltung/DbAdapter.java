package de.hska.rbmk.datenVerwaltung;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbAdapter {
	
	private static final String TAG = "DbAdapter";
	private DatabaseHelper mDbHelper;
    private static SQLiteDatabase mDb;
    
    private static final String DATABASE_PATH = "/data/data/de.hska.rbmk/databases/";
    private static final String DATABASE_NAME = "AppDatenbank.db";
    public static final String DATABASE_TABLE_METERREADINGS = "meterReadings";
    private static final int DATABASE_VERSION = 2;
    
    // Zählerstände Tabelle
    public static final String KEY_ROWID = "_id";
    public static final String KEY_CONNECTIONINFO = "connectionInfo";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_METERNUMBER = "meterNumber";
    public static final String KEY_METERVALUE = "meterValue";
    public static final String KEY_METERTYPE = "meterType";
    public static final String KEY_METERVALUEREVISED = "meterValueRevised"; 
    public static final String KEY_SYNCHRONIZED = "synchronized";
    
    // Waschmaschinen Tabelle
    public static final String KEY_HERSTELLER = "hersteller";
    public static final String KEY_MODELL = "modell";
    public static final String KEY_PREIS = "preis";
    public static final String KEY_WASSERVERBRAUCH = "wasserverbrauch";
    public static final String KEY_STROMVERBRAUCH = "stromverbrauch";
    public static final String KEY_LADEVOLUMEN = "ladevolumen"; 
    public static final String KEY_EEK = "energieeffizienzklasse";
    public static final String KEY_STRICHCODE = "strichcode";
    
    public static final String KEY_MASSGEDECKE = "massgedecke";
    public static final String KEY_NUTZINHALT = "nutzinhalt";
    
    
	public static final String TABLE_METERNUMBERS_NAME = "meternumbers";
	public static final String TABLE_WM_NAME = "gv_waschmaschinen";
	public static final String TABLE_KS_NAME = "gv_kuehlschraenke";
	public static final String TABLE_SM_NAME = "gv_spuelmaschinen";
	
    
	public static final String KEY_ID = "_id";
	public static final String KEY_NUMBER = "number";


    private final Context mCtx;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
	    private final Context myContext;
	    
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.myContext = context;
		}
		
		public void createDataBase() throws IOException{
			 
	    	boolean dbExist = checkDataBase();
	 
	    	if(dbExist){
	    		//do nothing - database already exist
	    	}else{
	 
	    		//By calling this method and empty database will be created into the default system path
	               //of your application so we are gonna be able to overwrite that database with our database.
	        	this.getReadableDatabase();
	 
	        	try {
	 
	    			copyDataBase();
	 
	    		} catch (IOException e) {
	 
	        		throw new Error("Error copying database");
	 
	        	}
	    	}
	 
	    }
		
		private boolean checkDataBase(){
			 
	    	SQLiteDatabase checkDB = null;
	 
	    	try{
	    		String myPath = DATABASE_PATH + DATABASE_NAME;
	    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	 
	    	}catch(SQLiteException e){
	 
	    		//database does't exist yet.
	 
	    	}
	 
	    	if(checkDB != null){
	 
	    		checkDB.close();
	 
	    	}
	 
	    	return checkDB != null ? true : false;
	    }
		
		private void copyDataBase() throws IOException{
			 
	    	//Open your local db as the input stream
	    	InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
	 
	    	// Path to the just created empty db
	    	String outFileName = DATABASE_PATH + DATABASE_NAME;
	 
	    	//Open the empty db as the output stream
	    	OutputStream myOutput = new FileOutputStream(outFileName);
	 
	    	//transfer bytes from the inputfile to the outputfile
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	while ((length = myInput.read(buffer))>0){
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	//Close the streams
	    	myOutput.flush();
	    	myOutput.close();
	    	myInput.close();
	 
	    }
		
		public void openDataBase() throws SQLException{
			 
	    	//Open the database
	        String myPath = DATABASE_PATH + DATABASE_NAME;
	        mDb = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	 
	    }
	 
	    @Override
		public synchronized void close() {
	 
	    	    if(mDb != null)
	    	    	mDb.close();
	 
	    	    super.close();
	 
		}
	 
		@Override
		public void onCreate(SQLiteDatabase db) {
	 
		}
	 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	 
		}
		
	}
	
	/**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
	public DbAdapter(Context ctx) {
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
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
 
        try {
        	mDbHelper.createDataBase();
        } catch (IOException ioe) {
        	throw new Error("Unable to create database");
        }
        
//        try {
//        	mDbHelper.openDataBase();
//        }catch(SQLException sqle){
//        	throw sqle;
//        }
        
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
    public void addReading(String connectionInfo, Date timeStamp, int meterNumber, 
    		int meterValue, MeterType meterType, boolean isMeterValueRevised, boolean isSynchronized) {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CONNECTIONINFO, connectionInfo);
        initialValues.put(KEY_TIMESTAMP, timeStamp.getTime());
        initialValues.put(KEY_METERNUMBER, meterNumber);
        initialValues.put(KEY_METERVALUE, meterValue);
        initialValues.put(KEY_METERTYPE, meterType.getValue());
        initialValues.put(KEY_METERVALUEREVISED, isMeterValueRevised); 
        initialValues.put(KEY_SYNCHRONIZED, isSynchronized);

        Log.d("DbAdapter", "storing meter reading " + initialValues.toString());
        
        mDb.insert(DATABASE_TABLE_METERREADINGS, null, initialValues);
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
    
    public long getLastMeterReadingDateForMeterNumber(int meterNumber) {
    	String query = "SELECT MAX(" + KEY_TIMESTAMP + ") AS " + KEY_TIMESTAMP +"Max FROM " + DATABASE_TABLE_METERREADINGS + " WHERE " + KEY_METERNUMBER + " = ?";
    	Cursor queryCursor = mDb.rawQuery(query, new String[] {String.valueOf(meterNumber)});
    	queryCursor.moveToFirst();
    	long retValue = queryCursor.getLong(queryCursor.getColumnIndex(KEY_TIMESTAMP + "Max"));
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
	    	retValue = new MeterReadingEntry(queryCursor.getInt(queryCursor.getColumnIndexOrThrow(DbAdapter.KEY_ROWID)),
	    			queryCursor.getString(queryCursor.getColumnIndexOrThrow(DbAdapter.KEY_CONNECTIONINFO)), 
	    			new Date(new Long(queryCursor.getString(queryCursor.getColumnIndexOrThrow(DbAdapter.KEY_TIMESTAMP)))), 
	    			queryCursor.getInt(queryCursor.getColumnIndexOrThrow(DbAdapter.KEY_METERNUMBER)), 
	    			queryCursor.getInt(queryCursor.getColumnIndexOrThrow(DbAdapter.KEY_METERVALUE)), 
	    			MeterType.get(queryCursor.getInt(queryCursor.getColumnIndexOrThrow(DbAdapter.KEY_METERTYPE))), 
	    			Boolean.getBoolean(queryCursor.getString(queryCursor.getColumnIndexOrThrow(DbAdapter.KEY_METERVALUEREVISED))), 
	    			Boolean.getBoolean(queryCursor.getString(queryCursor.getColumnIndexOrThrow(DbAdapter.KEY_SYNCHRONIZED))));
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
