package com.smarking.mhealthsyria.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.SanaApplication;
import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.api.sync.SyncProcessor;
import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.category.Category;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by Saravanan Vaithianathan (svaithia@uwaterloo.ca) on 08/03/15.
 */
public class Physician extends Model implements Parcelable{
    public static final String TABLE = "Physician";

    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static final String FIRSTNAME = "firstName";
    public static final String LASTNAME = "lastName";
    public static final String PICTURE = "picture";
    public static final String DEVICE_KEY_USER_KEY = "device_key_user_key";
    public static final String HASHEDPIN_USER_KEY = "hashedPIN_user_key";

    public static final String SALT = "recovery_salt";
    public static final String RECOVERY_KEY = "recovery_device_key";
    public static final String RECOVERY_QUESTION = "recovery_question";


    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String TYPE = "type";

    public static final String USER_KEY_STRING = "USER_KEY_STRING";

    private String firstName;
    private String lastName;
    private String picture;
    private String key;
    private String hashedPIN_user_key;

    private String recoveryKey;
    private String recoveryQuestion;
    private String salt;

    private String phone;
    private String email;
    private String type;

    public Physician(String uuid, String created, String updated, String synchronized_, String fname,
                     String lname, String picturePath, String deviceKeyUserKey, String hashedPinUserKey,
                     String recoveryKey, String salt, String recoveryQuestion, String type) {
        super(uuid, created, updated, synchronized_);
        this.firstName = fname;
        this.lastName = lname;
        this.picture = picturePath;
        this.key = deviceKeyUserKey;
        this.hashedPIN_user_key = hashedPinUserKey;
        this.recoveryKey = recoveryKey;
        this.salt = salt;
        this.recoveryQuestion = recoveryQuestion;
        this.type = type;
    }

    public Physician(JSONObject jsonObject, boolean sync) throws JSONException {
        super(jsonObject);
        this.firstName = jsonObject.getString(FIRSTNAME);
        this.lastName = jsonObject.getString(LASTNAME);
        this.type = jsonObject.getString(TYPE);


        if(!sync) {
            this.picture = jsonObject.getString(PICTURE);
            this.key = jsonObject.getString(DEVICE_KEY_USER_KEY);
            this.hashedPIN_user_key = jsonObject.getString(HASHEDPIN_USER_KEY);
            this.salt = jsonObject.getString(SALT);
            this.recoveryKey = jsonObject.getString(RECOVERY_KEY);
            this.recoveryQuestion = jsonObject.getString(RECOVERY_QUESTION);
        } else {
            this.phone = jsonObject.getString(PHONE);
            this.email = jsonObject.getString(EMAIL);
        }
    }

    public Physician(String firstName, String lastName, String key, String pin, String phone, String email, String date, String type) {
        super("",date, date, "");
        this.firstName = firstName;
        this.lastName = lastName;
        this.key = key;
        this.hashedPIN_user_key = pin;
        this.phone = phone;
        this.email = email;
        this.type = type;
    }

    public Physician(Parcel in) {
        super(in);
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.picture = in.readString();
        this.key = in.readString();
        this.hashedPIN_user_key = in.readString();
        this.recoveryKey = in.readString();
        this.salt = in.readString();
        this.recoveryQuestion = in.readString();
        this.type = in.readString();
    }

    public Physician(Cursor c){
        super(c);
        this.firstName = getString(c, FIRSTNAME);
        this.lastName = getString(c, LASTNAME);
        this.picture = getString(c, PICTURE);
        this.key = getString(c, DEVICE_KEY_USER_KEY);
        this.hashedPIN_user_key = getString(c, HASHEDPIN_USER_KEY);
        this.recoveryKey = getString(c, RECOVERY_KEY);
        this.salt = getString(c, SALT);
        this.recoveryQuestion = getString(c, RECOVERY_QUESTION);
        this.type = getString(c, TYPE);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(picture);
        parcel.writeString(key);
        parcel.writeString(hashedPIN_user_key);
        parcel.writeString(recoveryKey);
        parcel.writeString(salt);
        parcel.writeString(recoveryQuestion);
        parcel.writeString(type);
    }

    public String getFullName() {
        int salutation = 0;
        if (type != null) {
            switch (type) {
                case "D":
                    salutation = 0;
                    break;
                case "N":
                    salutation = 1;
                    break;
            }
        }

        String sal = "";
        try {
            sal = SanaApplication.getAppContext().getResources().getStringArray(R.array.physician_salutation)[salutation];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(this.getClass().getSimpleName(), "Dr. Salutation wrong: " + salutation);
        }

        //All names in English anyway.
        /*if (Category.language == Category.Language.ARABIC) {
            return lastName + " " + firstName + " " + sal;
        } else {*/
            return sal + " " + firstName + " " + lastName;
        //}
    }


    public String getType() {
        return type;
    }

