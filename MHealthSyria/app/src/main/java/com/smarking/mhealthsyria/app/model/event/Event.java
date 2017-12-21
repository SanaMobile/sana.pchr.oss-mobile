package com.smarking.mhealthsyria.app.model.event;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.model.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a recordable event that occurs during app usage.
 *
 * Created by winkler.em@gmail.com, on 05/03/2016.
 */
public class Event extends Model{
    public static final String TAG = Event.class.getSimpleName();
    public static final String TABLE = Event.class.getSimpleName();
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static final String USER = "user";
    public static final String DEVICE = "device";
    public static final String CLINIC = "clinic";
    public static final String STATUS = "status";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String EXCEPTION = "exception";

    public static final String NO_EXCEPTION = "NO EXCEPTION";

    /**
     * An enumeration of allowed event status values.
     */
    public enum Status{
        ERROR,
        SUCCESS,
        FAIL,
        OK
    }

    /**
     * Allowable event codes.
     */
    public enum Code {
        USER_LOGIN,
        USER_LOGOUT,
        ENCOUNTER,
        ENCOUNTER_STARTED,
        ENCOUNTER_COMPLETE,
        SYNC_START,
        SYNC_COMPLETE,
        SYNC,
        UPDATE,
        EXIT_STATUS,
        RUNTIME,
        UNHANDLED_EXCEPTION;

        public static final Code fromString(String value){
            Code code = null;
            for(Code c:Code.values()){
                if(value.compareToIgnoreCase(c.name()) == 0){
                    code = c;
                    break;
                }
            }
            if(code == null) throw new IllegalArgumentException("Invalid Code string");
            return code;
        }
    }

    public String user = "";
    public String device = "";
    public String clinic = "";
    public String status = Status.FAIL.name();
    public String code = "";
    public String message = "";
    public String exception = NO_EXCEPTION;

    public Event(String user, String device, String clinic, String status, String code, String message, String exception) {
        super("", "", "", "");
        this.status = status;
        this.code = code;
        message = String.valueOf(message);
        int messageLength = (message.length() > 128)? 127: message.length();
        this.message = message.substring(0, messageLength);

        exception = String.valueOf(exception);
        messageLength = (exception.length() > 128)? 127: exception.length();
        this.exception = exception.substring(0, messageLength);
    }

    public Event(Parcel in){
        super(in);
        user = in.readString();
        device = in.readString();
        clinic = in.readString();
        status = in.readString();
        code = in.readString();
        message = in.readString();
        exception = in.readString();
    }

    public Event(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        user = jsonObject.getString(USER);
        device = jsonObject.getString(DEVICE);
        clinic = jsonObject.getString(CLINIC);
        status = jsonObject.getString(STATUS);
        code = jsonObject.getString(CODE);
        message = jsonObject.getString(MESSAGE);
        exception = jsonObject.getString(EXCEPTION);
    }

    public Event(Cursor c){
        super(c);
        user = getString(c, USER);
        device = getString(c, DEVICE);
        clinic = getString(c, CLINIC);
        status = getString(c, STATUS);
        code = getString(c, CODE);
        message = getString(c, MESSAGE);
        exception = getString(c, EXCEPTION);
    }

    @Override
    public JSONObject getJSONObject(){
        JSONObject jsonObject = super.getJSONObject();
        try {
            jsonObject.put(USER, user);
            jsonObject.put(DEVICE, device);
            jsonObject.put(CLINIC, clinic);
            jsonObject.put(STATUS, status);
            jsonObject.put(CODE, code);
            jsonObject.put(MESSAGE, message);
            jsonObject.put(EXCEPTION, exception);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR serializing object");
        }
        return jsonObject;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeString(user);
        parcel.writeString(device);
        parcel.writeString(clinic);
        parcel.writeString(status);
        parcel.writeString(code);
        parcel.writeString(message);
        parcel.writeString(exception);
    }

