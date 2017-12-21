package com.smarking.mhealthsyria.app.view.login;

import android.app.Dialog;
import android.support.v4.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.server.ServerSearch;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.view.custom.LoginCellAdapter;
import com.smarking.mhealthsyria.app.view.loader.PhysicianListLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-22.
 */
public class RecoveryFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Physician>>, AdapterView.OnItemClickListener{
    private static final String TAG = RecoveryFragment.class.getSimpleName();

    private GridView mGridView;
    private LoginCellAdapter mLoginCellAdapter;
    private boolean dataFromServer = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recovery, container, false);
        mGridView = (GridView) view.findViewById(R.id.gridview);
        mGridView.setOnItemClickListener(this);
        mLoginCellAdapter = new LoginCellAdapter(getActivity());
        mGridView.setAdapter(mLoginCellAdapter);

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Physician physician = mLoginCellAdapter.getItem(i);
        RecoveryDialog rd = RecoveryDialog.newInstance(physician, dataFromServer);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("recover_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        rd.show(ft, "recover_dialog");
    }

    @Override
    public Loader<List<Physician>> onCreateLoader(int id, Bundle args) {
        return new PhysicianListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Physician>> loader, List<Physician> data) {
        mLoginCellAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Physician>> loader) {
        mLoginCellAdapter.setData(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_physician_login, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mSearchLocal:
                openSearchDialog();
                break;
            case R.id.mSearchServer:
                openServerSearchDialog();
                break;
            case R.id.mScan:
                LoginActivity parent = (LoginActivity) getActivity();
                parent.scanQRCode();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void openServerSearchDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.fragment_physician_search);
        dialog.setTitle(R.string.physician_server_search_title);

        final LinearLayout llLastName = (LinearLayout) dialog.findViewById(R.id.llLastName);
        final LinearLayout llFirstName = (LinearLayout) dialog.findViewById(R.id.llFirstName);

        llLastName.setVisibility(View.GONE);
        llFirstName.setVisibility(View.GONE);

        final EditText etPhoneNumber = (EditText) dialog.findViewById(R.id.etPhoneNumber);
        final EditText etEmailAddress = (EditText) dialog.findViewById(R.id.etEmailAddress);

        Button bCancel = (Button) dialog.findViewById(R.id.bCancel);
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button bSearch = (Button) dialog.findViewById(R.id.bSearch);
        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = etEmailAddress.getText().toString();
                final String phone = etPhoneNumber.getText().toString();


                final List<Physician> matches = new ArrayList<Physician>();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!email.isEmpty()) {
                            try {
                                JSONArray emailMatches = ServerSearch.findPhysician(getActivity(), email);
                                Log.e(TAG, emailMatches.toString());
                                for (int i = 0; i < emailMatches.length(); i++) {
                                    JSONObject physicianDoc = emailMatches.getJSONObject(i);
                                    Physician physician = new Physician(physicianDoc, false);
                                    getActivity().getContentResolver().insert(Physician.URI, physician.putContentValues());
                                    matches.add(physician);
                                }

                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                        if (!phone.isEmpty()) {
                            try {
                                JSONArray phoneMatches = ServerSearch.findPhysician(getActivity(), phone);
                                Log.e(TAG, phoneMatches.toString());
                                for (int i = 0; i < phoneMatches.length(); i++) {
                                    JSONObject physicianDoc = phoneMatches.getJSONObject(i);
                                    Physician physician = new Physician(physicianDoc, false);
                                    getActivity().getContentResolver().insert(Physician.URI, physician.putContentValues());
                                    matches.add(physician);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        }

                        if (email.isEmpty() && phone.isEmpty()) {
                            String message = getString(R.string.physician_server_search_error);
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        } else {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLoginCellAdapter.setData(matches);
                                    dataFromServer = true;
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
                thread.start();
            }
        });

        dialog.show();
    }

    private void openSearchDialog(){
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.fragment_physician_search);

        final EditText etLastName = (EditText) dialog.findViewById(R.id.etLastName);
        final EditText etFirstName = (EditText) dialog.findViewById(R.id.etFirstName);
        final EditText etPhoneNumber = (EditText)dialog.findViewById(R.id.etPhoneNumber);
        final EditText etEmailAddress = (EditText) dialog.findViewById(R.id.etEmailAddress);

        Spinner spTypeInit = (Spinner) dialog.findViewById(R.id.spType);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.physician_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTypeInit.setAdapter(adapter);

        final Spinner spType = spTypeInit;
        Button bCancel = (Button) dialog.findViewById(R.id.bCancel);
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button bSearch = (Button) dialog.findViewById(R.id.bSearch);
        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String whereFormat = " %s = '%s' ";
                String whereClause = "";

                String lastName = etLastName.getText().toString();
                if (!lastName.isEmpty())
                    whereClause += String.format(whereFormat, Physician.LASTNAME, lastName);

                String firstName = etFirstName.getText().toString();
                if (!firstName.isEmpty()) {
                    if (whereClause.isEmpty())
                        whereClause += String.format(whereFormat, Physician.FIRSTNAME, firstName);
                    else
                        whereClause += "OR" + String.format(whereFormat, Physician.FIRSTNAME, firstName);
                }

                String email = etEmailAddress.getText().toString();
                if (!email.isEmpty()) {
                    if (whereClause.isEmpty())
                        whereClause += String.format(whereFormat, Physician.EMAIL, email);
                    else
                        whereClause += "OR" + String.format(whereFormat, Physician.EMAIL, email);
                }

                String phone = etPhoneNumber.getText().toString();
                if (!phone.isEmpty()) {
                    if (whereClause.isEmpty())
                        whereClause += String.format(whereFormat, Physician.PHONE, phone);
                    else
                        whereClause += "OR" + String.format(whereFormat, Physician.PHONE, phone);
                }

                int type = spType.getSelectedItemPosition();
                String typeString = type == 0 ? "D" : "N";
                if (!typeString.isEmpty()) {
                    if (whereClause.isEmpty())
                        whereClause += String.format(whereFormat, Physician.TYPE, typeString);
                    else
                        whereClause += "OR" + String.format(whereFormat, Physician.TYPE, typeString);
                }

                Log.e(TAG, "WHERE " + whereClause);
                Cursor physicianCursor = getActivity().getContentResolver().query(Physician.URI, null, whereClause, null, null);
                Log.e(TAG, "Data: " +  physicianCursor.getCount());
                List<Physician> matches = new ArrayList<Physician>();

                while(physicianCursor.moveToNext())
                    matches.add(new Physician(physicianCursor));

                mLoginCellAdapter.setData(matches);
                dataFromServer = false;
                physicianCursor.close();

                dialog.dismiss();
            }
        });

        dialog.show();
    }
}