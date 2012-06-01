package de.hska.rbmk.zaehlerstand;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MeterNumbersOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
	public static final String DATABASE_NAME = "meternumbers.db";
	public static final String TABLE_METERNUMBERS_NAME = "meternumbers";
    
	public static final String KEY_ID = "_id";
	public static final String KEY_NUMBER = "number";
	public static final String KEY_METERTYPE = "meterType";
	
	private static final String TABLE_METERNUMBERS_CREATE = 
    		"CREATE TABLE " + TABLE_METERNUMBERS_NAME + "(" +
    				KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " +
    				KEY_NUMBER + " TEXT NOT NULL, " +
    				KEY_METERTYPE + " TEXT NOT NULL" +
    				")";

    MeterNumbersOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_METERNUMBERS_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_METERNUMBERS_NAME);
		onCreate(db);
		
	}
}