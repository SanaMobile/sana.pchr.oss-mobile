package com.smarking.mhealthsyria.app.api.volley;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.auth.AuthenticationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-07.
 */
public class GetByte extends APIRequest<ByteArrayOutputStream> {
    public GetByte(String url, Response.Listener<ByteArrayOutputStream> successListener, Response.ErrorListener errorListener) throws AuthenticationException, IllegalAccessException {
        super(Method.GET, url, successListener, errorListener);
    }

    @Override
    protected Response<ByteArrayOutputStream> parseNetworkResponse(NetworkResponse networkResponse) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            buffer.write(networkResponse.data);
            return Response.success(buffer, HttpHeaderParser.parseCacheHeaders(networkResponse));
        }
        catch(IOException e){
            return Response.error(new ParseError(e));
        }
    }

}