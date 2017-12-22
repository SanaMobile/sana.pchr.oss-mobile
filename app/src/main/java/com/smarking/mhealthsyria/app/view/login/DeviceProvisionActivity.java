package com.smarking.mhealthsyria.app.view.login;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;
import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.SanaApplication;
import com.smarking.mhealthsyria.app.api.auth.provision.DeviceProvisioner;
import com.smarking.mhealthsyria.app.api.volley.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-28.
 */
public class DeviceProvisionActivity extends AccountAuthenticatorActivity {
    private final String TAG = this.getClass().getSimpleName();

    private ProgressDialog mDialog;

    private AccountManager mAccountManager;
    private String mAuthTokenType;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        mAccountManager = AccountManager.get(getBaseContext());

        try {
            DeviceProvisioner deviceProvisioner = new DeviceProvisioner(this);
            deviceProvisioner.getAccount();
            setResult(RESULT_OK, new Intent());
            finish();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access, trying to get account");
            String accountName = getIntent().getStringExtra(DeviceProvisioner.ARG_KEY_ACCOUNT_NAME);
            Log.e(TAG, "Account Name: " + accountName);
            mAuthTokenType = getIntent().getStringExtra(DeviceProvisioner.ARG_KEY_AUTH_TYPE);
            if (mAuthTokenType == null)
                mAuthTokenType = getString(R.string.account_tokenType);

            mDialog = new ProgressDialog(this);
            mDialog.setCancelable(false);

            mDialog.setTitle(getString(R.string.check_device_provision));
            mDialog.setMessage(getString(R.string.checking_please_wait));
            mDialog.show();

            makeRequest();
        }

    }

    public void makeRequest() {
        final String accountType = getIntent().getStringExtra(DeviceProvisioner.ARG_KEY_ACCOUNT_TYPE);

        Task.callInBackground(checkRemote).continueWith(new Continuation<Bundle, Void>() {
            @Override
            public Void then(Task<Bundle> task) throws Exception {
                if(task.isFaulted()){
                    mDialog.setMessage(getString(R.string.failure));
                }
                else{
                    mDialog.dismiss();
                    Toast.makeText(DeviceProvisionActivity.this,getString(R.string.success_provision_device), Toast.LENGTH_SHORT).show();
                    Bundle data = task.getResult();
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);

                    final Intent res = new Intent();
                    res.putExtras(data);
                    finishLogin(res);
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private void finishLogin(Intent intent) {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        Account[] MHealthAccounts = mAccountManager.getAccountsByType(getResources().getString(R.string.account_type));

        if(MHealthAccounts.length == 0){
            String auth_token = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

            mAccountManager.addAccountExplicitly(account, "", null);
            mAccountManager.setAuthToken(account, mAuthTokenType, auth_token);
        } else {
            mAccountManager.setPassword(account, "");
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, new Intent());
        finish();
    }

    public static final String RQ_PARAM_PROVISIONING_PASSWORD = "provisioning_password";
    public static final String RQ_PARAM_NAME = "name";
    public static final String RQ_PARAM_DEVICE_MAC = "deviceMAC";
    public final static String RS_UUID = "uuid";
    public final static String RS_TOKEN = "token";

    public static String REMOTE_URL = Constants.getAPIPrefix() + "/auth/provision";


    public static Callable<Bundle> checkRemote = new Callable<Bundle>() {
        public Bundle call() throws Exception {
            final Context context = SanaApplication.getAppContext();

            JSONObject requestBodyParams = new JSONObject();
            requestBodyParams.put(RQ_PARAM_PROVISIONING_PASSWORD, Constants.DEVICE_PROVISIONING_PASSWORD);
            requestBodyParams.put(RQ_PARAM_NAME, android.os.Build.MODEL);

            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            String macAddress = wm.getConnectionInfo().getMacAddress();
            Log.e("MAC ADDRESS", macAddress);
            if(Constants.EMULATOR) {
                requestBodyParams.put(RQ_PARAM_DEVICE_MAC, "00:1C:B3:09:85:15");
            } else {
                requestBodyParams.put(RQ_PARAM_DEVICE_MAC, macAddress);
            }
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, REMOTE_URL, requestBodyParams, future, future);
            VolleySingleton.addToRequestQueue(jsonRequest);

            try {
                JSONObject jsonObject = future.get(30, TimeUnit.SECONDS);
                Bundle data = new Bundle();
                data.putString(AccountManager.KEY_ACCOUNT_NAME, jsonObject.getString(RS_UUID));
                data.putString(AccountManager.KEY_AUTHTOKEN, jsonObject.getString(RS_TOKEN));
                return data;
            }
            catch (JSONException | InterruptedException | ExecutionException | java.util.concurrent.TimeoutException e){
                throw e;
            }
        }
    };
}