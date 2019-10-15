package com.incorcadit16.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.incorcadit16.criminalintent.Crime;
import com.incorcadit16.criminalintent.database.CrimeDBScheme.CrimeTable;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {

    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(CrimeTable.cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.cols.DATE));
        int isSolved = getInt(getColumnIndex(CrimeTable.cols.SOLVED));
        String suspect = getString(getColumnIndex(CrimeTable.cols.SUSPECT));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setmDate(new Date(date));
        crime.setmTitle(title);
        crime.setmSolved(isSolved != 0);
        crime.setSuspect(suspect);

        return crime;
    }
}