    public ContentValues putContentValues(){
        ContentValues values = super.putContentValues();
        values.put(USER, user);
        values.put(DEVICE, device);
        values.put(CLINIC, clinic);
        values.put(STATUS, status);
        values.put(CODE, code);
        values.put(MESSAGE, message);
        values.put(EXCEPTION, exception);
        return values;
    }

    @Override
    public String toString(){
        return String.format("<Event status='%s', code='%s', message='%s', exception='%s'>",
                status, code, message, exception);
    }

    public static JSONArray up_sync(Context context) {
        JSONArray jsonArray = new JSONArray();
        String whereClause = String.format(" %s = %s OR %s = %s ", SYNCHRONIZED, "'null'", SYNCHRONIZED, "''");

        Cursor cursor = null;
        cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        while (cursor.moveToNext()) {
            jsonArray.put(new Event(cursor).getJSONObject());
        }
        cursor.close();
        return jsonArray;
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValues = new ContentValues[length];
        for (int i = 0; i < length; i++) {
            Event obj = new Event(jsonArray.getJSONObject(i));
            contentValues[i] = obj.putContentValues();
        }
        return c.getContentResolver().bulkInsert(URI, contentValues);
    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>(){

        @Override
        public Event createFromParcel(Parcel source) {
            return new Event(source);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    // Static utility methods
    /**
     * Creates a new error level event
     * @param code Type of event
     * @param exception Exception that caused the event
     * @param message The narrative message
     * @return
     */
    public static Event err(Code code, Throwable exception, String message){
        Event event = new Event("","","",Status.ERROR.name(), code.name(), message, String.valueOf(exception));
        return event;
    }

    /**
     * Creates a new error level event
     * @param code Type of event
     * @param exception Exception that caused the event
     * @param message The narrative message
     * @return
     */
    protected static Event err(String code, Throwable exception, String message){
        return err(Code.fromString(code),exception, message);
    }

    /**
     * Creates a new error level event
     * @param code Type of event
     * @param traceElement Throwable that caused the exception event
     * @return
     */
    public static Event err(Code code, StackTraceElement traceElement){
        Event event = new Event("","","",Status.ERROR.name(), code.name(),
                String.valueOf(traceElement),
                traceElement.getClassName() +"("+traceElement.getLineNumber()+"):"+traceElement.getMethodName());
        return event;
    }

    public static List<Event> stacktrace(Code code, Throwable exception, String rootMessage){
        StackTraceElement[] stack = exception.getStackTrace();
        List<Event> events = new ArrayList<>(stack.length+ 1);
        events.add(err(code,exception, rootMessage));
        for(StackTraceElement traceElement: stack) {
            Event event = new Event("", "", "", Status.ERROR.name(), code.name(),
                    rootMessage, String.valueOf(traceElement));
            events.add(event);
        }
        return events;
    }

    /**
     * Creates a new fail level event
     * @param code Type of event
     * @param message The narrative message
     * @return
     */
    public static Event fail(Code code, String message){
        Event event = new Event("","","",Status.FAIL.name(), code.name(),message, NO_EXCEPTION);
        return event;
    }

    /**
     * Creates a new fail level event
     * @param code Type of event
     * @param message The narrative message
     * @return
     */
    protected static Event fail(String code, String message){
        return fail(Code.fromString(code), message);
    }

    /**
     * Creates a new success level event
     * @param code Type of event
     * @param message The narrative message
     * @return
     */
    public static Event success(Code code, String message){
        Event event = new Event("","","",Status.SUCCESS.name(), code.name(),message, NO_EXCEPTION);
        return event;
    }

    /**
     * Creates a new success level event
     * @param code Type of event
     * @param message The narrative message
     * @return
     */
    protected static Event success(String code, String message){
        return success(Code.fromString(code), message);
    }

    public static Event ok(Code code, String message){
        return new Event("","","", Status.OK.name(), code.name(),message, NO_EXCEPTION);
    }
}
