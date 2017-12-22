package com.smarking.mhealthsyria.app.api.sync;

import android.accounts.Account;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.AndroidAuthenticator;
import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.SanaApplication;
import com.smarking.mhealthsyria.app.api.auth.provision.DeviceProvisioner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Wraps some basic information about the installed package needed for in app
 * updates.
 */
public class UpdateInfo {

    public static final String VERSION_CODE = "version";
    public static final String DEVICE = "device";
    public static final String PKG = "pkg";
    public static final String CHECKSUM = "checksum";
    public static final String TOKEN = "token";
    public static final String PROVISIONING_PASSWORD = "provisioning_password";
    public static final String URL = "url";

    public int versionCode = 0;
    public String pkg = "";
    public String checkSum = null;
    public String device = "";
    public String token = Constants.API_KEY_APP_UPDATE;
    public String url = Constants.URL_UPDATE;

    public Uri uri = Uri.EMPTY;
    public boolean updatable = false;

    public String getFileName() {
        return uri.getLastPathSegment();
    }

    public boolean isUpdatable(int currentVersion) {
        return (currentVersion < versionCode);
    }

    public File getOutputFile(File root) {
        return new File(Environment.getDownloadCacheDirectory(), getFileName());
    }

    public static UpdateInfo parse(JSONObject json) throws JSONException {
        UpdateInfo info = new UpdateInfo();
        info.pkg = json.getString(PKG);
        info.versionCode = json.optInt(VERSION_CODE,0);
        info.checkSum = json.optString(CHECKSUM, "0");
        info.token = json.optString(TOKEN, Constants.API_KEY_APP_UPDATE);
        info.uri = Uri.parse(info.pkg);
        info.url = json.optString(URL,Constants.URL_UPDATE);
        return info;
    }
}
