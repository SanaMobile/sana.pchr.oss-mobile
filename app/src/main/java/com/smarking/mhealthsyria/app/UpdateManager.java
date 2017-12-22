package com.smarking.mhealthsyria.app;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.toolbox.RequestFuture;
import com.smarking.mhealthsyria.app.api.auth.provision.DeviceProvisioner;
import com.smarking.mhealthsyria.app.api.sync.UpdateInfo;
import com.smarking.mhealthsyria.app.api.sync.UpdateService;
import com.smarking.mhealthsyria.app.api.volley.UpdateRequest;
import com.smarking.mhealthsyria.app.model.event.Event;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.auth.AuthenticationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Provides utilities for initializing update checks and logic for performing
 * any updates. The actual check is passed off to the
 * {@link com.smarking.mhealthsyria.app.api.sync.UpdateService UpdateService}
 */
public class UpdateManager {

    public static final String TAG = UpdateManager.class.getSimpleName();
    public static final int UPDATE_REQUEST = 0x1;
    public static final String UPDATE_URL = "/app/update/";
    public static final String MIMETYPE_PKG = "application/vnd.android.package-archive";
    public static final String ACTION_UPDATE_IN_PROGRESS = "com.smarking.mhealthsyria.app.action.UPDATE_IN_PROGRESS";
    public static final String ACTION_UPDATE_DOWNLOAD_SUCCESS = "com.smarking.mhealthsyria.app.action.UPDATE_DOWNLOAD_SUCCESS";
    public static final String ACTION_UPDATE_DOWNLOAD_FAIL = "com.smarking.mhealthsyria.app.action.UPDATE_DOWNLOAD_FAILED";
    public static final String EXTRA_DOWNLOAD_ID = "extra_download_id";
    public static final String EXTRA_DOWNLOAD_URI = "extra_download_uri";

    public static final String PREF_UPDATE_CHECK = "_update_check";
    public static final String PREF_IS_UPDATING = "_is_updating";

    /** The period between when the update checks will perform */
    public static final long UPDATE_DELTA = 1000*60*60*8;

    public static final void markUpdateCheckTime(Context context){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        long now = new Date().getTime();
        editor.putLong(PREF_UPDATE_CHECK, now);
        editor.apply();
    }

    public static final boolean doUpdateByPeriod(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        long lastUpdate = pref.getLong(PREF_UPDATE_CHECK, 0);
        long now = new Date().getTime();
        return(now - lastUpdate)  > UPDATE_DELTA;
    }

