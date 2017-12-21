package com.smarking.mhealthsyria.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Saravanan Vaithianathan (svaithia@uwaterloo.ca) on 08/03/15.
 */
public abstract class Model implements Parcelable{

    /**
     * Constructs a new instance from the current row of a Cursor
     *
     * @param <T>
     */
    public interface Builder<T extends Model> {
        T build(Cursor cursor);
    }

    public static final String UUID = "uuid";

    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String SYNCHRONIZED = "synchronized";
    private static final String TAG = Model.class.getSimpleName();

    public String uuid;
    private String created;
    private String updated;
    private String synchronized_;
    public static SimpleDateFormat LEAST_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat ISO8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static SimpleDateFormat ISO8601_FORMAT_TZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    static {
        ISO8601_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public Model(String uuid, String created, String updated, String synchronized_){
        this.uuid = uuid.toLowerCase();
        this.created = created;
        this.updated = updated;
        this.synchronized_ = synchronized_;
    }

    public static String blankIfNull(String str) {
        if (str == null || str.equals("null"))
            return "";
        else
            return str;
    }

    public Model(Parcel in){
        this.uuid = in.readString();
        this.created = in.readString();
        this.updated = in.readString();
        this.synchronized_ = in.readString();
    }
        //TODO CREDENTIAL REQUEST SHOULD RETURN THESE VALUES AS WELL
    public Model(JSONObject jsonObject) throws JSONException {
        this.uuid = jsonObject.getString(UUID);
        this.created = jsonObject.has(CREATED) ? jsonObject.getString(CREATED) : "";
        this.updated = jsonObject.has(UPDATED) ? jsonObject.getString(UPDATED) : "";
        this.synchronized_ = jsonObject.has(SYNCHRONIZED) ? jsonObject.getString(SYNCHRONIZED) : "";
    }

    public Model(Cursor c) {
        this.uuid = getString(c, UUID);
        this.created = getString(c, CREATED);
        this.updated = getString(c, UPDATED);
        this.synchronized_ = getString(c, SYNCHRONIZED);
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(uuid);
        parcel.writeString(created);
        parcel.writeString(updated);
        parcel.writeString(synchronized_);
    }

    public ContentValues putContentValues(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(UUID, uuid);
        contentValues.put(CREATED, created);
        contentValues.put(UPDATED, updated);
        contentValues.put(SYNCHRONIZED, synchronized_);
        return contentValues;
    }

    public JSONObject getJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(UUID, uuid);
            jsonObject.put(CREATED, created);
            jsonObject.put(UPDATED, updated);
            jsonObject.put(SYNCHRONIZED, synchronized_);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("MODEL", "BIG ERROR");
        }

        return jsonObject;
    }

    public void updateTime(boolean created_now, boolean updated_now, boolean synced_now){
        ISO8601_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = ISO8601_FORMAT.format(new Date());

        if(created_now){
            created = currentTime;
        }
        if(updated_now){
            updated = currentTime;
        }
        if(synced_now){
            synchronized_ = currentTime;
        }
    }

    public int describeContents() {
        return 0;
    }

    public ContentValues save_helper(ContentValues contentValues, String created, String updated, String synchronized_){
        if(created != null){
            contentValues.put(CREATED, created);
        }
        if(updated != null){
            contentValues.put(UPDATED, updated);
        }
        if(synchronized_ != null){
            contentValues.put(SYNCHRONIZED, synchronized_);
        }
        return contentValues;
    }

    public void markForUpdate() {
        updated = ISO8601_FORMAT_TZ.format(Calendar.getInstance().getTime());
        synchronized_ = "";
    }

    // HELPER METHODS

    public static String getString(Cursor c, String column){
        return c.getString(c.getColumnIndex(column));
    }

    public static Date getDate(Cursor c, String column){
        String dateStr = getString(c, column);
        try {
            return ISO8601_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            Log.e(TAG, "error parsing date from cursor");
            return new Date();
        }
    }

    public static int getInt(Cursor c, String column){
        return c.getInt(c.getColumnIndex(column));
    }

    public static double getDouble(Cursor c, String column){
        return c.getDouble(c.getColumnIndex(column));
    }



    public static String formatDateTime(Context context, String timeToFormat) {
        String finalDateTime = "";
        Date date = null;

        if (timeToFormat != null) {
            try {
                date = ISO8601_FORMAT.parse(timeToFormat);
            } catch (ParseException e) {
                date = null;
            }

            if (date != null) {
                long when = date.getTime();
                int flags = 0;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
                flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;

                finalDateTime = android.text.format.DateUtils.formatDateTime(context,
                        when + TimeZone.getDefault().getOffset(when), flags);
            }
        }
        return finalDateTime;
    }

    public String getCreated() {
        return created;
    }

    public Date getCreatedDate() {
        Date output = null;
        try {
            output = ISO8601_FORMAT_TZ.parse(created);
        } catch (ParseException e) {
            try {
                output = ISO8601_FORMAT.parse(created);
            } catch (ParseException e2) {
                Log.e(TAG, "Error Parsing date: " + created);
            }
        }
        return output;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getSynchronized_() {
        return synchronized_;
    }

    public static String getTableColumnName(String table, String columnName){
        return table + "." + columnName;
    }

    @Override
    public boolean equals(Object o) {
        try{
            Model mod = (Model) o;
            return mod.uuid.equals(this.uuid);
        }catch(Exception e){}
        return super.equals(o);
    }

    /**
     * Factory method to return a JSONArray of model records stored in the
     * database that should be scheduled for synchronization.
     *
     * @param context
     * @param uri
     * @param builder
     * @param <T>
     * @return
     * @throws NullPointerException
     */
    public static <T extends Model> JSONArray up_sync(Context context, Uri uri, Model.Builder<T> builder){
        JSONArray jsonArray = new JSONArray();
        String whereClause = String.format(" %s=? OR %s=? ", SYNCHRONIZED, SYNCHRONIZED);
        String[] selectionArgs = new String[]{ "'null'", "''" };
        Cursor cursor = context.getContentResolver().query(uri, null,
                whereClause, selectionArgs, null);
        while(cursor.moveToNext()){
            T object = builder.build(cursor);
            jsonArray.put(object.getJSONObject());
        }
        cursor.close();
        return jsonArray;
    }
}
