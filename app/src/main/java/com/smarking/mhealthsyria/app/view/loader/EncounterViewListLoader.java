package com.smarking.mhealthsyria.app.view.loader;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-25.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.view.viewmodel.EncounterViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EncounterViewListLoader extends AsyncTaskLoader<List<EncounterViewModel>> {
    private static final String TAG = PhysicianListLoader.class.getSimpleName();
    private List<EncounterViewModel> mEncounterViewModels;

    private Visit mVisit;

    public EncounterViewListLoader(Context context, Visit visit) {
        super(context);
        mVisit = visit;

    }

    @Override
    public List<EncounterViewModel> loadInBackground() {
        mEncounterViewModels = new ArrayList<>();

        String whereClause = " ? = '?' ";
        Cursor encounterCursor = getContext().getContentResolver().query(Encounter.URI, null,
                whereClause, new String[]{Encounter.VISIT_UUID, mVisit.uuid}, null);

        while(encounterCursor.moveToNext()){
            Encounter encounter = new Encounter(encounterCursor);

            Cursor recordCursor = getContext().getContentResolver().query(Record.URI, null,
                    whereClause, new String[]{Encounter.VISIT_UUID, mVisit.uuid}, null);
            while(recordCursor.moveToNext()){
                Record record = new Record(recordCursor);
                mEncounterViewModels.add(new EncounterViewModel(getContext(), encounter, record));
            }
            recordCursor.close();

            Cursor testCursor = getContext().getContentResolver().query(Test.URI, null,
                    whereClause, new String[]{Encounter.VISIT_UUID, mVisit.uuid}, null);
            while(testCursor.moveToNext()){
                Test test = new Test(testCursor);
                mEncounterViewModels.add(new EncounterViewModel(getContext(), encounter, test));
            }
            testCursor.close();

        }
        encounterCursor.close();

        Collections.reverse(mEncounterViewModels);

        return mEncounterViewModels;
    }

    @Override
    public void deliverResult(List<EncounterViewModel> encounterViewModelList) {
        if (isReset()) {
            if (encounterViewModelList != null) {
                releaseResources(encounterViewModelList);
                return;
            }
        }

        List<EncounterViewModel> oldEnounterViewModels = mEncounterViewModels;

        if (isStarted()) {
            super.deliverResult(mEncounterViewModels);
        }

        if (oldEnounterViewModels != null && oldEnounterViewModels != mEncounterViewModels) {
            releaseResources(oldEnounterViewModels);
        }
    }


    @Override
    protected void onStartLoading() {
        if (mEncounterViewModels != null) {
            deliverResult(mEncounterViewModels);
        }
        if (takeContentChanged()) {
            forceLoad();
        } else if (mEncounterViewModels == null) {
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
        if (mEncounterViewModels != null) {
            releaseResources(mEncounterViewModels);
            mEncounterViewModels = null;
        }
    }

    @Override
    public void onCanceled(List<EncounterViewModel> encounterViewModelLists) {
        super.onCanceled(encounterViewModelLists);
        releaseResources(encounterViewModelLists);
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
    }

    private void releaseResources(List<EncounterViewModel> encounterViewModels) {
    }
}
