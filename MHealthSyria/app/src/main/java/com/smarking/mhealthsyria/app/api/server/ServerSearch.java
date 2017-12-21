package com.smarking.mhealthsyria.app.api.server;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.toolbox.RequestFuture;
import com.smarking.mhealthsyria.app.api.auth.Security;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.api.volley.GetByte;
import com.smarking.mhealthsyria.app.api.volley.GetList;

import org.apache.http.HttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by abe707 on 1/12/16.
 *
 * ahhh!!! exceptions
 */
public class ServerSearch {
    private static final String TAG = ServerSearch.class.getSimpleName();

    private static final String ADD_PATIENT = "/sync/?add_patient=%s&physician=%s";
    private static final String SEARCH_PATIENT  = "/search/patient/?unhcr=%s";
    private static final String SEARCH_PHYSICIAN = "/search/physician/?contact=%s";
    private static final String ADD_PHYSICIAN = "/search/physician/?add_physician=%s";

    private static final String SEARCH_PATIENT_BASE = "/search/patient?";
    private static final String YEAR = "birthYear=%s";
    private static final String FIRSTNAME = "firstName=%s";
    private static final String LASTNAME = "lastName=%s";
    private static final String CITY = "birthCity=%s";
    private static final String GENDER = "gender=%s";


    public static JSONArray findPhysician(Context context, String contact) throws Exception {
        String urlEnd = String.format(SEARCH_PHYSICIAN, contact);
        JSONArray array =  httpGetDecrypted(context, urlEnd);
        JSONArray concatenated = new JSONArray();
        for(int i = 0; i < array.length(); i++){
            JSONArray nested = array.getJSONArray(i);
            for(int j = 0; j < nested.length(); j++){
                JSONObject doc = nested.getJSONObject(j);
                concatenated.put(doc);
            }
        }
        return  concatenated;

    }

    public static JSONArray addPhysician(Context context, String uuid) throws Exception {
        Log.e(TAG, "add doc");
        String urlEnd = String.format(ADD_PHYSICIAN, uuid);
        JSONArray array = httpGetDecrypted(context, urlEnd);
        Log.e(TAG, "array: " + array.toString());
        return array;
    }



    public static JSONArray addPatient(Context context, String patientUUID, String physicianUUID) throws Exception {
        Log.e(TAG, "ADD PATIENT");
        String urlEnd = String.format(ADD_PATIENT, patientUUID, physicianUUID);
        JSONArray result = httpGetEncrypted(context, urlEnd).getJSONArray("Patient");
        Log.e(TAG, "result: " + result.toString());
        return result;
    }

    public static JSONArray findPatient(Context context, String unchr) throws Exception {
        String urlEnd = String.format(SEARCH_PATIENT, unchr);
        JSONObject result =  httpGetEncrypted(context, urlEnd);
        JSONArray patients = result.getJSONArray("Patient");
        return patients;
    }

    public static JSONArray findPatient(Context context, String year, String city, String firstName, String lastName, String gender) throws Exception {

        String url = SEARCH_PATIENT_BASE + String.format(YEAR, year);
        if (!city.isEmpty())
            url = url + "&" + String.format(CITY, city);
        if (!city.isEmpty())
            url = url + "&" + String.format(FIRSTNAME, firstName);
        if (!city.isEmpty())
            url = url + "&" + String.format(LASTNAME, lastName);
        url = url + "&" + String.format(GENDER, gender);
        JSONObject result = httpGetEncrypted(context, url);
        JSONArray patients = result.getJSONArray("Patient");
        return  patients;
    }

    private static JSONObject httpGetEncrypted(Context context, String url) throws Exception {
        RequestFuture<ByteArrayOutputStream> future = RequestFuture.newFuture();
        future.setRequest(new GetByte(url, future, future));

        ByteArrayOutputStream response = future.get(30, TimeUnit.SECONDS);
        byte[] encrypted = response.toByteArray();
        byte[] decryptKey = Base64.decode(context.getSharedPreferences(SessionManager.PREF_NAME, SessionManager.PRIVATE_MODE).getString(SessionManager.KEY_DEVICE_KEY_USER_KEY, null), Base64.DEFAULT);
        byte[] decrypted = Security.decrypt(decryptKey, encrypted, true);
        String result = new String(decrypted);
        Log.e(TAG, "result: " + result);
        return new JSONObject(result);
    }

    private static JSONArray httpGetDecrypted(Context context, String url) throws Exception {
        RequestFuture<ByteArrayOutputStream> future = RequestFuture.newFuture();
        Log.e(TAG, "future:" + future.toString());
        future.setRequest(new GetByte(url, future,future));
        ByteArrayOutputStream response = future.get(30, TimeUnit.SECONDS);
        byte[] decrypted = response.toByteArray();
        Log.e(TAG, "decrypted: " + decrypted.toString());
        String result = new String(decrypted);
        Log.e(TAG, "result: " + result);
        return new JSONArray(result);
    }

    private static JSONArray httpGetListDecrypted(Context context, String url) throws Exception {
        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        Log.e(TAG,"future:" + future.toString());
        future.setRequest(new GetList(url, future, future));
        JSONArray response = future.get(30, TimeUnit.SECONDS);
//        byte[] decrypted = response.toByteArray();
//        Log.e(TAG, "decrypted: " + decrypted.toString());
//        String result = new String(decrypted);
        Log.e(TAG, "result: " + response);
        return response;
    }


}
