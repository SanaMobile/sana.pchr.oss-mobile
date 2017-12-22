package com.smarking.mhealthsyria.app.api.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;
import android.util.Log;

import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.Reporter;
import com.smarking.mhealthsyria.app.api.auth.Security;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Clinic_Physician;
import com.smarking.mhealthsyria.app.model.Device;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Patient_Physician;
import com.smarking.mhealthsyria.app.model.category.DoseUnitCategory;
import com.smarking.mhealthsyria.app.model.category.EncounterCategory;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.category.IntervalUnitCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationGroupCategory;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.category.TestCategory;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.model.category.VisitCategory;
import com.smarking.mhealthsyria.app.model.event.Event;
import com.smarking.mhealthsyria.app.view.physician.PhysicianMenuActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-07.
 */
public class SyncProcessor {
    public static String TAG = SyncProcessor.class.getSimpleName();
    public static String KEY_DEVICE_KEY = "device_key";

    private Context mContext;

    public static final String SP_LAST_PROCESSED_TIME = "SP_LAST_PROCESSED_TIME";

    public SyncProcessor(Context context){
        mContext = context;
    }

    public synchronized void get_process(byte[] decrypt_key){
        Log.e(TAG, "get_process");
        final File file = new File(mContext.getFilesDir(), Constants.DOWN_SYNC_FILE);
        if(!file.exists()){
            Log.e(TAG, "file doesn't exist" + file.getAbsolutePath());
            return;
        }
        try {
            ByteBuffer buf = ByteBuffer.allocateDirect((int) file.length());
            InputStream is = new FileInputStream(file);
            int b;
            while ((b = is.read()) != -1) {
                buf.put((byte) b);
            }

            byte[] cipherText = buf.array();
            byte[] decrypted = Constants.ENCRYPTED_SERVER_REQUESTS ? Security.decrypt(decrypt_key, cipherText, true) : cipherText;
            //TODO UNCOMMENT DECRYPTION ON LIVE SERVER. LOCALLY REMOVED ENCRYPTION!

            String jsonResponseDecrypted = new String(decrypted);
            Log.e(TAG + "j", jsonResponseDecrypted);
            JSONObject jsonObject = new JSONObject(jsonResponseDecrypted);
            int numrecs = 0;
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                numrecs = numrecs + 1;
                String key = keys.next();
                Log.e(TAG, key);
                JSONArray value = jsonObject.getJSONArray(key);

                if (key.equals(EncounterCategory.TABLE)) {
                    EncounterCategory.save(mContext, value);
                } else if (key.equals(Encounter.TABLE)) {
                    Encounter.save(mContext, value);
                } else if (key.equals(Patient.TABLE)) {
                    Patient.save(mContext, value);
                } else if (key.equals(Physician.TABLE)) {
                    Physician.save(mContext, value);
                } else if (key.equals(Record.TABLE)) {
                    Record.save(mContext, value);
                } else if (key.equals(RecordCategory.TABLE)) {
                    RecordCategory.save(mContext, value);
                } else if (key.equals(Test.TABLE)) {
                    Test.save(mContext, value);
                } else if (key.equals(TestCategory.TABLE)) {
                    TestCategory.save(mContext, value);
                } else if (key.equals(Visit.TABLE)) {
                    Visit.save(mContext, value);
                } else if (key.equals(VisitCategory.TABLE)) {
                    VisitCategory.save(mContext, value);
                } else if(key.equals(Clinic.TABLE)){
                    Clinic.save(mContext, value);
                } else if(key.equals(Device.TABLE)){
                    Device.save(mContext, value);
                } else if(key.equals(Medication.TABLE)){
                    Medication.save(mContext, value);
                } else if(key.equals(Clinic_Physician.TABLE)){
                    Clinic_Physician.save(mContext, value);
                } else if(key.equals(Patient_Physician.TABLE)){
                    Patient_Physician.save(mContext, value);
                } else if(key.equals(MedicationCategory.TABLE)){
                    MedicationCategory.save(mContext, value);
                } else if(key.equals(MedicationGroupCategory.TABLE)){
                    MedicationGroupCategory.save(mContext, value);
                } else if (key.equals(DoseUnitCategory.TABLE)) {
                    DoseUnitCategory.save(mContext, value);
                } else if (key.equals(IntervalUnitCategory.TABLE)) {
                    IntervalUnitCategory.save(mContext, value);
                } else if (key.equals(Event.TABLE)){
                    Event.save(mContext, value);
                }

            }
            file.delete();
            try {
                if (PhysicianMenuActivity.hasLoaded == 0) {
                    PhysicianMenuActivity.mDialog.dismiss();
                }
            } catch (Exception e) {
                Log.e(TAG, "We got an error: " + e);
                Reporter.logStack(mContext, Event.Code.SYNC, e, "get_process()");
            }

            SharedPreferences pref = mContext.getSharedPreferences(SessionManager.PREF_NAME, SessionManager.PRIVATE_MODE);

            String lastDownloaded = pref.getString(SyncService.SyncAdapter.SP_LAST_GET_RQ_TIME, null);
            if (lastDownloaded != null) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(SP_LAST_PROCESSED_TIME, lastDownloaded);
                editor.apply();
            }

        } catch (Exception e){
//        } catch (IOException | NoSuchPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | ChecksumException | InvalidKeyException | JSONException e) {
            Log.e(TAG, "PROBLEM IN SYNC PROCESSOR " + e);
            Log.e(TAG, new String(decrypt_key));
            Reporter.logStack(mContext, Event.Code.SYNC, e, "get_process()");
        }
    }



    public synchronized void post_process(byte[] encrypt_key){
        final File file = new File(mContext.getFilesDir(), Constants.UP_SYNC_FILE);
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(Clinic_Physician.TABLE, Clinic_Physician.up_sync(mContext));
            jsonObject.put(Patient_Physician.TABLE, Patient_Physician.up_sync(mContext));
            jsonObject.put(Device.TABLE, Device.up_sync(mContext));
            jsonObject.put(Patient.TABLE, Patient.up_sync(mContext));
            jsonObject.put(Record.TABLE, Record.up_sync(mContext));
            jsonObject.put(Test.TABLE, Test.up_sync(mContext));
            jsonObject.put(Encounter.TABLE, Encounter.up_sync(mContext));
            jsonObject.put(Visit.TABLE, Visit.up_sync(mContext));
            jsonObject.put(Medication.TABLE, Medication.up_sync(mContext));
            jsonObject.put(Event.TABLE, Event.up_sync(mContext));
            Log.e(TAG, jsonObject.toString());
            byte[] rawText = jsonObject.toString().getBytes();
            byte[] encryptedText = Security.encrypt(encrypt_key, rawText, true);
            FileChannel channel = new FileOutputStream(file, false).getChannel();
            channel.write(ByteBuffer.wrap(encryptedText));
            channel.close();
//        } catch (NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | ShortBufferException | BadPaddingException | IllegalBlockSizeException | IOException | JSONException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param pairList {url, fileName}
     */
    public synchronized void put_images(List<Pair<String, String>> pairList){
        File inputFile = new File(mContext.getFilesDir(), Constants.DOWN_IMAGES_CSV_FILE);

        StringBuilder sb = new StringBuilder();
        for(Pair<String, String> pair : pairList){
            if(!pair.first.isEmpty() && !pair.first.equals("null")) {
                sb.append(pair.first).append(",").append(pair.second).append("\n");
            }
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(inputFile, true));
            bw.append(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try{
                if(bw != null) bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
