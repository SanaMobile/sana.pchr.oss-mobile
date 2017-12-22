package com.smarking.mhealthsyria.app.api.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.RequestFuture;
import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.Reporter;
import com.smarking.mhealthsyria.app.UpdateManager;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.api.auth.provision.DeviceProvisioner;
import com.smarking.mhealthsyria.app.api.volley.GetByte;
import com.smarking.mhealthsyria.app.api.volley.PostSync;
import com.smarking.mhealthsyria.app.api.volley.VolleySingleton;
import com.smarking.mhealthsyria.app.model.Model;
import com.smarking.mhealthsyria.app.model.event.Event;

import org.apache.http.auth.AuthenticationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-27.
 */
public class SyncService extends Service {
    private static final String TAG = SyncService.class.getSimpleName();
    private static SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        if (sSyncAdapter == null){
            sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return sSyncAdapter.getSyncAdapterBinder();
    }

    public static void requestSync(Context context, Bundle bundle) {
        Account account = null;
        try {
            account = new DeviceProvisioner(context).getAccount();
        } catch (IllegalAccessException e) {
            Reporter.logStack(context, e, TAG + "-requestSync");
            Log.e(TAG, "account is null");
        }
        Log.d(TAG, "requestSyncStart");
        ContentResolver.requestSync(account, ContentProvider.AUTHORITY, bundle);
        Log.d(TAG, "requestSyncEnd");
    }

    public static void requestSyncImmediately(Context context, Bundle bundle) {
        Log.i(TAG, "REQUEST SYNC");
        if (bundle == null) bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        requestSync(context, bundle);
    }

    /**
     *
     * @param context
     * @param device_key Base64-encoded
     * @param bundle
     */
    public static void requestSyncAndProcessImmediately(Context context, String device_key, Bundle bundle){
        Log.i(TAG, "REQUEST SYNC AND PROCESS");
        if (bundle == null) bundle = new Bundle();
        // CAN'T HAVE BYTE ARRAY EXTRA
        bundle.putString(SessionManager.KEY_DEVICE_KEY_USER_KEY, device_key);
        requestSyncImmediately(context, bundle);
    }


    public static class SyncAdapter extends AbstractThreadedSyncAdapter {
        public static final String TAG = SyncAdapter.class.getSimpleName();
        private final AccountManager mAccountManager;

        public static String GET_REMOTE_URL = "/sync?synchronized_after=%s";
        public static String POST_REMOTE_URL = "/sync/";

        public static String SP_LAST_GET_RQ_TIME = "LAST_GET_RQ_TIME";
        public static String SP_LAST_POST_RQ_TIME = "LAST_POST_RQ_TIME";

        public SyncAdapter(Context context, boolean autoInitialize) {
            super(context, autoInitialize);
            mAccountManager = AccountManager.get(context);
        }