    public String getRecoveryQuestion() {
        return recoveryQuestion;
    }

    public String getRecoveryKey() {
        return recoveryKey;
    }

    public String getSalt() {
        return salt;
    }

    public String getKey() {
        return key;
    }

    public String getHashedPINUserKey() {
        return hashedPIN_user_key;
    }

    public String getPicture() {
        return picture;
    }

    public String getEmail(){
        if(this.email == null)
            return "";
        return this.email;
    }

    public String getPhone(){
        if(this.phone == null)
            return "";
        return this.phone;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public static Bitmap getBitmap(Context context, String fileName) {
        final File dir = new File(context.getFilesDir(), Constants.PICTURE_DIRECTORY);
        File imgFile = fileName == null || fileName.isEmpty()? null : new File(dir.getPath(), fileName);

        boolean useDefaultPicture = imgFile == null || !imgFile.exists();

        return useDefaultPicture
                ? ((BitmapDrawable) context.getResources().getDrawable(R.drawable.default_profile_picture)).getBitmap()
                : BitmapFactory.decodeFile(imgFile.getAbsolutePath());
    }

    public Bitmap getBitmap(Context context) {
        return getBitmap(context, picture);
    }



    public static Physician getPhysician(Context c, String uuid) throws NoSuchElementException{
        Uri uri = Uri.withAppendedPath(Physician.URI, uuid.toLowerCase());
        Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);

        Physician physician = null;
        if(cursor.moveToFirst()){
            physician = new Physician(cursor);
        }
        cursor.close();
        DatabaseHandler.get(c).closeDatabase();

        return physician;
    }

    public JSONObject jsonObject() {
        JSONObject jsonObject = super.getJSONObject();
        try {
            jsonObject.put(FIRSTNAME, firstName);
            jsonObject.put(LASTNAME, lastName);
            jsonObject.put(HASHEDPIN_USER_KEY, hashedPIN_user_key);
            jsonObject.put(PHONE, phone);
            jsonObject.put(EMAIL, email);
            jsonObject.put(PICTURE, picture);
            jsonObject.put(TYPE, type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONArray up_sync(Context context) {
        JSONArray jsonArray = new JSONArray();
        String whereClause = String.format(" %s = %s OR %s = %s ", SYNCHRONIZED, "'null'", SYNCHRONIZED, "''");

        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        while (cursor.moveToNext()) {
            jsonArray.put(new Physician(cursor).jsonObject());
        }
        cursor.close();
        return jsonArray;
    }

    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(FIRSTNAME, firstName);
        contentValues.put(LASTNAME, lastName);
        if(picture != null) contentValues.put(PICTURE, picture);
        if(key != null) contentValues.put(DEVICE_KEY_USER_KEY, key);
        if(hashedPIN_user_key != null) contentValues.put(HASHEDPIN_USER_KEY, hashedPIN_user_key);
        if(phone != null) contentValues.put(PHONE, phone);
        if(email != null) contentValues.put(EMAIL, email);
        if (salt != null) contentValues.put(SALT, salt);
        if (recoveryKey != null) contentValues.put(RECOVERY_KEY, recoveryKey);
        if (recoveryQuestion != null) contentValues.put(RECOVERY_QUESTION, recoveryQuestion);
        if (type != null) contentValues.put(TYPE, type);
        return contentValues;
    }

    public static List<Physician> getPhysicians(Cursor cursor){
        List<Physician> physicians = new ArrayList<Physician>();

        while(cursor.moveToNext()){
            physicians.add(new Physician(cursor));
        }
        cursor.close();
        return physicians;
    }

    public static final Parcelable.Creator<Physician> CREATOR = new Parcelable.Creator<Physician>() {
        public Physician createFromParcel(Parcel in) {
            return new Physician(in);
        }

        public Physician[] newArray(int size) {
            return new Physician[size];
        }
    };

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();

        List<Pair<String, String>> images = new ArrayList<>();

        ContentValues[] contentValueses = new ContentValues[length];
        for(int i = 0; i<length; i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            /*final String fileName = jsonObject.getString(UUID) + ".jpg";
            String pictureURL = jsonObject.getString(PICTURE);
            jsonObject.put(PICTURE, fileName);
            images.add(new Pair<>(pictureURL, fileName));*/

            Physician physician = new Physician(jsonObject, true);
            contentValueses[i] = physician.putContentValues();
        }

        new SyncProcessor(c).put_images(images);

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }

    public static Physician get(Context context, String uuid){
        String whereClause = String.format(" %s = '%s' ", UUID, uuid);
        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        Physician physician = cursor.moveToNext() ? new Physician(cursor) : null;
        if(!cursor.isClosed()) cursor.close();
        return physician;
    }


    public static final Model.Builder<Physician> BUILDER = new Model.Builder<Physician>() {
        public Physician build(Cursor cursor) {
            return new Physician(cursor);
        }
    };
}
