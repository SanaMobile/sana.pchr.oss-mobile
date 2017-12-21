package com.smarking.mhealthsyria.app.view;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.Reporter;
import com.smarking.mhealthsyria.app.UpdateManager;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.api.auth.credentials.CredentialsRequest;
import com.smarking.mhealthsyria.app.api.auth.provision.DeviceProvisioner;
import com.smarking.mhealthsyria.app.api.sync.UpdateService;
import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.view.login.DeviceProvisionActivity;
import com.smarking.mhealthsyria.app.view.login.LoginActivity;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bolts.Continuation;
import bolts.Task;

public class MainActivity extends ActionBarActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog mDialog;
    private SessionManager mSessionManager;

    public static final int REQ_CODE_PROVISION = 1;

    private void InitializeSQLCipher() {
        SQLiteDatabase.loadLibs(this);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeSQLCipher();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.sana_blue)));
        getSupportActionBar().setIcon(getResources().getDrawable(R.mipmap.ic_launcher));

        mSessionManager = SessionManager.get(this);
        Reporter.logLastExit(this);
        SessionManager.get(this).markSessionClean();
        if (savedInstanceState != null) {
            return;
        }

        updateLanguage();

        //Load recommender information
        Task.callInBackground(Recommender.loadFromResources).continueWith(new Continuation<Boolean, Void>() {
            @Override
            public Void then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e("Main", "Could not load data lists from resources: " + task.getError());
                }
                return null;
            }
        }, Task.BACKGROUND_EXECUTOR);
        // Uncomment line below for auto checking update on start up
        //startService(UpdateService.getUpdateIntent(getApplicationContext()));
        // Initialize updater
        // Comment this out when explicitly calling startService above
        // UpdateManager.setPeriodicUpdateTrigger(getApplicationContext(), AlarmManager.INTERVAL_DAY);

        // Provision
        final Intent intent = new Intent(this, DeviceProvisionActivity.class);
        intent.putExtra(DeviceProvisioner.ARG_KEY_ACCOUNT_TYPE, getString(R.string.account_type));
        intent.putExtra(DeviceProvisioner.ARG_KEY_AUTH_TYPE, getString(R.string.account_tokenType));
        startActivityForResult(intent, REQ_CODE_PROVISION);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_PROVISION) {
            if(resultCode == RESULT_OK){
                if (mSessionManager.getClinicUUID() == null) {
                    pickClinic();
                } else {
                    getCredentialList("");
                }
            }
        }
    }

    private void updateLanguage() {
        String lang = mSessionManager.getDeviceClinicLanguage();
        if (lang != null && !lang.isEmpty()) {
            Resources res = getBaseContext().getResources();
            // Change locale settings in the app.
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();

            if (lang.equals("ar")) {
                conf.locale = new Locale("ar");

                Category.language = Category.Language.ARABIC;
            } else {
                conf.locale = new Locale("en");
                Category.language = Category.Language.ENGLISH;
            }
            conf.setLayoutDirection(conf.locale);
            res.updateConfiguration(conf, dm);

            Configuration config = res.getConfiguration();
            Log.e(TAG, config.locale.getLanguage());
        }
    }

    public void showClinicPicker() {
        DatabaseHandler dbHandler = DatabaseHandler.get(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final SQLiteDatabase db = dbHandler.openDatabase();
        Cursor cursor = db.query(Clinic.TABLE, null, null, null, null, null, null);
        final List<Clinic> clinicList = Clinic.getClinics(cursor);
        final List<String> clinicsUUID = new ArrayList<String>();
        final List<String> clinicsName = new ArrayList<String>();
        final List<String> clinicsLanguage = new ArrayList<>();
        final ArrayList<String> uuid = new ArrayList<>(1);

        dbHandler.closeDatabase();
        if(Constants.DEMO){
            clinicList.clear();
            clinicList.addAll(Clinic.getDemoClinics());
        }

        for (Clinic clinic : clinicList) {
            clinicsUUID.add(clinic.uuid);
            clinicsName.add(clinic.name);
            clinicsLanguage.add(clinic.language);
        }

        builder.setTitle(R.string.credential_choose_clinic);
        builder.setSingleChoiceItems(clinicsName.toArray(new CharSequence[clinicsName.size()]), -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int i) {
                ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int which = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                if (which > -1) {
                    SessionManager mSessionManager = SessionManager.get(getParent());
                    mSessionManager.setClinicUUID(clinicsUUID.get(which));
                    mSessionManager.setClinicLanguage(clinicsLanguage.get(which));
                    updateLanguage();
                    dialog.dismiss();

                    getCredentialList(clinicsUUID.get(which));
                } else {
                    dialog.cancel();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


    }

    public void pickClinic() {
        mDialog = new ProgressDialog(this);
        mDialog.setCancelable(false);
        mDialog.show();
        mDialog.setTitle(getString(R.string.credentialing));
        mDialog.setMessage(getString(R.string.downloading_clinic_wait));

        Task.callInBackground(CredentialsRequest.checkRemoteClinic).continueWith(new Continuation<Boolean, Void>() {
            @Override
            public Void then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    mDialog.setMessage(getString(R.string.failure));
                } else {
                    if (task.getResult()) {
                        Toast.makeText(MainActivity.this, getString(R.string.ok), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.connectionNone), Toast.LENGTH_SHORT).show();
                    }
                    mDialog.dismiss();
                    showClinicPicker();
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);

    }

    public void getCredentialList(String deviceUUID) {
        String url = CredentialsRequest.REMOTE_URL;

        mDialog = new ProgressDialog(this);
        mDialog.setCancelable(false);
        mDialog.show();
        mDialog.setTitle(getString(R.string.credential_list));
        mDialog.setMessage(getString(R.string.downloading_cred_list_wait));

        if (!deviceUUID.equals(""))
            url += CredentialsRequest.SET_CLINIC_STR + deviceUUID;

        Task.callInBackground(new CredentialsRequest(url).checkRemote).continueWith(new Continuation<Boolean, Void>() {
            @Override
            public Void then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    mDialog.setMessage(getString(R.string.failure));
                } else {
                    if(task.getResult()){
                        Toast.makeText(MainActivity.this, getString(R.string.got_cred_list), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this, getString(R.string.not_update_cred_no_internet), Toast.LENGTH_SHORT).show();
                    }

                    mDialog.dismiss();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }


}
