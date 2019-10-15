package com.incorcadit16.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.incorcadit16.criminalintent.database.CrimeBaseHelper;
import com.incorcadit16.criminalintent.database.CrimeCursorWrapper;
import com.incorcadit16.criminalintent.database.CrimeDBScheme.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public List<Crime> getCrimes() {
        List<Crime> list = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null,null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                list.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return list;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null, // null - выбрать все столбцы
                whereClause,
                whereArgs,
                null,
                null,
                null
        );


        return new CrimeCursorWrapper(cursor);
    }

    public Crime getCrime(UUID id) {
        CrimeCursorWrapper cursor = queryCrimes(CrimeTable.cols.UUID + "= ?", new String[]{id.toString()});

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getmId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME, values, CrimeTable.cols.UUID + " = ?", new String[] {uuidString});
    }

    private  CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(context).getWritableDatabase();
    }

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public void addCrime(Crime crime) {
        ContentValues values = getContentValues(crime);

        mDatabase.insert(CrimeTable.NAME,null,values);
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.cols.UUID, crime.getmId().toString());
        values.put(CrimeTable.cols.TITLE, crime.getmTitle());
        values.put(CrimeTable.cols.DATE, crime.getmDate().getTime());
        values.put(CrimeTable.cols.SOLVED, crime.ismSolved()? 1:0);
        values.put(CrimeTable.cols.SUSPECT, crime.getSuspect());
        return values;
    }

    public File getPhotoFile(Crime crime) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir,crime.getPhotoFileName());
    }
}
