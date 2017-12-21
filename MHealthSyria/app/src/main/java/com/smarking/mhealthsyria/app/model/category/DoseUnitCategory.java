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
public class DoseUnitCategory extends Category {
    public static final String TABLE = "DoseUnitCategory";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public DoseUnitCategory(String uuid, String created, String updated, String synchronized_,
                            String displayName, String displayNameAr) {
        super(uuid, created, updated, synchronized_, displayName, displayNameAr);
    }

    public DoseUnitCategory(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    public DoseUnitCategory(Cursor cursor) {
        super(cursor);
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValueses = new ContentValues[length];
        for (int i = 0; i < length; i++) {
            DoseUnitCategory doseUnitCategory = new DoseUnitCategory(jsonArray.getJSONObject(i));
            contentValueses[i] = doseUnitCategory.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }
}
