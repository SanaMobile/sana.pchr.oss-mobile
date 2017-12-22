package com.smarking.mhealthsyria.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.model.category.VisitCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-04.
 */
public class Visit extends Model {
    public static final String TABLE = "Visit";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static final String PATIENT_UUID = "patient_uuid";
    public static final String CATEGORY_UUID = "category_uuid";

    private String patient_uuid;
    private String category_uuid;

    public Visit(String uuid, String created, String updated, String synchronized_, String patient_uuid, String category_uuid) {
        super(uuid, created, updated, synchronized_);
        this.patient_uuid = patient_uuid;
        this.category_uuid = category_uuid;
    }

    public Visit(JSONObject jsonObject) throws JSONException{
        super(jsonObject);
        this.patient_uuid = jsonObject.getString(PATIENT_UUID);
        this.category_uuid = jsonObject.getString(CATEGORY_UUID);
    }

    public Visit(Parcel in){
        super(in);
        this.patient_uuid = in.readString();
        this.category_uuid = in.readString();
    }

    public Visit(Cursor c){
        super(c);
        this.patient_uuid = getString(c, PATIENT_UUID);
        this.category_uuid = getString(c, CATEGORY_UUID);
    }

    public static final Parcelable.Creator<Visit> CREATOR = new Parcelable.Creator<Visit>() {
        public Visit createFromParcel(Parcel in) {
            return new Visit(in);
        }

        public Visit[] newArray(int size) {
            return new Visit[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(patient_uuid);
        parcel.writeString(category_uuid);
    }

    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(PATIENT_UUID, patient_uuid);
        contentValues.put(CATEGORY_UUID, category_uuid);
        return contentValues;
    }

    public String getPatient_uuid() {
        return patient_uuid;
    }

    public void setPatient_uuid(String patient_uuid) {
        this.patient_uuid = patient_uuid;
    }

    public String getCategory_uuid() {
        return category_uuid;
    }

    public void setCategory_uuid(String category_uuid) {
        this.category_uuid = category_uuid;
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValueses = new ContentValues[length];
        for(int i = 0; i<length; i++){
            Visit visit = new Visit(jsonArray.getJSONObject(i));
            contentValueses[i] = visit.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }

    public Visit(String patient_uuid, VisitCategory visitCategory) {
        super("", "", "", "");
        this.patient_uuid = patient_uuid;
        this.category_uuid = visitCategory.uuid;
    }

    public static Visit save(Context context, Visit unsavedVisit){
        Uri uri = context.getContentResolver().insert(URI, unsavedVisit.putContentValues());
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        Visit savedVisit = new Visit(cursor);
        cursor.close();
        return savedVisit;
    }

    public static JSONArray up_sync(Context context){
        JSONArray jsonArray = new JSONArray();
        String whereClause = String.format(" %s = %s OR %s = %s ", SYNCHRONIZED, "'null'", SYNCHRONIZED, "''");

        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        while(cursor.moveToNext()){
            jsonArray.put(new Visit(cursor).jsonObject());
        }
        cursor.close();
        return jsonArray;
    }

    public JSONObject jsonObject(){
        JSONObject jsonObject = super.getJSONObject();
        try {
            jsonObject.put(PATIENT_UUID, patient_uuid);
            jsonObject.put(CATEGORY_UUID, category_uuid);
            jsonObject.put(CATEGORY_UUID, category_uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static Visit get(Context context, String uuid) {
        Visit instance = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI, null, UUID + " = '" + uuid + "'", null, null);
            if (cursor != null && cursor.moveToFirst())
                instance = new Visit(cursor);
        } finally {
            if (cursor != null) cursor.close();
        }
        return instance;
    }

    public static final Model.Builder<Visit> BUILDER = new Model.Builder<Visit>() {
        public Visit build(Cursor cursor) {
            return new Visit(cursor);
        }
    };
}