    public static UpdateInfo check(Context context){
        Log.i(TAG, "check(Context)");
        UpdateInfo pkgInfo = new UpdateInfo();
        String pkgName = context.getPackageName();
        try {
            // Current version
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(pkgName, 0);
            int version = pi.versionCode;

            //Device name
            UpdateInfo currentPkg = new UpdateInfo();
            currentPkg.versionCode = pi.versionCode;
            currentPkg.pkg = pkgName;
            currentPkg.device = new DeviceProvisioner(context).getAccount().name;
            Log.i(TAG, "checking update with: device=" + currentPkg.device
                    + ", version=" + currentPkg.versionCode);
            // Check update
            RequestFuture<UpdateInfo> future = RequestFuture.newFuture();
            UpdateRequest request = UpdateRequest.post(UPDATE_URL,
                    future, future, currentPkg);
            future.setRequest(request);
            pkgInfo = future.get(30, TimeUnit.SECONDS);

            // If returned apk version > installed version
            if(pkgInfo.updatable){
                Log.i(TAG, "Update available");
            } else {
                Log.w(TAG, "Application up to date");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Can't check update. Unable to get PackageInfo for "
                    + "'" + pkgName + "'");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return pkgInfo;
    }

    public static Uri update(Context context, UpdateInfo pkg){
        Log.i(TAG, "update(Context, UpdateInfo)");
        long id = pkg.uri.hashCode();
        Uri uri = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                pkg.getFileName()));
        try {
            broadcastInProgress(context, id, uri);
            // Execute request
            RequestFuture<UpdateInfo> future = RequestFuture.newFuture();
            UpdateRequest request = UpdateRequest.get(UPDATE_URL,
                    future, future, pkg);
            future.setRequest(request);
            UpdateInfo pkgUpdate = future.get(3600, TimeUnit.SECONDS);
            broadcastSuccess(context,id,pkgUpdate.uri);
            return pkgUpdate.uri;
        } catch (AuthenticationException e) {
            broadcastError(context, id, e);
        } catch (IllegalAccessException e) {
            broadcastError(context, id, e);
        } catch (InterruptedException e) {
            broadcastError(context, id, e);
        } catch (ExecutionException e) {
            broadcastError(context, id, e);
        } catch (TimeoutException e) {
            broadcastError(context, id, e);
        }
        return Uri.EMPTY;
    }

    public static boolean checkAvailableAndUpdate(Context context){
        Log.i(TAG, "checkAvailableAndUpdate(Context)");
        if(isUpdating(context)){
            // resets the active updating to false prior to check
            setUpdating(context, false);
            return false;
        }
        boolean doUpdate = doUpdateByPeriod(context);
        Log.d(TAG, "ready to check update="+doUpdate);
        if(doUpdateByPeriod(context)){
            Reporter.ok(context, Event.Code.UPDATE, "Periodic server check");
            Intent intent = UpdateService.getUpdateIntent(context);
            context.startService(intent);
            markUpdateCheckTime(context);
        } else {

        }
        return doUpdate;
    }

    public static boolean isUpdating(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_IS_UPDATING, false);
    }

    public static void setUpdating(Context context, boolean updating){
        Log.i(TAG, "setUpdating(Context,boolean) +( "+updating+" )" );
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(PREF_IS_UPDATING, updating);
        editor.commit();

    }

    private static void broadcastError(Context context, long id, Exception e){
        Log.i(TAG, "broadcastError(Context,long,Exception)");
        e.printStackTrace();
        Intent intent = new Intent(UpdateManager.ACTION_UPDATE_DOWNLOAD_FAIL);
        intent.setType(MIMETYPE_PKG).
                putExtra(EXTRA_DOWNLOAD_ID, id);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void broadcastInProgress(Context context, long id, Uri uri){
        Log.i(TAG, "broadcastInProgress(Context,long)");
        Intent intent = new Intent(UpdateManager.ACTION_UPDATE_IN_PROGRESS);
        intent.setType(MIMETYPE_PKG)
                .putExtra(EXTRA_DOWNLOAD_ID, id);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void broadcastSuccess(Context context, long id, Uri uri){
        Log.i(TAG, "broadcastSuccess(Context,long,Uri)");
        Intent intent = new Intent(UpdateManager.ACTION_UPDATE_DOWNLOAD_SUCCESS);
        intent.setType(MIMETYPE_PKG)
                .putExtra(EXTRA_DOWNLOAD_ID, id)
                .putExtra(EXTRA_DOWNLOAD_URI, uri);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static Uri updateDM(Context context, Uri requestUri, Uri destinationUri, String authToken){
        Log.i(TAG, "updateDM(Context, Uri, Uri, String)");
        Log.d(TAG, "Request: " + requestUri.toString());
        Log.d(TAG, "Destination: " + destinationUri.toString());
        try {
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(requestUri);
            // Add the auth headers and DM configs
            request.addRequestHeader("Authorization", authToken)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                    .setDestinationUri(destinationUri)
                    .setMimeType(MIMETYPE_PKG)
                    .setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            // CONSIDER
            request.setTitle(destinationUri.getLastPathSegment())
                    .setDescription("Updating..." + destinationUri.getLastPathSegment())
                    .setVisibleInDownloadsUi(true);
            long id = dm.enqueue(request);
            Intent intent = new Intent(ACTION_UPDATE_IN_PROGRESS);
            intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, id);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Exception: "+e);
            e.printStackTrace();
        }
        return destinationUri;
    }

    public static Uri updateDM(Context context, UpdateInfo pkg){
        File root  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File output = new File(root, "pchr-update-v" +pkg.versionCode+ ".apk");
        Uri uri = Uri.fromFile(output);
        try {
            if(output.exists()) {
                if (isValid(context, output, pkg)) {
                    return uri;
                } else {
                    // if exists but was corrupted delete
                    output.delete();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Uri requestUri = updateRequestUri(pkg);
        String token = getUpdateAuth(pkg);
        return updateDM(context, requestUri, uri, token);
    }


    public static boolean isValid(Context context, File file, UpdateInfo updateInfo) throws FileNotFoundException {
        if(!file.exists()) return false;
        return isValid(context.getContentResolver().openInputStream(Uri.fromFile(file)), updateInfo);
    }

    public static boolean isValid(InputStream in, UpdateInfo updateInfo){
        boolean valid= false;
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] bytes = new byte[8192];
            int byteCount;
            while ((byteCount = in.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }
            byte[] digest = digester.digest();
            String hex = DigestUtils.md5Hex(digest);
            return (hex.compareToIgnoreCase(updateInfo.checkSum) == 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return valid;
    }

    public static void updatePackage(Context context, Uri apkUri){
        Log.i(TAG, "updatePackage(Context,Uri)");
        File f = new File(apkUri.getPath());
        boolean exists = f.exists();
        if(!exists){
            Log.w(TAG, "Update file !exists: " + f.getPath());
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, MIMETYPE_PKG);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void setPeriodicUpdateTrigger(Context context, long interval){
        Log.i(TAG, "setPeriodicUpdateTrigger(Context,long)");
        long triggerAt = getUpdateTime(context);
        Intent intent = UpdateService.getUpdateIntent(
                context.getApplicationContext());
        PendingIntent pi = PendingIntent.getService(context.getApplicationContext(),
                UPDATE_REQUEST,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, interval, pi);
    }

    public static void cancelPeriodicUpdateTrigger(Context context){
        Log.i(TAG, "cancelPeriodicUpdateTrigger(Context)");
        Intent intent = UpdateService.getUpdateIntent(
                context.getApplicationContext());
        PendingIntent pi = PendingIntent.getService(context.getApplicationContext(),
                UPDATE_REQUEST,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static Uri updateRequestUri() {
        throw new UnsupportedOperationException();
    }

    public static Uri updateRequestUri(UpdateInfo info) {
        //return Uri.parse(Constants.getAPIPrefix() + UPDATE_URL);
        return Uri.parse(info.url);
    }

    public static String getUpdateAuth(UpdateInfo info){
        // sana.pchr-web requires "Bearer: " + authToken
        // Work around for heroku dynos requires external server
        // so we use simple api key token
        return "Bearer: " + Constants.API_KEY_APP_UPDATE;
    }

    /**
     * Returns the time periodic updates should be triggered. Default
     * implementation is to set it for 6PM in the local time zone.
     * @param context
     * @return
     */
    public static long getUpdateTime(Context context){
        // TODO This would probably be better if it were configurable by the
        // user-i.e. set in the an app settings
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 6);
        calendar.set(Calendar.AM_PM, Calendar.PM);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();
    }
}
