package com.smarking.mhealthsyria.app.view.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.UpdateManager;
import com.smarking.mhealthsyria.app.api.auth.Security;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.api.server.ServerSearch;
import com.smarking.mhealthsyria.app.api.sync.SyncService;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.view.ActivityUtils;
import com.smarking.mhealthsyria.app.view.custom.QRScanner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-03.
 */
public class LoginActivity extends ActionBarActivity  implements LoginPINFragment.LoginPINListener{
    public static String TAG = LoginActivity.class.getSimpleName();

    public static final int REQ_CODE_QR_SCAN = 2;
    private static final String NO_CODE_AVAILABLE = "NO CODE AVAILABLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityUtils.setVersionedLabel(this);
        SyncService.requestSyncImmediately(this, new Bundle());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.sana_blue)));

        showHome();

    }

    public void showHome() {
        getSupportActionBar().hide();
        Fragment homeFragment = new HomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, homeFragment);
        transaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showHome();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void scanQRCode(){
        Intent qrScanIntent = new Intent(this, QRScanner.class);
        startActivityForResult(qrScanIntent, REQ_CODE_QR_SCAN);
        getSupportActionBar().show();
    }

    public void loginWithPassword(){
        Fragment loginFragment = new RecoveryFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, loginFragment);
        transaction.commit();
        getSupportActionBar().show();
    }

    public void QRCodeInvalidResultDialog(String msg){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        scanQRCode();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        loginWithPassword();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setMessage(msg.trim() + " " + getString(R.string.scan_again))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();

        if(msg.equals(NO_CODE_AVAILABLE))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();

    }

    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "result: " + resultCode);
        if(requestCode == REQ_CODE_QR_SCAN){
            if(resultCode == RESULT_OK){
                String qrString = data.getStringExtra(QRScanner.QR_DATA);
                byte[] qrBytes = Base64.decode(qrString.getBytes(), Base64.DEFAULT);

                if(qrBytes.length == 0 || qrBytes.length > 49 || qrBytes[0] != 0){
                    QRCodeInvalidResultDialog(getString(R.string.scan_unsuccessful));
                    return;
                }
                final String uuid = Security.printHex(qrBytes, 1, 17);
                final byte[] userKeyBytes = new byte[32];
                try {

                    System.arraycopy(qrBytes, 17, userKeyBytes, 0, 32);
                } catch (ArrayIndexOutOfBoundsException e) {
                    QRCodeInvalidResultDialog(getString(R.string.scan_unsuccessful));
                    return;
                }

                final Bundle bundle = new Bundle();

                Task.callInBackground(new Callable<Physician>() {
                    @Override
                    public Physician call() throws Exception {
                        Physician physician = Physician.getPhysician(LoginActivity.this, uuid);
                        if(physician == null){
                            throw new NoSuchElementException();
                        }
                        byte[] key = Security.decrypt(userKeyBytes, Base64.decode(physician.getKey(), Base64.DEFAULT), true);
                        bundle.putByteArray(Physician.DEVICE_KEY_USER_KEY, key);
                        return physician;
                    }
                }).continueWith(new Continuation<Physician, Void>() {
                    @Override
                    public Void then(Task<Physician> task) throws Exception {
                        if(task.isFaulted()) {
                            Log.e(TAG, "USER NOT FOUND");
                            Task.callInBackground(new Callable<Physician>() {
                                @Override
                                public Physician call() throws Exception {
                                    JSONArray result = ServerSearch.addPhysician(getApplicationContext(), uuid.toLowerCase());
                                    Log.e(TAG, "result: " + result.toString());
                                    JSONObject physicianDoc = result.getJSONObject(0);
                                    return new Physician(physicianDoc, false);
                                }
                            }).continueWith(new Continuation<Physician, Void>() {
                                @Override
                                public Void then(Task<Physician> task) throws Exception {
                                    if (task.isFaulted()) {
                                        Log.e(TAG, "USER NOT FOUND ON SERVER");
                                        QRCodeInvalidResultDialog(getString(R.string.user_not_found));
                                        return null;
                                    } else {
                                        Physician physician = task.getResult();
                                        getContentResolver().insert(Physician.URI, physician.putContentValues());
                                        RecoveryDialog rd = RecoveryDialog.newInstance(physician, false);
                                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                        Fragment prev = getSupportFragmentManager().findFragmentByTag("recover_dialog");
                                        if (prev != null) {
                                            ft.remove(prev);
                                        }
                                        ft.addToBackStack(null);

                                        rd.show(ft, "recover_dialog");
                                    }
                                    return null;
                                }
                            }, Task.UI_THREAD_EXECUTOR);


                        } else {
                            bundle.putParcelable(Physician.TABLE, task.getResult());
                            bundle.putByteArray(Physician.USER_KEY_STRING, userKeyBytes);

                            LoginPINFragment loginPINFragment = new LoginPINFragment();
                            loginPINFragment.setListener(LoginActivity.this);
                            loginPINFragment.setArguments(bundle);
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.container, loginPINFragment);
                            transaction.commit();
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
            } else if(resultCode == RESULT_CANCELED){
                QRCodeInvalidResultDialog(NO_CODE_AVAILABLE);
            } else
                QRCodeInvalidResultDialog(getString(R.string.scan_unsuccessful));

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SessionManager.get(this).logoutUser(false);
    }

    @Override
    public void LoginPinFragmentCancel() {
        scanQRCode();
    }


    @Override
    protected void onResume() {
        super.onResume();
        UpdateManager.checkAvailableAndUpdate(this);
    }

}
