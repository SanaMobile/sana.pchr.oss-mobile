package com.smarking.mhealthsyria.app.database.migrations;

import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.event.Event;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * Created by winkler.em@gmail.com, on 05/19/2016.
 */
public class V6 extends Migration {
    public static final String TAG = V6.class.getSimpleName();

    @Override
    public void apply(SQLiteDatabase db, DatabaseHandler databaseHandler) {
        db.execSQL("CREATE TABLE " + Event.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(64, Event.USER, true) + ", " +
                VARCHAR(64, Event.DEVICE, true) + ", " +
                VARCHAR(64, Event.CLINIC, false) + ", " +
                VARCHAR(16, Event.STATUS, false) + ", " +
                VARCHAR(64, Event.CODE, false) + ", " +
                VARCHAR(127, Event.MESSAGE, false) + ", " +
                VARCHAR(127, Event.EXCEPTION, false) + ")");
    }

    @Override
    public void revert(SQLiteDatabase db, DatabaseHandler databaseHandler) {

    }
}
