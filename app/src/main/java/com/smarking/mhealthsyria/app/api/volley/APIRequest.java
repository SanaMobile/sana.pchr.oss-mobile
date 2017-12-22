package com.smarking.mhealthsyria.app.api.volley;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.AndroidAuthenticator;
import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.SanaApplication;
import com.smarking.mhealthsyria.app.api.auth.provision.DeviceProvisioner;

import org.apache.http.auth.AuthenticationException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-19.
 */
public abstract class APIRequest<T> extends Request<T>{
    private static final String TAG = APIRequest.class.getSimpleName();
    protected Response.Listener<T> mResponseHandler;

    private String authToken;
    private static AccountManager actManager;
    private static AndroidAuthenticator androidAuthenticator;


    public APIRequest(int method, String url, Response.Listener<T> successListener, Response.ErrorListener errorListener) throws AuthenticationException, IllegalAccessException {
        super(method, Constants.getAPIPrefix() + url, errorListener);
        Context context = SanaApplication.getAppContext();

        if(actManager == null){
            actManager = AccountManager.get(context);
        }

        Account account = new DeviceProvisioner(context).getAccount();
        androidAuthenticator = new AndroidAuthenticator(context, account, context.getString(R.string.account_tokenType));

        this.mResponseHandler = successListener;
        setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(20),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.addToRequestQueue(this);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        String authToken = androidAuthenticator.getAuthToken();
        headers.put("Authorization", "Bearer: " + authToken);
        return headers;
    }

    @Override
    protected void deliverResponse(T response) {
        mResponseHandler.onResponse(response);
    }

}
