package com.smarking.mhealthsyria.app.view.patient.search;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.api.server.ServerSearch;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.view.patient.history.PatientHistoryActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-18.
 */
public class PatientListFragment  extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener{

    private static final String TAG = PatientListFragment.class.getSimpleName();
    private static final int SEARCHED_INFO = 0;
    public static final String SELECTION = "SELECTION";
    private GridView mGridView;
    private PatientListCellAdapter mPatientListCellAdapter;
    private TextView tvNoResults;
    private boolean listFromSearchServer = false;
    private Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_list, container, false);
        tvNoResults = (TextView) view.findViewById(R.id.tvNoResults);
        mGridView = (GridView) view.findViewById(R.id.gridview);
        mGridView.setNumColumns(1);
        mGridView.setOnItemClickListener(this);
        mPatientListCellAdapter = new PatientListCellAdapter(getActivity());
        mGridView.setAdapter(mPatientListCellAdapter);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try{
            Bundle args = getArguments();
            Set<String> keys = args.keySet();
            Log.e(TAG, "num keys: " + keys.size());

            if (!keys.isEmpty())
                getLoaderManager().initLoader(SEARCHED_INFO, getArguments(), this);
        }catch(NullPointerException e){
            Log.e(TAG, e.toString());
        }


    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.e(TAG, "ITEM CLICKED: " + i);
        final Patient patient = mPatientListCellAdapter.getItem(i);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.patient_selected_dialog_title);
        String message = patient.getFullName() + " " + patient.getBirthYear()+" " + patient.getBirthCity();
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                Thread thread  = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(listFromSearchServer){
                            String physician_uuid = SessionManager.get(getActivity()).getCurrentUserData().get(SessionManager.KEY_UUID);
                            try {
                                JSONArray resultAdd = ServerSearch.addPatient(getActivity(), patient.uuid, physician_uuid);
                                for(int i = 0; i < resultAdd.length(); i++){
                                    JSONObject patientDoc = resultAdd.getJSONObject(i);
                                    Patient pat = new Patient(patientDoc);
                                    Uri uri_patient = getActivity().getContentResolver().insert(Patient.URI, pat.putContentValues());
                                    Cursor pCursor = getActivity().getContentResolver().query(uri_patient, null, null, null, null);

                                    if(pCursor.moveToNext())
                                        pat = new Patient(pCursor);

                                    pCursor.close();

                                    Intent intent = new Intent(getActivity(), PatientHistoryActivity.class);
                                    intent.putExtra(Patient.TABLE, pat);
                                    startActivity(intent);
                                    getActivity().finish();
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Intent intent = new Intent(getActivity(), PatientHistoryActivity.class);
                        intent.putExtra(Patient.TABLE, patient);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
                thread.start();

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    public void setFromServer(boolean fromServer){
        this.listFromSearchServer = fromServer;
    }

    public void setPatients(Context context , List<Patient> patients){
        if(mPatientListCellAdapter == null) {
            mPatientListCellAdapter = new PatientListCellAdapter(context);
            mGridView.setAdapter(mPatientListCellAdapter);
        }
        if (!patients.isEmpty()) {
            tvNoResults.setVisibility(View.GONE);
            mPatientListCellAdapter.setPatients(patients);
        }else{
            tvNoResults.setVisibility(View.VISIBLE);
            tvNoResults.setText(R.string.no_patients_found);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id){
            case SEARCHED_INFO:{
                Log.e(TAG, args.getString(SELECTION));
                // TODO Do we really need to limit to 10
                //String orderBy = String.format(" %s ASC LIMIT 10 COLLATE NOCASE ", Patient.LASTNAME);
                String orderBy = String.format(" %s COLLATE NOCASE ASC", Patient.LASTNAME);
                return new CursorLoader(getActivity(), Patient.URI, null, args.getString(SELECTION), null, orderBy);
            }
            default:
                return null;
        }


    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try {
            if (data.getCount() == 0) {
                tvNoResults.setVisibility(View.VISIBLE);
                tvNoResults.setText(R.string.no_patients_found);
                Log.e(TAG, "NOTHING");
            } else {
                tvNoResults.setVisibility(View.GONE);
                mPatientListCellAdapter.setData(data);
            }
        } catch (NullPointerException e) {
            tvNoResults.setVisibility(View.VISIBLE);
            tvNoResults.setText(R.string.no_patients_found);
            Log.e(TAG, "NOTHING");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPatientListCellAdapter.setData(null);
    }


    public static class PatientListCellAdapter extends ArrayAdapter<Patient> {
        private final LayoutInflater mInflater;


        public PatientListCellAdapter(Context context) {
            super(context, R.layout.login_grid_cell, R.id.tvName);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        public void setData(Cursor patients){
            clear();
            if(patients != null) {
                while (patients.moveToNext()) {
                    add(new Patient(patients));
                }
            }
        }

        public void setPatients(List<Patient> patients){
            clear();
            for(Patient patient: patients)
                add(patient);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if(convertView == null) {
                view = mInflater.inflate(R.layout.login_grid_cell, parent, false);
            }
            else{
                view = convertView;
            }

            Patient patient = getItem(position);

            TextView tvName = (TextView) view.findViewById(R.id.tvName);
            TextView tvBirthCity = (TextView) view.findViewById(R.id.tvInfo1);
            TextView tvBirthDate = (TextView) view.findViewById(R.id.tvInfo2);



            tvName.setText(patient.getFullName());
            tvBirthCity.setText(patient.getBirthCity());
            tvBirthDate.setText(patient.getBirthYear());

            return view;
        }
    }

}
