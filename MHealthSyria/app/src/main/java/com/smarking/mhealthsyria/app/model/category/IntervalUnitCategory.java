package com.smarking.mhealthsyria.app.model.category;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hok Hei Tam
 * 1/12/16
 */
public class IntervalUnitCategory extends Category {
    public static final String TABLE = "IntervalUnitCategory";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public IntervalUnitCategory(String uuid, String created, String updated, String synchronized_,
                                String displayName, String displayNameAr) {
        super(uuid, created, updated, synchronized_, displayName, displayNameAr);
    }

    public IntervalUnitCategory(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    public IntervalUnitCategory(Cursor cursor) {
        super(cursor);
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValueses = new ContentValues[length];
        for (int i = 0; i < length; i++) {
            IntervalUnitCategory intervalUnitCategory = new IntervalUnitCategory(jsonArray.getJSONObject(i));
            contentValueses[i] = intervalUnitCategory.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }
}
