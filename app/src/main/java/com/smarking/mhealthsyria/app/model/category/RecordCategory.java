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
public class RecordCategory extends Category {
    public static final String TABLE = "RecordCategory";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static final String RECORDTYPE = "recordType";
    public static final String RECORDDATATYPE = "resultDataType";

    private int recordType;
    private String recordDatatype;

    public RecordCategory(String uuid, String created, String updated, String synchronized_,
                          String displayName, String displayNameAr, int recordType, String recordDatatype) {
        super(uuid, created, updated, synchronized_, displayName, displayNameAr);
        this.recordType = recordType;
        this.recordDatatype = recordDatatype;
    }

    public RecordCategory(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.recordType = jsonObject.getInt(RECORDTYPE);
        this.recordDatatype = jsonObject.getString(RECORDDATATYPE);
    }

    public RecordCategory(Cursor cursor) {
        super(cursor);
        this.recordType = getInt(cursor, RECORDTYPE);
        this.recordDatatype = getString(cursor, RECORDDATATYPE);
    }

    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(RECORDTYPE, recordType);
        contentValues.put(RECORDDATATYPE, recordDatatype);
        return contentValues;
    }

    public int getRecordType() {
        return recordType;
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
    }

    public String getRecordDatatype() {
        return recordDatatype;
    }

    public void setRecordDatatype(String recordDatatype) {
        this.recordDatatype = recordDatatype;
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValueses = new ContentValues[length];
        for(int i = 0; i<length; i++){
            RecordCategory recordCategory = new RecordCategory(jsonArray.getJSONObject(i));
            contentValueses[i] = recordCategory.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }

    public static RecordCategory get(Context context, String uuid) {
        String whereClause = String.format(" %s = '%s' ", UUID, uuid);
        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        RecordCategory recordCategory = cursor.moveToNext() ? new RecordCategory(cursor) : null;
        if(!cursor.isClosed()) cursor.close();
        return recordCategory;
    }

    public static final Model.Builder<RecordCategory> BUILDER = new Model.Builder<RecordCategory>() {
        public RecordCategory build(Cursor cursor) {
            return new RecordCategory(cursor);
        }
    };

}
