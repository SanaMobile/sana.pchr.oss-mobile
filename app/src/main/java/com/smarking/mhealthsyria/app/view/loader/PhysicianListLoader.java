package com.smarking.mhealthsyria.app.view.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.Physician;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-24.
 */
public class PhysicianListLoader extends AsyncTaskLoader<List<Physician>> {
    private static final String TAG = PhysicianListLoader.class.getSimpleName();
    private List<Physician> mPhysicians;
    private DatabaseHandler dbHandler;

    public PhysicianListLoader(Context context) {
        super(context);
        dbHandler = DatabaseHandler.get(context);
    }

    @Override
    public List<Physician> loadInBackground() {
        ContentResolver resolver = getContext().getContentResolver();
        List<Physician> list = new ArrayList<>();

        Cursor cursor = resolver.query(Physician.URI, null, null, null, null);
        Physician p;
        while (cursor.moveToNext()) {
            p = new Physician(cursor);
            if (p.getKey() != null && !p.getKey().isEmpty() && !p.getKey().equals("null"))
                list.add(new Physician(cursor));
        }

        cursor.close();
        return list;

    }

    @Override
    public void deliverResult(List<Physician> physicians) {
        if (isReset()) {
            if (physicians != null) {
                releaseResources(physicians);
                return;
            }
        }

        List<Physician> oldPhysicians = mPhysicians;
        mPhysicians = physicians;

        if (isStarted()) {
            super.deliverResult(physicians);
        }

        if (oldPhysicians != null && oldPhysicians != physicians) {
            releaseResources(oldPhysicians);
        }
    }


    @Override
    protected void onStartLoading() {
        if (mPhysicians != null) {
            deliverResult(mPhysicians);
        }
        if (takeContentChanged()) {
            forceLoad();
        } else if (mPhysicians == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if (mPhysicians != null) {
            releaseResources(mPhysicians);
            mPhysicians = null;
        }
    }

    @Override
    public void onCanceled(List<Physician> physicians) {
        super.onCanceled(physicians);
        releaseResources(physicians);
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
    }

    private void releaseResources(List<Physician> physicians) {
    }
}
