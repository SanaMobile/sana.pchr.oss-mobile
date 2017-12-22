package com.smarking.mhealthsyria.app.api.volley;

import android.app.Application;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.api.sync.UpdateInfo;
import com.smarking.mhealthsyria.app.model.Device;

import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Posts the current installed app version and device information to the server.
 * Response will be the current available version and url path of for download.
 */
public class UpdateRequest extends APIRequest<UpdateInfo> {
    /**
     * Charset for request.
     */
    private static final String PROTOCOL_CHARSET = "utf-8";
    /**
     * Content type for request.
     */
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private final Map<String, String> mParams = new HashMap<>();
    private final UpdateInfo mUpdate;

    public UpdateRequest(int method, String url,
                         Response.Listener<UpdateInfo> successListener,
                         Response.ErrorListener errorListener,
                         UpdateInfo updateInfo) throws AuthenticationException, IllegalAccessException {
        super(method, url, successListener, errorListener);
        mParams.put(UpdateInfo.VERSION_CODE, String.valueOf(updateInfo.versionCode));
        mParams.put(UpdateInfo.DEVICE, updateInfo.device);
        mParams.put(UpdateInfo.PROVISIONING_PASSWORD, Constants.DEVICE_PROVISIONING_PASSWORD);
        mUpdate = new UpdateInfo();
        mUpdate.device = updateInfo.device;
        mUpdate.pkg = updateInfo.pkg;
        mUpdate.uri = updateInfo.uri;
        mUpdate.versionCode = updateInfo.versionCode;
    }

    @Override
    public String getPostBodyContentType() {
        return getBodyContentType();
    }

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getPostBody() {
        return getBody();
    }

    @Override
    public byte[] getBody() {
        JSONObject json = new JSONObject(mParams);
        String mRequestBody = json.toString();
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
            return null;
        }
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    @Override
    protected Response<UpdateInfo> parseNetworkResponse(NetworkResponse networkResponse) {
        if (getMethod() == Method.GET) {
            return handleGet(networkResponse);
        } else {
            return handlePost(networkResponse);
        }
    }

    private Response<UpdateInfo> handleGet(NetworkResponse networkResponse) {
        try {
            File output = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    mUpdate.getFileName());
            // Write to file
            FileChannel channel = new FileOutputStream(output, false).getChannel();
            channel.write(ByteBuffer.wrap(networkResponse.data));
            channel.close();
            mUpdate.uri = Uri.fromFile(output);
            return Response.success(mUpdate, HttpHeaderParser.parseCacheHeaders(networkResponse));
        } catch (FileNotFoundException e) {
            return Response.error(new ParseError(e));
        } catch (IOException e) {
            return Response.error(new ParseError(e));
        }
    }

    private Response<UpdateInfo> handlePost(NetworkResponse networkResponse) {
        try {
            String jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
            UpdateInfo info = UpdateInfo.parse(new JSONObject(jsonString));
            info.updatable = info.versionCode > mUpdate.versionCode;
            return Response.success(info, HttpHeaderParser.parseCacheHeaders(networkResponse));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    public static UpdateRequest post(String updateUrl,
                                     Response.Listener<UpdateInfo> successListener,
                                     Response.ErrorListener errorListener,
                                     UpdateInfo info) throws AuthenticationException, IllegalAccessException {
        UpdateRequest request = new UpdateRequest(Method.POST, updateUrl,
                successListener, errorListener, info);
        return request;
    }

    public static UpdateRequest get(String updateUrl,
                                    Response.Listener<UpdateInfo> successListener,
                                    Response.ErrorListener errorListener,
                                    UpdateInfo info) throws AuthenticationException, IllegalAccessException {
        UpdateRequest request = new UpdateRequest(Method.GET, updateUrl,
                successListener, errorListener, info);
        return request;
    }
}