        @Override
        public void onPerformSync(Account account, final Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
            Log.e(TAG, "onPerformSync");


            final byte[] key_device_user_key = extras.containsKey(SessionManager.KEY_DEVICE_KEY_USER_KEY)
                    ? Base64.decode(extras.getString(SessionManager.KEY_DEVICE_KEY_USER_KEY), Base64.DEFAULT)
                    : null;

            if (Constants.isNetworkAvailable(getContext())) {
                Log.d(TAG, "network available - sync");
                ArrayList<Task<Void>> tasks = new ArrayList<>();
                tasks.add(Task.callInBackground(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        if (key_device_user_key != null) {
                            Log.d(TAG, "post_process_first");
                            Reporter.ok(getContext(), Event.Code.SYNC, TAG + "-TASK start-onPerformSynch-post_process");
                            new SyncProcessor(getContext()).post_process(key_device_user_key);
                            Reporter.ok(getContext(), Event.Code.SYNC, TAG + "-TASK complete-onPerformSynch-post_process");
                            Log.d(TAG, "post_sync");
                            post_sync();
                            Log.d(TAG, "get_sync");
                            get_sync();
                            Reporter.ok(getContext(), Event.Code.SYNC, TAG + "-TASK start-onPerformSynch-get_process");
                            new SyncProcessor(getContext()).get_process(key_device_user_key);
                            Reporter.ok(getContext(), Event.Code.SYNC, TAG + "-TASK complete-onPerformSynch-get_process");
                            get_images();
                        }
                        return null;
                    }
                }).continueWith(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) throws Exception {
                        if (task.isFaulted()) {
                            Reporter.logStack(getContext(), task.getError(), "SYNCH");
                            Log.e(TAG, "ERROR " + task.getError());
                        }
                        return null;
                    }
                }));
            } else {
                Reporter.ok(getContext(), Event.Code.SYNC, TAG + " onPerformSynch-network unavailable");
                if (key_device_user_key != null) {
                    Reporter.ok(getContext(), Event.Code.SYNC, TAG + " onPerformSynch-start get_process");
                    new SyncProcessor(getContext()).get_process(key_device_user_key);
                    Reporter.ok(getContext(), Event.Code.SYNC, TAG + "onPerformSynch-end get_process");
                }
            }
        }

        private synchronized void get_sync() {
            try {
                File file = new File(getContext().getFilesDir(), Constants.DOWN_SYNC_FILE);

                SharedPreferences pref = getContext().getSharedPreferences(SessionManager.PREF_NAME, SessionManager.PRIVATE_MODE);

                // if file exists, it means it has not been processed yet. so use the date from the previously processed file
                String lastDownloaded = pref.getString(SP_LAST_GET_RQ_TIME, "2000-01-01");
                String date = file.exists() ? pref.getString(SyncProcessor.SP_LAST_PROCESSED_TIME, lastDownloaded) : lastDownloaded;

                String get_url = String.format(Locale.US, GET_REMOTE_URL, date.replace(" ", "%20"));
                Log.d(TAG, "get_sync(): " + date);
                RequestFuture<ByteArrayOutputStream> future = RequestFuture.newFuture();
                future.setRequest(new GetByte(get_url, future, future));
                Log.d(TAG, "GET_URL " + get_url);
                ByteArrayOutputStream response = future.get(60, TimeUnit.SECONDS);

                FileChannel channel = new FileOutputStream(file, false).getChannel();
                byte[] responseBytes = response.toByteArray();
                channel.write(ByteBuffer.wrap(responseBytes));
                Log.d(TAG, "File_Length: " + ((responseBytes != null)?responseBytes.length:0));
                SharedPreferences.Editor editor = pref.edit();
                //TODO DEBUG
                editor.putString(SP_LAST_GET_RQ_TIME, Model.ISO8601_FORMAT_TZ.format(new Date()));
                editor.apply();
            } catch (AuthenticationException | IllegalAccessException | InterruptedException | ExecutionException | TimeoutException | IOException e) {
                Reporter.logStack(getContext(), Event.Code.SYNC, e, TAG + "-get_sync");
                Log.e(TAG, "ERROR: " + e);
                e.printStackTrace();
            }
        }

        private synchronized void get_images() {
            String line;
            String csvSplitBy = ",";
            BufferedReader br = null;

            final File dir = new File(getContext().getFilesDir(), Constants.PICTURE_DIRECTORY);
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    return;
                }
            }

            File inputFile = new File(getContext().getFilesDir(), Constants.DOWN_IMAGES_CSV_FILE);
            StringBuilder sb = new StringBuilder();
            boolean success = false;
            try {
                br = new BufferedReader(new FileReader(inputFile));
                while ((line = br.readLine()) != null) {
                    String[] split = line.split(csvSplitBy);
                    File file = new File(dir, split[1]);
                    boolean result = file.exists() || downloadImage(split[0], file); // don't download again if file already exists
                    if (!result) {
                        sb.append(line.trim()).append("\n");
                    }
                }
                success = true;
            } catch (IOException e) {
                Reporter.logStack(getContext(), Event.Code.SYNC, e, TAG + "-get_images");
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) br.close();
                    if (success) {
                        BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile));
                        bw.write(sb.toString().trim());
                        bw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private synchronized void post_sync() {
            try {
                SharedPreferences pref = getContext().getSharedPreferences(SessionManager.PREF_NAME, SessionManager.PRIVATE_MODE);

                final File file = new File(getContext().getFilesDir(), Constants.UP_SYNC_FILE);
                if (!file.exists()) {
                    return;
                }

                ByteBuffer buf = ByteBuffer.allocateDirect((int) file.length());
                InputStream is = new FileInputStream(file);
                int b;
                while ((b = is.read()) != -1) {
                    buf.put((byte) b);
                }
                byte[] cipherText = buf.array();

                RequestFuture<String> future = RequestFuture.newFuture();

                PostSync request = new PostSync(POST_REMOTE_URL, future, future, cipherText);
                future.setRequest(request);

                String jsonObject = future.get(30, TimeUnit.SECONDS);
                future.isDone();

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(SP_LAST_POST_RQ_TIME, df.format(new Date()));
                editor.apply();
                //TODO GOTTA DELETE FILE
                file.delete();
            } catch (AuthenticationException | IllegalAccessException | InterruptedException | ExecutionException | TimeoutException | IOException e) {
                Reporter.logStack(getContext(), Event.Code.SYNC, e, TAG + "-post_sync");
                Log.e(TAG, "ERROR: " + e);
                e.printStackTrace();
            }
        }
    }

    public static boolean downloadImage(String remote_suffix_url, File file){
        Log.i(TAG, "downloadImage " + file.getPath() + " " + remote_suffix_url);
        String pictureURL = Constants.BASE_API_URL + remote_suffix_url;

        RequestFuture<Bitmap> future = RequestFuture.newFuture();
        ImageRequest request = new ImageRequest(pictureURL, future, 0, 0, null, future);
        future.setRequest(request);
        VolleySingleton.addToRequestQueue(request);

        FileOutputStream fOut = null;
        try {
            Bitmap bmp = future.get(30, TimeUnit.SECONDS);
            fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            return true;
        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            Log.e(TAG, ".downloadImage()-->"+ e);
            e.printStackTrace();
        }finally {
            if(fOut != null) try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
