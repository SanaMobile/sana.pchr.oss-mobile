package com.smarking.mhealthsyria.app.api.sync;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.smarking.mhealthsyria.app.UpdateManager;

public class UpdateReceiver extends BroadcastReceiver {
    private static final String TAG = UpdateReceiver.class.getSimpleName();
    static long updateId = 0l;

    public UpdateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive(Context, Intent)");
        long id = 0L;
        String action = intent.getAction();
        Log.i(TAG, "...action="+action);
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action) ||
                DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
            id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            Log.d(TAG, "Update download complete: " + id);
            Log.d(TAG, "id match: " + (updateId == id));
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);
            if (!cursor.moveToFirst()) {
                UpdateManager.setUpdating(context,false);
                Log.e(TAG, "Empty row");
                return;
            }
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(statusIndex);
            Log.d(TAG, "Download manager status="+status);
            if (status != DownloadManager.STATUS_SUCCESSFUL) {
                Log.w(TAG, "Download Failed");
                UpdateManager.setUpdating(context, false);
                return;
            }
            int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            Uri uri = Uri.parse(cursor.getString(uriIndex));
            UpdateManager.updatePackage(context, uri);
            UpdateManager.setUpdating(context,false);
        } else if (UpdateManager.ACTION_UPDATE_IN_PROGRESS.equals(action)) {
            id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            Log.d(TAG, "Update in progress: " + id);
            updateId = id;
        } else if (UpdateManager.ACTION_UPDATE_DOWNLOAD_FAIL.equals(action)) {
            id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            Log.e(TAG, "Updating failed " + id);
            updateId = id;
        } else if (UpdateManager.ACTION_UPDATE_DOWNLOAD_SUCCESS.equals(action)) {
            id = intent.getLongExtra(UpdateManager.EXTRA_DOWNLOAD_ID, 0L);
            Uri uri = intent.getParcelableExtra(UpdateManager.EXTRA_DOWNLOAD_URI);
            Log.i(TAG, "Updating: " + id);
            updateId = id;
            try {
                updatePackage(context, uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updatePackage(Context context, Uri apkUri) {
        Log.i(TAG, "updatePackage(Context,Uri");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, UpdateManager.MIMETYPE_PKG);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
