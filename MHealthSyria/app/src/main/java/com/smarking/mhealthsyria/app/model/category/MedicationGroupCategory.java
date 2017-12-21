package com.smarking.mhealthsyria.app.model.category;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by abe707 on 1/12/16.
 */
public class MedicationGroupCategory extends Category {
    public static final String TABLE = "MedicationGroupCategory";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public MedicationGroupCategory(String uuid, String created, String updated, String synchronized_,
                                   String displayName, String displayNameAr) {
        super(uuid, created, updated, synchronized_, displayName, displayNameAr);
    }

    public MedicationGroupCategory(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    public MedicationGroupCategory(Cursor cursor) {
        super(cursor);
    }

    public MedicationGroupCategory(Parcel in) {
        super(in);
    }

    public static MedicationGroupCategory get(Context context, String uuid) {
        String whereClause = String.format(" %s = '%s' ", UUID, uuid);
        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        cursor.moveToFirst();
        MedicationGroupCategory mgCategory = new MedicationGroupCategory(cursor);
        if (!cursor.isClosed()) cursor.close();
        return mgCategory;
    }
    
    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValues = new ContentValues[length];
        for (int i = 0; i < length; i++) {
            MedicationGroupCategory mgc = new MedicationGroupCategory(jsonArray.getJSONObject(i));
            contentValues[i] = mgc.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValues);
    }

}
