package com.smarking.mhealthsyria.app.api.auth;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.smarking.mhealthsyria.app.Reporter;
import com.smarking.mhealthsyria.app.api.sync.SyncProcessor;
import com.smarking.mhealthsyria.app.api.sync.SyncService;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Device;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.model.event.Event;
import com.smarking.mhealthsyria.app.view.login.LoginActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import bolts.Task;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-28.
 */
public class SessionManager {
    private static final String TAG = SessionManager.class.getSimpleName();
    SharedPreferences mPref;
    Context mContext;

    public static final int PRIVATE_MODE = 0;

    public static final String PREF_NAME = "com.smarking.mhealthsyria.app.login";

    public static final String KEY_UUID = Physician.UUID;
    public static final String KEY_FULLNAME = "FULLNAME";
    public static final String KEY_PICTURE = Physician.PICTURE;
    public static final String KEY_DEVICE_KEY_USER_KEY = Physician.DEVICE_KEY_USER_KEY;
    public static final String DEVICE_CLINIC_UUID = Device.CLINICUUID;
    public static final String DEVICE_CLINIC_LANGUAGE = Clinic.LANGUAGE;

    /** Marks the exit state of the app. Must be one of {@link #EXIT_CLEAN}
     *  or {@link #EXIT_ERROR }
     */
    public static final String KEY_EXIT = "_exit";
    public static final int EXIT_CLEAN = 0;
    public static final int EXIT_ERROR = 1;
    public static final int EXIT_INDETERMINATE = -1;

    private SessionManager(Context context){
        this.mContext = context;
        mPref = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    private static SessionManager instance = null;

    public static SessionManager get(Context context){
        if(instance == null){
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public String getClinicUUID() {
        return mPref.getString(DEVICE_CLINIC_UUID, null);
    }

    public String getDeviceClinicLanguage() {
        return mPref.getString(DEVICE_CLINIC_LANGUAGE, null);
    }

    public void setClinicLanguage(final String clinicLanguage) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(DEVICE_CLINIC_LANGUAGE, clinicLanguage);
        editor.commit();
    }

    public void setClinicUUID(final String clinicUUID) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(DEVICE_CLINIC_UUID, clinicUUID);
        editor.commit();
    }

    public void createLoginSession(Physician physician, final byte[] deviceKey){
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(KEY_UUID, physician.uuid);
        editor.putString(KEY_FULLNAME, physician.getFullName());
        editor.putString(KEY_PICTURE, physician.getPicture());
        editor.putString(KEY_DEVICE_KEY_USER_KEY, Base64.encodeToString(deviceKey, Base64.DEFAULT));

        boolean i = editor.commit();

        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Log.d(TAG, "Get data store start_process");
                Reporter.ok(mContext, Event.Code.SYNC_START, "TASK start-login synch");
                SyncService.requestSyncAndProcessImmediately(mContext, Base64.encodeToString(deviceKey, Base64.DEFAULT), null);
//                new SyncProcessor(mContext).get_process(deviceKey);
                Reporter.ok(mContext, Event.Code.SYNC_START, "TASK complete-login synch");
                Log.d(TAG, "Get data store end_process");
                return true;
            }
        });
    }

    public Map<String, String> getCurrentUserData(){
        if(!isLoggedIn()){
            return null;
        }

        Map<String, String> map = new HashMap<>();
        map.put(KEY_UUID, mPref.getString(KEY_UUID, null));
        map.put(KEY_FULLNAME, mPref.getString(KEY_FULLNAME, null));
        map.put(KEY_PICTURE, mPref.getString(KEY_PICTURE, null));
        map.put(KEY_DEVICE_KEY_USER_KEY, mPref.getString(KEY_DEVICE_KEY_USER_KEY, null));
        map.put(DEVICE_CLINIC_UUID, mPref.getString(DEVICE_CLINIC_UUID, null));
        map.put(DEVICE_CLINIC_LANGUAGE, mPref.getString(DEVICE_CLINIC_LANGUAGE, null));
        return map;
    }

    public void checkLogin(){
        if(!isLoggedIn()){
            Intent intent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(intent);
        }
    }

    public boolean isLoggedIn(){
        return mPref.contains(KEY_UUID) && mPref.contains(KEY_FULLNAME);
    }

    public void logoutUser(boolean loginAfter){
        if(isLoggedIn()) {
            final byte[] deviceKeyUserKey = Base64.decode(mPref.getString(KEY_DEVICE_KEY_USER_KEY, null), Base64.DEFAULT);

            Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Log.e(TAG, "Post data store start");
                    Reporter.ok(mContext, Event.Code.SYNC_START, "TASK start-logout post");
                    new SyncProcessor(mContext).post_process(deviceKeyUserKey);
                    Reporter.ok(mContext, Event.Code.SYNC_START, "TASK complete-logout post");
                    Log.e(TAG, "Post data store end");
                    return true;
                }
            });

            onDestroySession();
            Reporter.logout(mContext);
        }

        if(loginAfter) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    public void onDestroySession(){
        SharedPreferences.Editor editor = mPref.edit();
        editor.remove(KEY_DEVICE_KEY_USER_KEY)
                .remove(KEY_FULLNAME)
                .remove(KEY_PICTURE)
                .remove(KEY_UUID)
                .commit();
    }

    public void markSessionClean(){
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(KEY_EXIT, EXIT_CLEAN);
        editor.commit();
    }


    public void markSessionError(){
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(KEY_EXIT, EXIT_ERROR);
        editor.commit();
    }

    public boolean isLastExitClean(){
        return (lastExit() == EXIT_CLEAN);
    }

    public boolean isLastExitError(){
        return (lastExit() == EXIT_ERROR);
    }

    public int lastExit(){
        return mPref.getInt(KEY_EXIT, EXIT_INDETERMINATE);
    }

    /**
     * Attempts to shut down the current context if it is a
     * {@link android.app.Service} or {@link android.app.Activity} and throws
     * an {@link java.lang.Error}. {@link android.app.Activity} classes
     * finished with this method will have their result set to
     * {@link android.app.Activity#RESULT_CANCELED} prior to calling
     * {@link Activity#finish()} This should be used only
     * explicitly needing to generate an uncaught error when testing.
     *
     * @param context
     * @throws Error always
     */
    public static void barf(Context context){
        Log.i(TAG, "barf(Context)");
        if(context instanceof Activity) {
            Activity activity = (Activity)context;
            activity.setResult(Activity.RESULT_CANCELED);
            activity.finish();
        } else if(context instanceof Service) {
            ((Service)context).stopSelf();
        }
        throw new Error("BARF");
    }
}
