package com.smarking.mhealthsyria.app.model.category;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.model.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-04.
 */
public class VisitCategory extends Category {
    public static final String TABLE = "VisitCategory";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public VisitCategory(String uuid, String created, String updated, String synchronized_,
                         String displayName, String displayNameAr) {
        super(uuid, created, updated, synchronized_, displayName, displayNameAr);
    }

    public VisitCategory(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    public VisitCategory(Cursor cursor) {
        super(cursor);
    }

    public static final Parcelable.Creator<VisitCategory> CREATOR = new Parcelable.Creator<VisitCategory>() {
        public VisitCategory createFromParcel(Parcel in) {
            return new VisitCategory(in);
        }

        public VisitCategory[] newArray(int size) {
            return new VisitCategory[size];
        }
    };

    public VisitCategory(Parcel in) {
        super(in);
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValueses = new ContentValues[length];
        for(int i = 0; i<length; i++){
            VisitCategory visitCategory = new VisitCategory(jsonArray.getJSONObject(i));
            contentValueses[i] = visitCategory.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public static final Model.Builder<VisitCategory> BUILDER = new Model.Builder<VisitCategory>() {
        public VisitCategory build(Cursor cursor) {
            return new VisitCategory(cursor);
        }
    };
}
