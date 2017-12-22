package com.smarking.mhealthsyria.app.model;

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
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-25.
 */
public class Device extends Model {
    public static String TABLE = "Device";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public final static String DEVICEMAC = "deviceMAC";
    public final static String NAME = "name";
    public final static String CLINICUUID = "clinic_uuid";

    public final String deviceMAC;
    public final String name;
    public final String clinic_uuid;


    public Device(String uuid, String created, String updated, String synchronized_, String deviceMAC, String name, String clinic_uuid) {
        super(uuid, created, updated, synchronized_);
        this.deviceMAC = deviceMAC;
        this.name = name;
        this.clinic_uuid = clinic_uuid;
    }

    public Device(Parcel in) {
        super(in);
        this.deviceMAC = in.readString();
        this.name = in.readString();
        this.clinic_uuid = in.readString();
    }

    public Device(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.deviceMAC = jsonObject.getString(DEVICEMAC);
        this.name = jsonObject.getString(NAME);
        this.clinic_uuid = jsonObject.getString(CLINICUUID);
    }

    public Device(Cursor c) {
        super(c);
        this.deviceMAC = getString(c, DEVICEMAC);
        this.name = getString(c, NAME);
        this.clinic_uuid = getString(c, CLINICUUID);
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeString(deviceMAC);
        parcel.writeString(name);
        parcel.writeString(clinic_uuid);
    }


    public JSONObject jsonObject() {
        JSONObject jsonObject = super.getJSONObject();
        try {
            jsonObject.put(DEVICEMAC, deviceMAC);
            jsonObject.put(NAME, name);
            jsonObject.put(CLINICUUID, clinic_uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONArray up_sync(Context context) {
        JSONArray jsonArray = new JSONArray();
        String whereClause = String.format(" %s = %s OR %s = %s ", SYNCHRONIZED, "'null'", SYNCHRONIZED, "''");

        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        while (cursor.moveToNext()) {
            jsonArray.put(new Device(cursor).jsonObject());
        }
        cursor.close();
        return jsonArray;
    }

    public static Device get(Context context, String uuid){
        String whereClause = String.format(" %s = '%s' ", UUID, uuid);
        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        Device device = cursor.moveToNext() ? new Device(cursor) : null;
        cursor.close();
        return device;
    }

    @Override
    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(DEVICEMAC, deviceMAC);
        contentValues.put(NAME, name);
        contentValues.put(CLINICUUID, clinic_uuid);
        return contentValues;
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValues = new ContentValues[length];
        for(int i = 0; i<length; i++){
            Device device = new Device(jsonArray.getJSONObject(i));
            contentValues[i] = device.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValues);
    }


    public static final Model.Builder<Device> BUILDER = new Model.Builder<Device>() {
        public Device build(Cursor cursor) {
            return new Device(cursor);
        }
    };
}
