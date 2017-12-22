package com.smarking.mhealthsyria.app.view.custom;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.model.category.VisitCategory;

import java.util.ArrayList;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-25.
 */
public class VisitCategoryDialogFragment extends DialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    public static final String TAG = VisitCategoryDialogFragment.class.getSimpleName();
    public static final String KEY_INTENT = "KEY_FRAGMENT";
    public static final String KEY_URI = "KEY_URI";

    private Intent mIntent;
    private Uri mLoaderUri;

    private CategoryAdapter<VisitCategory> mCategoryAdapter;
    private Spinner sCategory;

    public static VisitCategoryDialogFragment newInstance(Uri uri, Intent intent) {
        VisitCategoryDialogFragment frag = new VisitCategoryDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(VisitCategoryDialogFragment.KEY_INTENT, intent);
        bundle.putParcelable(VisitCategoryDialogFragment.KEY_URI, uri);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIntent = getArguments().getParcelable(KEY_INTENT);
        mLoaderUri = getArguments().getParcelable(VisitCategoryDialogFragment.KEY_URI);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.visit_category_dialog_fragment);
        dialog.show();

        sCategory = (Spinner) dialog.findViewById(R.id.sRecordCategory);
        mCategoryAdapter = new CategoryAdapter<>(getActivity(), R.layout.list_category_item, new ArrayList<VisitCategory>());
        sCategory.setAdapter(mCategoryAdapter);

        Button bCancel = (Button) dialog.findViewById(R.id.bCancel);
        Button bOk = (Button) dialog.findViewById(R.id.bOk);

        bCancel.setOnClickListener(this);
        bOk.setOnClickListener(this);

        return dialog;
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), mLoaderUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        mCategoryAdapter.clear();
        while(data.moveToNext()){
            mCategoryAdapter.add(new VisitCategory(data));
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mCategoryAdapter.clear();
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bOk:{
                int position = sCategory.getSelectedItemPosition();
                VisitCategory visitCategory = mCategoryAdapter.getItem(position);
                mIntent.putExtra(VisitCategory.TABLE, visitCategory);
                startActivity(mIntent);
                dismiss();
                break;
            }
            case R.id.bCancel: {
                dismiss();
                break;
            }
        }
    }
}