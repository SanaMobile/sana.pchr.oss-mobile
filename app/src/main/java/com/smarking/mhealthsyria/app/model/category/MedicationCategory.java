package com.smarking.mhealthsyria.app.model.category;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.NoSuchElementException;

/**
 * Created by abe707 on 1/12/16.
 */
public class MedicationCategory extends Category {
    public static final String TABLE = "MedicationCategory";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static final String DOSE_DEFAULT = "dose_default";
    public static final String GROUP_UUID = "group_uuid";
    public static final String OTHERNAME = "otherName";
    public static final String INTERVAL_DEFAULT = "interval_default";
    public static final String DOSE_UNIT_UUID = "dose_unit_uuid";
    public static final String TIMES_DEFAULT = "times_default";
    public static final String INTERVAL_UNIT_UUID = "interval_unit_uuid";
    public static final String INTERACTION_WARNING = "interaction_warning";

    private String dose_default;
    private String group_uuid;
    private String otherName;
    private String interval_default;
    private String dose_unit_uuid;
    private String times_default;
    private String interval_unit_uuid;
    private String interactionWarning;

    public MedicationCategory(String uuid, String created, String updated, String synchronized_,
                              String displayName, String displayNameAr, String dose_default,
                              String group_uuid, String otherName, String interval_default,
                              String dose_unit_uuid, String times_default, String interval_unit_uuid,
                              String interactionWarning) {
        super(uuid, created, updated, synchronized_, displayName, displayNameAr);
        this.dose_default = dose_default;
        this.group_uuid = group_uuid;
        this.otherName = otherName;
        this.interval_default = interval_default;
        this.dose_unit_uuid = dose_unit_uuid;
        this.times_default = times_default;
        this.interval_unit_uuid = interval_unit_uuid;
        this.interactionWarning = interactionWarning;
    }

    public MedicationCategory(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.dose_default = jsonObject.getString(DOSE_DEFAULT);
        this.group_uuid = jsonObject.getString(GROUP_UUID);
        this.otherName = jsonObject.getString(OTHERNAME);
        this.interval_default = jsonObject.getString(INTERVAL_DEFAULT);
        this.dose_unit_uuid = jsonObject.getString(DOSE_UNIT_UUID);
        this.times_default = jsonObject.getString(TIMES_DEFAULT);
        this.interval_unit_uuid = jsonObject.getString(INTERVAL_UNIT_UUID);
        this.interactionWarning = jsonObject.getString(INTERACTION_WARNING);
    }


    public MedicationCategory(Parcel in) {
        super(in);
        this.dose_default = in.readString();
        this.group_uuid = in.readString();
        this.otherName = in.readString();
        this.interval_default = in.readString();
        this.dose_unit_uuid = in.readString();
        this.times_default = in.readString();
        this.interval_unit_uuid = in.readString();
        this.interactionWarning = in.readString();
    }


    public MedicationCategory(Cursor c) {
        super(c);
        this.dose_default = getString(c, DOSE_DEFAULT);
        this.group_uuid = getString(c, GROUP_UUID);
        this.otherName = getString(c, OTHERNAME);
        this.interval_default = getString(c, INTERVAL_DEFAULT);
        this.dose_unit_uuid = getString(c, DOSE_UNIT_UUID);
        this.times_default = getString(c, TIMES_DEFAULT);
        this.interval_unit_uuid = getString(c, INTERVAL_UNIT_UUID);
        this.interactionWarning = getString(c, INTERACTION_WARNING);
    }


    public void writeToParcel(Parcel parcel, int i) {

        super.writeToParcel(parcel, i);

        parcel.writeString(dose_default);
        parcel.writeString(group_uuid);
        parcel.writeString(otherName);
        parcel.writeString(interval_default);
        parcel.writeString(dose_unit_uuid);
        parcel.writeString(times_default);
        parcel.writeString(interval_unit_uuid);
        parcel.writeString(interactionWarning);

    }


    public static final Parcelable.Creator<MedicationCategory> CREATOR = new Parcelable.Creator<MedicationCategory>() {
        public MedicationCategory createFromParcel(Parcel in) {
            return new MedicationCategory(in);
        }

        public MedicationCategory[] newArray(int size) {
            return new MedicationCategory[size];
        }
    };

    public ContentValues putContentValues() {

        ContentValues contentValues = super.putContentValues();

        contentValues.put(DOSE_DEFAULT, dose_default);
        contentValues.put(GROUP_UUID, group_uuid);
        contentValues.put(OTHERNAME, otherName);
        contentValues.put(INTERVAL_DEFAULT, interval_default);
        contentValues.put(DOSE_UNIT_UUID, dose_unit_uuid);
        contentValues.put(TIMES_DEFAULT, times_default);
        contentValues.put(INTERVAL_UNIT_UUID, interval_unit_uuid);
        contentValues.put(INTERACTION_WARNING, interactionWarning);
        return contentValues;
    }

    public String getInteractionWarning() {
        return Model.blankIfNull(interactionWarning);
    }

    public String getDose_Default() {
        return dose_default;
    }

    public String getGroup_Uuid() {
        return group_uuid;
    }

    public String getOthername() {
        return otherName;
    }

    public String getInterval_Default() {
        return interval_default;
    }

    public String getDose_Unit_Uuid() {
        return dose_unit_uuid;
    }

    public String getTimes_Default() {
        return times_default;
    }

    public String getInterval_Unit_Uuid() {
        return interval_unit_uuid;
    }

    public void setDose_Default(String dose_default) {
        this.dose_default = dose_default;
    }

    public void setGroup_Uuid(String group_uuid) {
        this.group_uuid = group_uuid;
    }

    public void setOthername(String otherName) {
        this.otherName = otherName;
    }

    public void setInterval_Default(String interval_default) {
        this.interval_default = interval_default;
    }

    public void setDose_Unit_Uuid(String dose_unit_uuid) {
        this.dose_unit_uuid = dose_unit_uuid;
    }

    public void setTimes_Default(String times_default) {
        this.times_default = times_default;
    }

    public void setInterval_Unit_Uuid(String interval_unit_uuid) {
        this.interval_unit_uuid = interval_unit_uuid;
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValueses = new ContentValues[length];
        for (int i = 0; i < length; i++) {
            MedicationCategory mc = new MedicationCategory(jsonArray.getJSONObject(i));
            contentValueses[i] = mc.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }

    public MedicationGroupCategory getMedicationCategory(Context context) {
        String whereClause = " %s = '%s' ";
        Cursor duCursor = context.getContentResolver().query(MedicationGroupCategory.URI, null, String.format(whereClause, UUID, this.group_uuid), null, null);
        if (duCursor.moveToNext()) {
            MedicationGroupCategory medc = new MedicationGroupCategory(duCursor);
            duCursor.close();
            return medc;
        }
        duCursor.close();
        return null;
    }

    public static MedicationCategory getMedicationCategory(Context c, String uuid) throws NoSuchElementException, JSONException {
        Uri uri = Uri.withAppendedPath(MedicationCategory.URI, uuid.toLowerCase());
        Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);

        MedicationCategory mc = null;
        if (cursor.moveToFirst()) {
            mc = new MedicationCategory(cursor);
        }
        cursor.close();
        DatabaseHandler.get(c).closeDatabase();

        return mc;
    }


}