package com.smarking.mhealthsyria.app.api.auth.credentials;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.RequestFuture;
import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.SanaApplication;
import com.smarking.mhealthsyria.app.api.sync.SyncService;
import com.smarking.mhealthsyria.app.api.volley.GetList;
import com.smarking.mhealthsyria.app.api.volley.VolleySingleton;
import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Physician;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import bolts.Task;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-19.
 */
public class CredentialsRequest {
    public static final String TAG = CredentialsRequest.class.getSimpleName();

    public static String REMOTE_URL = "/auth/credentials";
    public static String GET_CLINIC_STR = "?clinic=1";
    public static String SET_CLINIC_STR = "?setclinic=";

    public String callURL;

    public CredentialsRequest(String call_URL) {
        callURL = call_URL;
    }

    public static Callable<Boolean> checkRemoteClinic = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            final Context context = SanaApplication.getAppContext();
            boolean result = false;

            if(!Constants.isNetworkAvailable(context)){
                Log.e(TAG, "network not available");
                return false;
            }

            RequestFuture<JSONArray> future = RequestFuture.newFuture();
            future.setRequest(new GetList(REMOTE_URL + GET_CLINIC_STR, future, future));

            try {
                JSONArray jsonArray = future.get(30, TimeUnit.SECONDS);
                context.getContentResolver().delete(Clinic.URI, null, null);

                for (int i = 0, len = jsonArray.length(); i < len; i++) {
                    Clinic clinic = new Clinic(jsonArray.getJSONObject(i));
                    context.getContentResolver().insert(Clinic.URI, clinic.putContentValues());
                }
                result = true;
            } catch (JSONException | ExecutionException | InterruptedException | TimeoutException e) {
                Log.e(TAG, "ClinicListError: " + e);
            }

            return result;
        }
    };

    public Callable<Boolean> checkRemote = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            final Context context = SanaApplication.getAppContext();
            boolean result = false;

            if (!Constants.isNetworkAvailable(context)) {
                Log.e(TAG, "network not available");
                return false;
            }

            RequestFuture<JSONArray> future = RequestFuture.newFuture();
            future.setRequest(new GetList(callURL, future, future));

            try {
                JSONArray jsonArray = future.get(30, TimeUnit.SECONDS);
                context.getContentResolver().delete(Physician.URI, null, null);
                ArrayList<Task<Void>> tasks = new ArrayList<>();

                for(int i = 0, len = jsonArray.length(); i<len; i++){
                    tasks.add(CredentialsRequest.saveWithPictures(jsonArray.getJSONObject(i)));
                }
                Task.whenAll(tasks);
                result = true;
            } catch (FileNotFoundException | JSONException | ExecutionException | InterruptedException | TimeoutException e) {
                Log.e(TAG, "ERROR: " + e);
            }

            return result;
        }
    };

    public static Task<Void> saveWithPictures(final JSONObject jsonObject) throws FileNotFoundException, JSONException {
        final Task<Void>.TaskCompletionSource tcs = Task.create();

        final Context context = SanaApplication.getAppContext();
        final String fileName = jsonObject.getString(Physician.UUID) + ".jpg";

        String suffixUrl = jsonObject.getString(Physician.PICTURE);

        final File dir = new File(context.getFilesDir(), Constants.PICTURE_DIRECTORY);
        if (!dir.exists()) {
            if(!dir.mkdir()){
                throw new FileNotFoundException(dir.getPath());
            }
        }

        final File file = new File(dir, fileName);
        boolean requestMade = suffixUrl != null && !suffixUrl.isEmpty() && !suffixUrl.equals("null");

        if(requestMade && SyncService.downloadImage(suffixUrl, file)){
            jsonObject.put(Physician.PICTURE, fileName);
        }
        else{
            jsonObject.put(Physician.PICTURE, "");
        }


        Physician physician = new Physician(jsonObject,false);
        context.getContentResolver().insert(Physician.URI, physician.putContentValues());

        return tcs.getTask();
    }



}
