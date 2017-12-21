package com.smarking.mhealthsyria.app.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.Reporter;
import com.smarking.mhealthsyria.app.api.auth.Security;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.api.server.ServerSearch;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.view.physician.PhysicianMenuActivity;

import org.json.JSONArray;

/**
 * USE THIS FOR RECOVERY INSTEAD!
 * Creates a login dialog.
 */
public class RecoveryDialog extends DialogFragment implements View.OnClickListener{
    public static final String TAG = RecoveryDialog.class.getSimpleName();

    private TextView tvUsername;
    private TextView tvQuestion;
    private EditText etAnswer;
    private Button bLogin;
    private Button bCancel;

    private Physician mPhysician;
    private byte[] mDeviceKey;
    private boolean fromServer = false;

    public RecoveryDialog(){

    }

    public static RecoveryDialog newInstance(Physician physician, boolean dataFromServer) {
        RecoveryDialog fragment = new RecoveryDialog();
        fragment.fromServer = dataFromServer;
        Bundle bundle = new Bundle();
        bundle.putParcelable(Physician.TABLE, physician);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_recovery, container);

        tvUsername = (TextView) view.findViewById(R.id.tvUsername);
        etAnswer = (EditText) view.findViewById(R.id.etAnswer);
        tvQuestion = (TextView) view.findViewById(R.id.tvQuestion);
        bLogin = (Button) view.findViewById(R.id.bLogin);
        bCancel = (Button) view.findViewById(R.id.bClear);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        mPhysician = arguments.getParcelable(Physician.TABLE);

        getDialog().setTitle(getString(R.string.login));

        tvUsername.setText(mPhysician.getFullName());
        tvQuestion.setText(mPhysician.getRecoveryQuestion());
        bLogin.setOnClickListener(this);
        bCancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.bClear:
                dismiss();
                break;
            case R.id.bLogin:
                String answer = etAnswer.getText().toString();
                if (!login(answer)) {
                    etAnswer.setText("");
                    Toast.makeText(getActivity(), getString(R.string.loggin_unsuccessful), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getActivity(), getString(R.string.loggin_successful), Toast.LENGTH_SHORT).show();
                    if(fromServer){
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONArray result = ServerSearch.addPhysician(getActivity(), mPhysician.uuid);
                                    Log.e(TAG, "result: " + result.toString());
                                } catch (Exception e) {
                                    Log.e(TAG, "error: " + e.toString());
                                }
                            }
                        });
                        thread.start();
                    }
                    SessionManager.get(getActivity()).createLoginSession(mPhysician, mDeviceKey);
                    // Record login
                    Reporter.login(RecoveryDialog.super.getActivity());
                    Intent intent = new Intent(RecoveryDialog.super.getActivity(), PhysicianMenuActivity.class);
                    dismiss();
                    startActivity(intent);
                }
                break;
        }
    }

    private boolean login(String answer) {
        answer = Security.normalize(answer);
        byte[] device_key = Security.derivedCredential_decrypt(answer, mPhysician.getRecoveryKey(), mPhysician.getSalt());
        if (device_key == null) {
            return false;
        } else {
            mDeviceKey = device_key;
            return true;
        }
    }

}
