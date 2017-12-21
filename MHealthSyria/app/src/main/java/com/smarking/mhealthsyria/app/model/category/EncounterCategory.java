package com.smarking.mhealthsyria.app.model.category;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.model.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-04.
 */
public class EncounterCategory extends Category {
    public static final String TABLE = "EncounterCategory";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public EncounterCategory(String uuid, String created, String updated, String synchronized_,
                             String displayName, String displayNameAr) {
        super(uuid, created, updated, synchronized_, displayName, displayNameAr);
    }

    public EncounterCategory(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    public EncounterCategory(Cursor cursor) {
        super(cursor);
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValueses = new ContentValues[length];
        for(int i = 0; i<length; i++){
            EncounterCategory encounterCategory = new EncounterCategory(jsonArray.getJSONObject(i));
            contentValueses[i] = encounterCategory.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }

    public static EncounterCategory get(Context context, String uuid) {
        EncounterCategory instance = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI, null, UUID + " = " + uuid, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                instance = new EncounterCategory(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return instance;
    }

    public static final Model.Builder<EncounterCategory> BUILDER = new Model.Builder<EncounterCategory>() {
        public EncounterCategory build(Cursor cursor) {
            return new EncounterCategory(cursor);
        }
    };
}
