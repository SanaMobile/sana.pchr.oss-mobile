package com.smarking.mhealthsyria.app.database.test;

import android.content.Context;
import android.test.AndroidTestCase;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * Created by winkler.em@gmail.com, on 06/07/2016.
 */
public class ContentProviderTest  extends AndroidTestCase {
    public static final String TAG = ContentProviderTest.class.getSimpleName();

    Context context;

    public void setUp() {
        context = getContext();
        SQLiteDatabase.loadLibs(context);
    }

    public void testInsert(){

    }

    public void testQuery(){

    }

    public void testDelete(){

    }

    public void testUpdate(){

    }
}
