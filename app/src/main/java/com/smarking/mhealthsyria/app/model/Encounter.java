package com.smarking.mhealthsyria.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.api.auth.provision.DeviceProvisioner;
import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.model.category.EncounterCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-04.
 */
public class Encounter extends Model{
    public static String TABLE = "Encounter";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static String PHYSICIAN_UUID = "physician_uuid";
    public static String DEVICE_UUID = "device_uuid";
    public static String CLINIC_UUID = "clinic_uuid";
    public static String VISIT_UUID = "visit_uuid";
    public static String CATEGORY_UUID = "category_uuid";

    public final String physician_uuid;
    public final String device_uuid;
    public final String clinic_uuid;
    public final String visit_uuid;
    public final String category_uuid;

    public Encounter(String uuid, String created, String updated, String synchronized_, String physician_uuid, String device_uuid, String clinic_uuid, String visit_uuid, String category_uuid) {
        super(uuid, created, updated, synchronized_);
        this.physician_uuid = physician_uuid;
        this.device_uuid = device_uuid;
        this.clinic_uuid = clinic_uuid;
        this.visit_uuid = visit_uuid;
        this.category_uuid = category_uuid;
    }

    public Encounter(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.physician_uuid = jsonObject.getString(PHYSICIAN_UUID);
        this.device_uuid = jsonObject.getString(DEVICE_UUID);
        this.clinic_uuid = jsonObject.getString(CLINIC_UUID);
        this.visit_uuid = jsonObject.getString(VISIT_UUID);
        this.category_uuid = jsonObject.getString(CATEGORY_UUID);
    }

    public Encounter(Cursor cursor) {
        super(cursor);
        this.physician_uuid = getString(cursor, PHYSICIAN_UUID);
        this.device_uuid = getString(cursor, DEVICE_UUID);
        this.clinic_uuid = getString(cursor, CLINIC_UUID);
        this.visit_uuid = getString(cursor, VISIT_UUID);
        this.category_uuid = getString(cursor, CATEGORY_UUID);
    }

    public Encounter(Context context, Visit visit) throws IllegalAccessException {
        super("", "", "", "");
        SessionManager sessionManager = SessionManager.get(context);
        Map<String, String> userData = sessionManager.getCurrentUserData();
        this.physician_uuid = userData.get(SessionManager.KEY_UUID);
        this.device_uuid = new DeviceProvisioner(context).getAccount().name;
        this.visit_uuid = visit.uuid;
        this.clinic_uuid = userData.get(SessionManager.DEVICE_CLINIC_UUID);
        this.category_uuid = context.getString(R.string.general_encounter_cat);
    }

    public Encounter(Parcel in) {
        super(in);
        this.physician_uuid = in.readString();
        this.device_uuid = in.readString();
        this.clinic_uuid = in.readString();
        this.visit_uuid = in.readString();
        this.category_uuid = in.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.physician_uuid);
        parcel.writeString(this.device_uuid);
        parcel.writeString(this.clinic_uuid);
        parcel.writeString(this.visit_uuid);
        parcel.writeString(this.category_uuid);
    }

    public static final Parcelable.Creator<Encounter> CREATOR = new Parcelable.Creator<Encounter>() {
        public Encounter createFromParcel(Parcel in) {
            return new Encounter(in);
        }

        public Encounter[] newArray(int size) {
            return new Encounter[size];
        }
    };
    
    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(PHYSICIAN_UUID, physician_uuid);
        contentValues.put(DEVICE_UUID, device_uuid);
        contentValues.put(CLINIC_UUID, clinic_uuid);
        contentValues.put(VISIT_UUID, visit_uuid);
        contentValues.put(CATEGORY_UUID, category_uuid);
        return contentValues;
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValueses = new ContentValues[length];
        for(int i = 0; i<length; i++){
            Encounter encounter = new Encounter(jsonArray.getJSONObject(i));
            contentValueses[i] = encounter.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }

    public Encounter(Context context, Visit visit, EncounterCategory encounterCategory) throws IllegalAccessException {
        super("", "", "", "");
        SessionManager sessionManager = SessionManager.get(context);
        Map<String, String> userData = sessionManager.getCurrentUserData();
        this.physician_uuid = userData.get(SessionManager.KEY_UUID);
        this.device_uuid = new DeviceProvisioner(context).getAccount().name;
        this.visit_uuid = visit.uuid;

        Cursor cursor = context.getContentResolver().query(Clinic.URI, null, null, null, null);
        cursor.moveToFirst();
        this.clinic_uuid = new Clinic(cursor).uuid;
        cursor.close();

        this.category_uuid = encounterCategory.uuid;
    }

    public static JSONArray up_sync(Context context){
        JSONArray jsonArray = new JSONArray();
        String whereClause = String.format(" %s = %s OR %s = %s ", SYNCHRONIZED, "'null'", SYNCHRONIZED, "''");

        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        while(cursor.moveToNext()){
            jsonArray.put(new Encounter(cursor).jsonObject());
        }
        cursor.close();
        return jsonArray;
    }

    public JSONObject jsonObject(){
        JSONObject jsonObject = super.getJSONObject();
        try {
            jsonObject.put(PHYSICIAN_UUID, physician_uuid);
            jsonObject.put(DEVICE_UUID, device_uuid);
            jsonObject.put(CLINIC_UUID, clinic_uuid);
            jsonObject.put(VISIT_UUID, visit_uuid);
            jsonObject.put(CATEGORY_UUID, category_uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static Encounter get(Context context, String uuid) {
        Encounter instance = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI, null, UUID + " = '" + uuid +"'", null, null);
            if (cursor != null && cursor.moveToFirst()) {
                instance = new Encounter(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return instance;
    }

    public static final Model.Builder<Encounter> BUILDER = new Model.Builder<Encounter>() {
        public Encounter build(Cursor cursor) {
            return new Encounter(cursor);
        }
    };
}
