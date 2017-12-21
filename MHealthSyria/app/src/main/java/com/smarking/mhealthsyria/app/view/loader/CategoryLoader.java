package com.smarking.mhealthsyria.app.view.loader;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.model.category.TestCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abe707 on 12/18/15.
 */
public class CategoryLoader extends AsyncTaskLoader<List<Category>>{
    private static final String TAG = CategoryLoader.class.getSimpleName();
    private List<Category> mCategories;


    public CategoryLoader(Context context) {
        super(context);

    }

    @Override
    public List<Category> loadInBackground() {
        mCategories = new ArrayList<>();

        Cursor testCategoryCursor = getContext().getContentResolver().query(TestCategory.URI, null,null, null, null);
        while(testCategoryCursor.moveToNext()){
            TestCategory testCategory = new TestCategory(testCategoryCursor);
            mCategories.add(testCategory);
        }
        testCategoryCursor.close();

        Cursor recordCategoryCursor = getContext().getContentResolver().query(RecordCategory.URI, null, "priority < 100", null, RecordCategory.PRIORITY);
        while(recordCategoryCursor.moveToNext()){
            RecordCategory recordCategory = new RecordCategory(recordCategoryCursor);
            mCategories.add(recordCategory);
        }
        recordCategoryCursor.close();

        return mCategories;
    }

    @Override
    public void deliverResult(List<Category> categories) {
        if (isReset()) {
            if (categories != null) {
                releaseResources(categories);
                return;
            }
        }

        List<Category> oldCategories = mCategories;

        if (isStarted()) {
            super.deliverResult(mCategories);
        }

        if (oldCategories != null && oldCategories != mCategories) {
            releaseResources(oldCategories);
        }
    }


    @Override
    protected void onStartLoading() {
        if (mCategories != null) {
            deliverResult(mCategories);
        }
        if (takeContentChanged()) {
            forceLoad();
        } else if (mCategories == null) {
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
        if (mCategories!= null) {
            releaseResources(mCategories);
            mCategories = null;
        }
    }

    @Override
    public void onCanceled(List<Category> categories) {
        super.onCanceled(categories);
        releaseResources(categories);
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
    }

    private void releaseResources(List<Category> categories) {
    }
}
