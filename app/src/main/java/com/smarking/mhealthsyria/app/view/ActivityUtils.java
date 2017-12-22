package com.smarking.mhealthsyria.app.view;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by winkler.em@gmail.com, on 04/13/2016.
 */
public class ActivityUtils {
    public static final String TAG = ActivityUtils.class.getSimpleName();

    /**
     * Sets the displayed title of an activity to include the value of it's
     * package version code.
     * @param activity
     */
    public static final void setVersionedLabel(Activity activity){
            String labelFormat = "%s-v%d";
            try {
                PackageManager pm = activity.getPackageManager();
                ApplicationInfo ai = activity.getApplicationInfo();
                PackageInfo pi = pm.getPackageInfo(activity.getPackageName(),0);
                activity.setTitle(String.format(labelFormat,ai.loadLabel(pm),pi.versionCode));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
    }
}
