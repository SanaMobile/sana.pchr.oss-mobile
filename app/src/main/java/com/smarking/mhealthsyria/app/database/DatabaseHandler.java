package com.smarking.mhealthsyria.app.database;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.database.migrations.Migration;
import com.smarking.mhealthsyria.app.database.migrations.V1;
import com.smarking.mhealthsyria.app.database.migrations.V2;
import com.smarking.mhealthsyria.app.database.migrations.V3;
import com.smarking.mhealthsyria.app.database.migrations.V4;
import com.smarking.mhealthsyria.app.database.migrations.V5;
import com.smarking.mhealthsyria.app.database.migrations.V6;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Saravanan Vaithianathan (svaithia@uwaterloo.ca) on 3/7/2015.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    public static final int VERSION = 6;
    public static final String NAME = "mhealthsyria.db";

    private Migration[] migrations = {
            new V1(),
            new V2(),
            new V3(),
            new V4(),
            new V5(),
            new V6()

    };

    private static DatabaseHandler instance;
    private Context context;

    public static synchronized DatabaseHandler get(Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context.getApplicationContext());
        }
        return instance;
    }

    private WeakReference<SQLiteDatabase> reference = new WeakReference<SQLiteDatabase>(null);
    private AtomicInteger connections = new AtomicInteger(0);

    private DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
        this.context = context;
    }

    private SQLiteDatabase getDatabase() {
        SQLiteDatabase database = reference.get();

        if (database == null) {
            database = getWritableDatabase(getKey());
            reference = new WeakReference<SQLiteDatabase>(database);
        }

        return database;
    }

    private String getKey() {
        if(Constants.DEBUG){
            return "";
        }
        else {
            WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            return wm.getConnectionInfo().getMacAddress();
        }
    }

    public SQLiteDatabase openDatabase() {
        SQLiteDatabase result = getDatabase();
        connections.incrementAndGet();
        return result;
    }

    public void closeDatabase() {
        if (connections.decrementAndGet() == 0) {
            getDatabase().close();
            reference.clear();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (int i = 0; i < VERSION; i++) {
            migrations[i].apply(db, this);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; i++) {
            migrations[i].apply(db, this);
        }
    }
}
