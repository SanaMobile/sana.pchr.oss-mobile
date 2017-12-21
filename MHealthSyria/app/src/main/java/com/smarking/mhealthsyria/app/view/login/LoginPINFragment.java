package com.smarking.mhealthsyria.app.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.ChecksumException;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.Reporter;
import com.smarking.mhealthsyria.app.api.auth.PasswordHasher;
import com.smarking.mhealthsyria.app.api.auth.Security;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.view.physician.PhysicianMenuActivity;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-29.
 */
public class LoginPINFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = LoginPINFragment.class.getSimpleName();

    private TextView tvUsername;
    private EditText etPIN;
    private Button bForgotPIN;
    private Button bLogin;
    private Button bCancel;

    private LoginPINListener mListener;

    private Physician mPhysician;
    private byte[] mUserKey;
    private byte[] mDeviceKey;

    public LoginPINFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mPhysician = bundle.getParcelable(Physician.TABLE);
        mUserKey = bundle.getByteArray(Physician.USER_KEY_STRING);
        mDeviceKey = bundle.getByteArray(Physician.DEVICE_KEY_USER_KEY);
    }


    public void setListener(LoginPINListener listener){
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_login, container, false);

        tvUsername = (TextView) view.findViewById(R.id.tvUsername);
        etPIN = (EditText) view.findViewById(R.id.etPIN);
        bForgotPIN = (Button) view.findViewById(R.id.bForgotPIN);
        bLogin = (Button) view.findViewById(R.id.bLogin);
        bCancel = (Button) view.findViewById(R.id.bClear);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvUsername.setText(mPhysician.getFullName());

        bForgotPIN.setOnClickListener(this);
        bLogin.setOnClickListener(this);
        bCancel.setOnClickListener(this);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        etPIN.setText("");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private int pinTriesLeft = Integer.MAX_VALUE;

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.bClear:
                if(mListener != null){
                    mListener.LoginPinFragmentCancel();
                }
                else{
                    getActivity().finish();
                }
                break;
            case R.id.bForgotPIN:
                RecoveryDialog rd = RecoveryDialog.newInstance(mPhysician,false);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("recover_dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                rd.show(ft, "recover_dialog");

                break;
            case R.id.bLogin:
                if(validatePIN(etPIN.getText().toString())){
                    SessionManager.get(getActivity()).createLoginSession(mPhysician, mDeviceKey);
                    Reporter.login(LoginPINFragment.super.getActivity());
                    Intent intent = new Intent(LoginPINFragment.super.getActivity(), PhysicianMenuActivity.class);
                    startActivity(intent);
                }
                else{
                    if(--pinTriesLeft == 0){
                        if(mListener != null){
                            mListener.LoginPinFragmentCancel();
                        }
                        else{
                            getActivity().finish();
                        }
                    }
                    else {
                        etPIN.setText("");
                        Toast.makeText(getActivity(), String.format(getString(R.string.invalid_pin_format), pinTriesLeft), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private boolean validatePIN(String pin){
        try {
            byte[] decrypted_hashedPINUserKey = Security.decrypt(mUserKey, Base64.decode(mPhysician.getHashedPINUserKey(), Base64.DEFAULT), true);
            String decrypted = new String(decrypted_hashedPINUserKey);
            Log.e(TAG, "pin: " + pin);
            Log.e(TAG, "dec:" + decrypted);
            return PasswordHasher.checkPassword(pin, decrypted);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException | ChecksumException e) {
            return false;
        }
    }

    public interface LoginPINListener {
        void LoginPinFragmentCancel();
    }

}
