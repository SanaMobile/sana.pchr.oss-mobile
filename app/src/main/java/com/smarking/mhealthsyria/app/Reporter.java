package com.smarking.mhealthsyria.app;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.api.auth.provision.DeviceProvisioner;
import com.smarking.mhealthsyria.app.model.event.Event;

import java.util.List;
import java.util.Map;

/**
 * Created by winkler.em@gmail.com, on 04/13/2016.
 */
public final class Reporter {
    public static final String TAG = Reporter.class.getSimpleName();

    /** Device system user */
    public static final String SYSTEM = "SYSTEM";

    protected static final void setUserEnvironment(Context context,
                                                   Event event) throws IllegalAccessException{
        // Probably more useful if we get MAC
        String device = new DeviceProvisioner(context).getAccount().name;
        event.device = device;
        // Get current user
        SessionManager sessionManager = SessionManager.get(context);
        Map<String, String> userData = sessionManager.getCurrentUserData();
        try {
            String user = userData.get(SessionManager.KEY_UUID);
            if (TextUtils.isEmpty(user))
                event.device = SYSTEM;
            event.user = user;
            event.clinic = userData.get(SessionManager.DEVICE_CLINIC_UUID);
        } catch (NullPointerException e) {
            event.device = SYSTEM;
            event.user = "NOUSER";
            event.clinic = "NOCLINIC";
        }

    }

    /**
     * Records a user environment event.
     * @param context
     * @param event
     */
    public static final void record(Context context, Event event){
        try {
            setUserEnvironment(context, event);
            Uri uri = context.getContentResolver().insert(Event.URI, event.putContentValues());
            Log.d(TAG, "Recorded event: " + event.toString());
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Records a user environment exception message.
     * @param context
     * @param code The event type to record.
     * @param exception The exception message to record
     * @param message The message to record.
     */
    public static final void err(Context context, Event.Code code,
                                 Exception exception, String message){
        Event event = Event.err(code, exception, message);
        record(context, event);
    }

    /**
     * Records a user environment event failure message
     * @param context
     * @param code The event type to record.
     * @param message The message to record.
     */
    public static final void fail(Context context, Event.Code code, String message){
        Event event = Event.fail(code, message);
        record(context, event);
    }

    /**
     * Records a user environment event success message.
     * @param context
     * @param code The event type to record.
     * @param message The message to record.
     */
    public static final void success(Context context, Event.Code code, String message) {
        Event event = Event.success(code, message);
        record(context, event);
    }

    /**
     * Records a user environment event progress message.
     * @param context
     * @param code The event type to record.
     * @param message The message to record.
     */
    public static final void ok(Context context, Event.Code code, String message) {
        Event event = Event.ok(code, message);
        record(context, event);
    }

    /**
     * Records a successful user login event.
     *
     * @param context
     */
    public static final void login(Context context){
        success(context, Event.Code.USER_LOGIN, "");
    }

    /**
     * Records a successful user logout event.
     * @param context
     */
    public static final void logout(Context context) {
        success(context, Event.Code.USER_LOGOUT, "");
    }

    /**
     * Records a synchronization start event
     * @param context
     */
    public static final void synchStart(Context context){
        success(context, Event.Code.SYNC_START,"");
    }


    /**
     * Records a synchronization end event
     * @param context
     */
    public static final void synchComplete(Context context, boolean successful) {
        if(successful)
            success(context, Event.Code.SYNC_COMPLETE,"");
        else
            fail(context, Event.Code.SYNC_COMPLETE, "");
    }

    /**
     * Marks some progress in a sync process.
     * @param context
     * @param message
     */
    public static final void synch(Context context, String message) {
        ok(context, Event.Code.SYNC, message);
    }

    public static final void encounter(Context context, String message){
        ok(context, Event.Code.ENCOUNTER, message);
    }

    /**
     * Records an encounter start event
     * @param context
     */
    public static final void encounterStart(Context context) {
        success(context, Event.Code.ENCOUNTER_STARTED, "");
    }

    /**
     * Records an encounter start event
     * @param context
     */
    public static final void encounterEnd(Context context) {
        success(context, Event.Code.ENCOUNTER_COMPLETE, "");
    }

    /**
     * Records an encounter error event
     * @param context
     */
    public static final void encounterError(Context context,
                                            Exception exception, String message) {
        logStack(context, Event.Code.ENCOUNTER, exception, message);
    }

    /**
     * Records an encounter error event
     * @param context
     */
    public static final void logStack(Context context, Event.Code code,
                                      Throwable exception, String message) {
        List<Event> events = Event.stacktrace(code,exception,message);
        for(Event event: events){
            record(context,event);
        }
    }

    public static final void logStack(Context context, Throwable exception, String message) {
        logStack(context, Event.Code.UNHANDLED_EXCEPTION, exception, message);
    }

    public static final void logLastExit(Context context) {
        boolean clean = SessionManager.get(context).isLastExitClean();
        boolean error = SessionManager.get(context).isLastExitError();
        Event event = null;
        if(clean){
            event = Event.success(Event.Code.EXIT_STATUS, "last exit clean: " + clean);
        } else if(error){
            event = Event.fail(Event.Code.EXIT_STATUS, "last exit error: " + error);
        } else {
            event = Event.ok(Event.Code.EXIT_STATUS, "last exit: unknown");
        }
        record(context,event);
    }
}
