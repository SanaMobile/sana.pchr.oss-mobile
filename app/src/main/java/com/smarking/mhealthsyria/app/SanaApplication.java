package com.smarking.mhealthsyria.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.smarking.mhealthsyria.app.api.auth.SessionManager;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-21.
 */
public class SanaApplication extends Application {
    public static final String TAG = SanaApplication.class.getSimpleName();

    private static Application sInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                handleUncaughtException(thread, ex);
            }
        });
    }

    public static Application getInstance(){
        return sInstance;
    }

    public static Context getAppContext(){
        return sInstance.getApplicationContext();
    }

    public final void handleUncaughtException (Thread thread, Throwable e) {
        Log.e(TAG, "handleUncaughtException(Thread,Throwable)");
        Log.d(TAG, "thread: " + thread.toString());
        e.printStackTrace();
        Reporter.logStack(this, e, "APP CRASH-" + thread.getName());
        SessionManager.get(this).markSessionError();
        SessionManager.get(this).logoutUser(false);
        restart();
    }

    private final void restart(){
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(getPackageName());
        startActivity(intent);
        System.exit(1);
    }
}
