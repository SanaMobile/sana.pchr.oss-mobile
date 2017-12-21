package com.smarking.mhealthsyria.app.view.physician;

import android.app.Dialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Physician;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddNewPhysicianActivity extends ActionBarActivity implements View.OnClickListener{
    private static final String TAG = AddNewPhysicianActivity.class.getSimpleName();
    private Physician mPhysician;
    private Button bTakePhoto;
    private Button bSave;
    private ImageView mImageView;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPin;
    private EditText etPinConfirm;
    private EditText etQuestion;
    private EditText etAnswer;
    private Clinic mClinic;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static boolean imageTaken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_physician_activity);
        bTakePhoto = (Button) findViewById(R.id.bTakePhoto);
        bSave = (Button) findViewById(R.id.bSavePhysician);
        mImageView= (ImageView) findViewById(R.id.ivPhysicianPicture);
        etFirstName =(EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etEmail = (EditText) findViewById(R.id.etEmailAddress);
        etPhone = (EditText) findViewById(R.id.etPhoneNumber);
        etPin = (EditText)findViewById(R.id.etPin);
        etPinConfirm = (EditText) findViewById(R.id.etConfirmPin);
        etQuestion = (EditText) findViewById(R.id.etRecoveryQuestion);
        etAnswer = (EditText) findViewById(R.id.etRecoveryAnswer);

        bTakePhoto.setOnClickListener(this);
        bSave.setOnClickListener(this);
    }


    private Physician createPhysician(){
       return null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bSavePhysician:
                savePhysician();
                break;
            case R.id.bTakePhoto:
                takePhoto();
                break;
        }
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
            imageTaken = true;
        }
    }

    private void savePhysician() {
        String firstName = etFirstName.getText().toString();
        if (firstName == null || firstName.isEmpty()) {
            Toast.makeText(this, getString(R.string.must_first_name), Toast.LENGTH_SHORT).show();
            return;
        }

        String lastName = etLastName.getText().toString();
        if (lastName == null || lastName.isEmpty()) {
            Toast.makeText(this, getString(R.string.must_last_name), Toast.LENGTH_SHORT).show();
            return;
        }

        String email = etEmail.getText().toString();
        String phone = etPhone.getText().toString();

        String pin = etPin.getText().toString();
        String pinConfirm = etPinConfirm.getText().toString();

        if(pin.length() != 4){
            Toast.makeText(this, "PIN must be of length 4", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!pin.equals(pinConfirm)){
            Toast.makeText(this, "Comfirmation does not match pin, please try again", Toast.LENGTH_SHORT).show();
            return;
        }

        String question = etQuestion.getText().toString();
        if (question == null || question.isEmpty()) {
            Toast.makeText(this, "Please input in a question", Toast.LENGTH_SHORT).show();
            return;
        }

        String answer = etAnswer.getText().toString();
        if (answer == null || answer.isEmpty()) {
            Toast.makeText(this, "Please input in an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!imageTaken){
            Toast.makeText(this, "Please Take Photo", Toast.LENGTH_SHORT).show();
        }

        Date date = new Date();


        mPhysician = new Physician(firstName, lastName, "", pin, phone, email, date.toString(), "");

        Bitmap picture = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
        String filename = picture.toString() + date.toString();
        mPhysician.setPicture(filename);
        File sd = Environment.getExternalStorageDirectory();
        File dest = new File(sd, filename);

        try {
            FileOutputStream out = new FileOutputStream(dest);
            picture.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        openChoseClinicsDialog();



    }

    private void openChoseClinicsDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.visit_category_dialog_fragment);


        final Spinner spinner = (Spinner) dialog.findViewById(R.id.sRecordCategory);

        CursorLoader clinicLoader = new CursorLoader(this, Clinic.URI, null, null, null, null);
        android.database.Cursor clinicCursor = clinicLoader.loadInBackground();

        ArrayList<Clinic> clinics = new ArrayList<>();
        while(clinicCursor.moveToNext())
            clinics.add(new Clinic(clinicCursor));
        clinicCursor.close();

        final ClinicAdapter<Clinic> adapter = new ClinicAdapter<>(this,R.layout.list_category_item,clinics);
        spinner.setAdapter(adapter);

        Button bCancel = (Button) dialog.findViewById(R.id.bCancel);
        Button bOk = (Button) dialog.findViewById(R.id.bOk);
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClinic = (Clinic) spinner.getSelectedItem();
                Uri uri_clinic = getApplicationContext().getContentResolver().insert(Clinic.URI, mClinic.putContentValues());
                Cursor clinicCursor = getApplicationContext().getContentResolver().query(uri_clinic, null, null, null, null);
                if(clinicCursor.moveToFirst()) {
                    mClinic = new Clinic(clinicCursor);
                }else
                    Log.e(TAG, "unable to get chosen clinic");
                clinicCursor.close();

                dialog.dismiss();

                Uri uri_physician = getApplication().getContentResolver().insert(Physician.URI,mPhysician.putContentValues());
                Cursor phCursor = getApplicationContext().getContentResolver().query(uri_physician, null, null, null, null);
                if(phCursor.moveToFirst()) {
                    mPhysician = new Physician(phCursor);
                    Log.e(TAG, mPhysician.toString());

                    SessionManager.get(getApplicationContext()).createLoginSession(mPhysician, new byte[32]);
                    Intent intent = new Intent(getApplicationContext(), PhysicianMenuActivity.class);
                    startActivity(intent);
                }
                else
                    Log.e(TAG, "unable to get saved physician");
                phCursor.close();

            }
        });
        dialog.show();
    }

    private class ClinicAdapter<T extends Clinic> extends ArrayAdapter<T> {
        private final int layoutResourceId;

        public ClinicAdapter(Context context, int resource, List<T> items) {
            super(context, resource, items);
            this.layoutResourceId = resource;
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView,ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }

           T clinic = getItem(position);
            if (clinic != null) {
                TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
                tvName.setText(clinic.name);
            }
            return convertView;
        }
    }
}
