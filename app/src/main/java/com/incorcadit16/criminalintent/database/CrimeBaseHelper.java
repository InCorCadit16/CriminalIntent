package com.incorcadit16.criminalintent.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.incorcadit16.criminalintent.database.CrimeDBScheme.CrimeTable;

public class CrimeBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String NAME = "crimeBase.db";

    public CrimeBaseHelper(Context context) {
        super(context,NAME,null,VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + CrimeTable.NAME + "(" +
        " _id integer primary key autoincrement, " +
        CrimeTable.cols.UUID + ", " +
        CrimeTable.cols.TITLE + ", " +
        CrimeTable.cols.DATE + ", " +
        CrimeTable.cols.SOLVED+ ", " +
        CrimeTable.cols.SUSPECT +
        ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
