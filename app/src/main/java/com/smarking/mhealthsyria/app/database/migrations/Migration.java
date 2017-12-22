package com.smarking.mhealthsyria.app.database.migrations;

import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.Model;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.Locale;

/**
 * Created by Saravanan Vaithianathan (svaithia@uwaterloo.ca) on 08/03/15.
 */
public abstract class Migration {
    public abstract void apply(SQLiteDatabase db, DatabaseHandler databaseHandler);
    public abstract void revert(SQLiteDatabase db, DatabaseHandler databaseHandler);

    public static String VARCHAR(int size, String column){ return VARCHAR(size, column, true) ; }
    public static String BLOB(String column){ return BLOB(column, true); }
    public static String INT(String column){ return INT(column, true); }
    public static String TEXT(String column){ return TEXT(column, true); }
    public static String REAL(String column){ return REAL(column, true); }


    public static String VARCHAR(int size, String column, boolean nullity){
        return String.format(Locale.US, " '%s' VARCHAR(%d) %s ", column, size, nullity ? "NULL" : "NOT NULL");
    }

    public static String BLOB(String column, boolean nullity){
        return String.format(Locale.US, " '%s' BLOB %s ", column, nullity ? "NULL" : "NOT NULL");
    }

    public static String INT(String column, boolean nullity){
        return String.format(Locale.US, " %s INT %s ", column, nullity ? "NULL" : "NOT NULL");
    }

    public static String TEXT(String column, boolean nullity){
        return String.format(Locale.US, " '%s' TEXT %s ", column, nullity ? "NULL" : "NOT NULL");
    }

    public static String REAL(String column, boolean nullity){
        return String.format(Locale.US, " '%s' REAL %s ", column, nullity ? "NULL" : "NOT NULL");
    }

    public static String MODEL_COLUMNS = String.format(Locale.US,
            " '%s' CHAR(32) PRIMARY KEY, %s , %s , %s, ", Model.UUID, VARCHAR(20, Model.CREATED),
            VARCHAR(20, Model.UPDATED), VARCHAR(20, Model.SYNCHRONIZED));
}