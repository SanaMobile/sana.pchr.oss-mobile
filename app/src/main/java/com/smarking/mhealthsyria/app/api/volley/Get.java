package com.smarking.mhealthsyria.app.api.volley;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-21.
 */
public class Get extends APIRequest<JSONObject> {
    public Get(String url, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) throws AuthenticationException, IllegalAccessException {
        super(Method.GET, url, successListener, errorListener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse networkResponse) {
        try {
            String jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(networkResponse));
        }
        catch(JSONException e){
            return Response.error(new ParseError(e));
        }
        catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
}
