package com.incorcadit16.criminalintent.database;

public class CrimeDBScheme {
    static public final class CrimeTable {
         public static final String NAME = "crimes";
        
         public static final class cols {
             public static final String UUID = "uuid";
             public static final String TITLE = "title";
             public static final String DATE = "date";
             public static final String SOLVED = "solved";
             public static final String SUSPECT = "suspect";
        }
    }
}
