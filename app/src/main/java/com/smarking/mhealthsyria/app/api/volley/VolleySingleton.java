package com.smarking.mhealthsyria.app.api.volley;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.smarking.mhealthsyria.app.SanaApplication;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-21.
 */
public class VolleySingleton {
    public static String TAG = VolleySingleton.class.getSimpleName();

    private static VolleySingleton sInstance = null;

    private static RequestQueue sRequestQueue;
    private static ImageLoader sImageLoader;

    private VolleySingleton(){
        sRequestQueue = getRequestQueue();
        sImageLoader = getImageLoader();
    }

    public static synchronized VolleySingleton getInstance() {
        if (sInstance == null) {
            sInstance = new VolleySingleton();
        }
        return sInstance;
    }

    public static RequestQueue getRequestQueue(){
        if(sRequestQueue == null){
            sRequestQueue = Volley.newRequestQueue(SanaApplication.getAppContext());
        }
        return sRequestQueue;
    }

    public static ImageLoader getImageLoader(){
        if(sImageLoader == null){
            sImageLoader = new ImageLoader(getRequestQueue(),
                    new ImageLoader.ImageCache() {
                        private final LruCache<String, Bitmap>
                                cache = new LruCache<String, Bitmap>(20);

                        @Override
                        public Bitmap getBitmap(String url) {
                            return cache.get(url);
                        }

                        @Override
                        public void putBitmap(String url, Bitmap bitmap) {
                            cache.put(url, bitmap);
                        }
                    });
        }
        return sImageLoader;
    }

    public static <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }



}
