package com.smarking.mhealthsyria.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;

import java.util.Arrays;
import java.util.List;

/**
 * Created by root on 09/03/15.
 */
public class Constants {
    public static final String BASE_API_URL = "https://hostname";
    public static final boolean ENCRYPTED_SERVER_REQUESTS = true;

    public static final String ROOT_API = "/api";
    public static final String API_VERSION = "/v1";

    public static String getAPIPrefix(){
        return BASE_API_URL + ROOT_API + API_VERSION;
    }

    public static final int VARCHAR_LIMIT = 45;
    // Update url and key
    public static final String URL_UPDATE = "https://hostname/api/v1/client/update/";
    public static final String API_KEY_APP_UPDATE = "";

    // sana.pchr and sana.pchr-test
    public static final String DEVICE_PROVISIONING_PASSWORD = "";

    public static boolean DEBUG = false;
    public static boolean EMULATOR = false;
    public static boolean DEMO = false;

    public static final String PICTURE_DIRECTORY = "pictures";
    public static final String DOWN_SYNC_FILE = "down_sync_encrypted.bytes";
    public static final String UP_SYNC_FILE = "up_sync_encrypted.bytes";
    public static final String DOWN_IMAGES_CSV_FILE = "down_images_unencrypted.list";

    public static final String RECORD_CATEGORY_TEST_UUID = "70668b9f95e24a35867c5514fd424410";
    public static final String RECORD_CATEGORY_FOLLOWUP_UUID = "49cc095c88a54599b28bd478e28b0de4";
    public static final String RECORD_CATEGORY_MEDICATION_UUID = "45e4d86df4784a4f814a64aedde35724";
    public static final String PACKAGE_PRINT_APP = "com.google.android.apps.cloudprint";

    public static final List<String> LIFE_STYLE_RECORDS = Arrays.asList("d313ca3af1914dc3826b218ed37a8980","c049b24747dc421b8c25cd76aaaacced","b83bf21dc5674e20b1b779c1d0a4aef6", "ae7d99f6f1c842a89f59db7a4b798d4d", "39285deb6e324cc5bb81d6feaa257937","1fc0ddb238674e98962387b8daf24f30","1ede9591a3304db7bcd524dda3a99d41");

    public static boolean mainThread(){
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
