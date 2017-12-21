package com.smarking.mhealthsyria.app.api.volley;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-07.
 */
public class PostSync extends APIRequest<String> {

    private final byte[] mRequestBody;

    public PostSync(String url, Response.Listener<String> successListener, Response.ErrorListener errorListener, byte[] requestBody) throws AuthenticationException, IllegalAccessException {
        super(Method.POST, url, successListener, errorListener);
        this.mRequestBody = requestBody;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mRequestBody;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse networkResponse) {
        try {
            String jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
            return Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(networkResponse));
        }
        catch (UnsupportedEncodingException e) {
            Log.e("PostSync", "network response: " + networkResponse.toString());
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mResponseHandler.onResponse(response);
    }


}