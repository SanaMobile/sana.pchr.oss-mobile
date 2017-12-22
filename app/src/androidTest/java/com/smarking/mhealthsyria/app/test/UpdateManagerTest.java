package com.smarking.mhealthsyria.app.test;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.test.AndroidTestCase;
import android.util.Log;

import com.smarking.mhealthsyria.app.UpdateManager;
import com.smarking.mhealthsyria.app.api.sync.UpdateInfo;

import java.io.File;

/**
 *
 */
public class UpdateManagerTest extends AndroidTestCase{
    public static final String TAG = UpdateManagerTest.class.getSimpleName();

    /** Set to current available version on server */
    public static final int AVAILABLE_VERSION = 9;
    Context context;
    Uri apkUri = Uri.EMPTY;
    BroadcastReceiver mReceiver;
    boolean downloaded = false;

    public void setUp(){
        context = getContext();
        registerReceiver();
    }

    @Override
    public void tearDown(){
        unregisterReceiver();
    }

    protected final void registerReceiver(){
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive(Context context, Intent intent)");
                String action = intent.getAction();
                long id = 0L;
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
                    Log.d(TAG, "Update download complete: " + id);
                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(id);
                    Cursor cursor = downloadManager.query(query);
                    if (!cursor.moveToFirst()) {
                        Log.e(TAG, "Empty row");
                        return;
                    }
                    /*
                    int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                        Log.w(TAG, "Download Failed");
                        return;
                    }
                    */
                    int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                    Uri uri = Uri.parse(cursor.getString(uriIndex));
                    File file = new File(uri.getPath());
                    Log.i(TAG, "Download file=" + file.getPath());
                    if(file.exists()){
                        Log.i(TAG, "Download size="+file.length());
                    }
                    synchronized(UpdateManagerTest.this){
                        setApkUri(uri);
                        setDownloaded(true);
                        UpdateManagerTest.this.notify();
                    }
                } else{
                    Log.w(TAG, "Unknown action received: " + action);
                }
            }
        };
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
            filter.addDataType(UpdateManager.MIMETYPE_PKG);
            IntentFilter filter2 = new IntentFilter();
            filter2.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            filter2.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
            context.registerReceiver(mReceiver, filter);
            context.registerReceiver(mReceiver,filter2);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
    }

    protected final void unregisterReceiver(){
        context.unregisterReceiver(mReceiver);
    }

    public void testCheck(){
        UpdateInfo pkg = UpdateManager.check(context);
        assertNotNull(pkg);
        assertEquals(AVAILABLE_VERSION, pkg.versionCode);
    }

    public final void setApkUri(Uri uri){
        apkUri = uri;
    }

    public final Uri getApkUri(){
        return apkUri;
    }

    public final boolean getDownloaded(){
        return downloaded;
    }

    public final void setDownloaded(boolean downloaded){
        this.downloaded = downloaded;
    }

    public void testGetUpdateDM(){
        UpdateInfo pkg = UpdateManager.check(context);
        Uri requestUri = UpdateManager.updateRequestUri(pkg);
        String token = UpdateManager.getUpdateAuth(pkg);

        File root  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File output = new File(root, "pchr-update-v" +pkg.versionCode+ ".apk");
        if(output.exists()) output.delete();

        Uri target = Uri.fromFile(output);
        Uri uriOut = UpdateManager.updateDM(context, requestUri, target, token);
        assertEquals(target, uriOut);
        assertNotSame(Uri.EMPTY, uriOut);
        setApkUri(uriOut);
        long ms = 0;
        synchronized(this) {
            while (true) {
                try {
                    this.wait(1000);
                    if(getDownloaded()) break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        assertEquals(getApkUri(), uriOut);
    }
}
