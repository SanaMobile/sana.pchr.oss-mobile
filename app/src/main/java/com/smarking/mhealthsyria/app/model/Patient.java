package com.smarking.mhealthsyria.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.api.sync.SyncProcessor;
import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.category.Category;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-03.
 */
public class Patient extends Model {
    public static String TABLE = "Patient";

    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    // These are the table column name identifiers
    public static final String FIRSTNAME = "firstName";
    public static final String LASTNAME = "lastName";
    public static final String UNHCR = "UNHCR";
    public static final String BIRTHYEAR = "birthYear";
    public static final String BIRTHCITY = "birthCity";
    public static final String PICTURE = "picture";
    public static final String GENDER = "gender";
    public static final String PHONE = "phone";
    public static final String PROVIDERID = "provider_id";


    // These are the actual values
    private String firstName;
    private String lastName;
    private String unCHR;
    private String birthYear;
    private String birthCity;
    private String picture;
    private String gender;
    private String phone;
    private String providerId;

    public Patient(String uuid, String created, String updated, String synchronized_, String firstName,
                   String lastName, String unCHR, String birthYear, String birthCity, String picture,
                   String gender, String phone, String providerId) {
        super(uuid, created, updated, synchronized_);
        this.firstName = firstName;
        this.lastName = lastName;
        this.unCHR = unCHR;
        this.birthYear = birthYear;
        this.birthCity = birthCity;
        this.picture = picture;
        this.gender = gender;
        this.phone = phone;
        this.providerId = providerId;
    }

    public Patient(String firstName, String lastName, String unCHR, String birthYear, String birthCity, String gender,
                   String providerId, String phone) {
        super("", "", "", "");
        this.firstName = firstName;
        this.lastName = lastName;
        this.unCHR = unCHR;
        this.birthYear = birthYear;
        this.birthCity = birthCity;
        this.gender = gender;
        this.providerId = providerId;
        this.phone = phone;
    }

    public Patient(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.firstName = jsonObject.getString(FIRSTNAME);
        this.lastName = jsonObject.getString(LASTNAME);
        this.unCHR= jsonObject.getString(UNHCR);
        this.birthYear = jsonObject.getString(BIRTHYEAR);
        this.birthCity = jsonObject.getString(BIRTHCITY);
        this.picture = jsonObject.getString(PICTURE);
        this.gender = jsonObject.getString(GENDER);
        this.phone = jsonObject.getString(PHONE);
        this.providerId = jsonObject.getString(PROVIDERID);
    }

    public Patient(Parcel in) {
        super(in);
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.unCHR = in.readString();
        this.birthYear = in.readString();
        this.birthCity = in.readString();
        this.picture = in.readString();
        this.gender = in.readString();
        this.phone = in.readString();
        this.providerId = in.readString();
    }

    public Patient(Cursor c){
        super(c);
        this.firstName = getString(c, FIRSTNAME);
        this.lastName = getString(c, LASTNAME);
        this.unCHR = getString(c, UNHCR);
        this.birthYear = getString(c, BIRTHYEAR);
        this.birthCity = getString(c, BIRTHCITY);
        this.picture = getString(c, PICTURE);
        this.gender = getString(c, GENDER);
        this.phone = getString(c, PHONE);
        this.providerId = getString(c, PROVIDERID);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(unCHR);
        parcel.writeString(birthYear);
        parcel.writeString(birthCity);
        parcel.writeString(picture);
        parcel.writeString(gender);
        parcel.writeString(phone);
        parcel.writeString(providerId);
    }

    public static final Parcelable.Creator<Patient> CREATOR = new Parcelable.Creator<Patient>() {
        public Patient createFromParcel(Parcel in) {
            return new Patient(in);
        }

        public Patient[] newArray(int size) {
            return new Patient[size];
        }
    };


    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(FIRSTNAME, firstName);
        contentValues.put(LASTNAME, lastName);
        contentValues.put(UNHCR, unCHR);
        contentValues.put(BIRTHYEAR, birthYear);
        contentValues.put(BIRTHCITY, birthCity);
        contentValues.put(PICTURE, picture);
        contentValues.put(GENDER, gender);
        contentValues.put(PHONE, phone);
        contentValues.put(PROVIDERID, providerId);
        return contentValues;
    }

    public JSONObject jsonObject(){
        JSONObject jsonObject = super.getJSONObject();
        try {
            jsonObject.put(FIRSTNAME, firstName);
            jsonObject.put(LASTNAME, lastName);
            jsonObject.put(UNHCR, unCHR);
            jsonObject.put(BIRTHYEAR, birthYear);
            jsonObject.put(BIRTHCITY, birthCity);
            jsonObject.put(PICTURE, picture);
            jsonObject.put(GENDER, gender);
            jsonObject.put(PHONE, phone);
            jsonObject.put(PROVIDERID, providerId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONArray up_sync(Context context){
        JSONArray jsonArray = new JSONArray();
        String whereClause = String.format(" %s = %s OR %s = %s ", SYNCHRONIZED, "'null'", SYNCHRONIZED, "''");

        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        while(cursor.moveToNext()){
            jsonArray.put(new Patient(cursor).jsonObject());
        }
        cursor.close();
        return jsonArray;
    }

    public static Patient getPatient(Context c, String uuid) throws NoSuchElementException {
        Uri uri = Uri.withAppendedPath(Patient.URI, uuid.toLowerCase());
        Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);

        Patient patient = null;
        if (cursor.moveToFirst()) {
            patient = new Patient(cursor);
        }
        cursor.close();
        DatabaseHandler.get(c).closeDatabase();

        return patient;
    }

    public String getProviderID() {
        return providerId;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUNHCR() {
        return unCHR;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public String getBirthCity() {
        return birthCity;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getFullName(){
        if (Category.language == Category.Language.ARABIC) {
            return lastName + " " + firstName;
        } else {
            return firstName + " " + lastName;
        }
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();

        List<Pair<String, String>> images = new ArrayList<>();

        ContentValues[] contentValues = new ContentValues[length];
        for(int i = 0; i<length; i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            /*final String fileName = jsonObject.getString(UUID) + ".jpg";
            String pictureURL = jsonObject.getString(PICTURE);
            jsonObject.put(PICTURE, fileName);
            Log.e(TABLE + " > save", pictureURL + " " + fileName);
            images.add(new Pair<>(pictureURL, fileName));*/

            Patient patient = new Patient(jsonObject);
            contentValues[i] = patient.putContentValues();
        }

        new SyncProcessor(c).put_images(images);

        return c.getContentResolver().bulkInsert(URI, contentValues);
    }

    public static final Model.Builder<Patient> BUILDER = new Model.Builder<Patient>() {
        @Override
        public Patient build(Cursor cursor) {
            return new Patient(cursor);
        }
    };
}
