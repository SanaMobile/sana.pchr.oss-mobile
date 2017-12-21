package com.smarking.mhealthsyria.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-23.
 */
public class Clinic extends Model{
    public static String TABLE = "Clinic";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static String NAME = "name";
    public static String LONGITUDE = "longitude";
    public static String LATITUDE = "latitude";
    public static String LANGUAGE = "language";

    public final String name;
    public final String longitude;
    public final String latitude;
    public final String language;

    public Clinic(String uuid, String created, String updated, String synchronized_, String name, String longitude, String latitude, String language) {
        super(uuid, created, updated, synchronized_);
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.language = language;
    }

    public Clinic(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.name = jsonObject.getString(NAME);
        this.longitude = jsonObject.getString(LONGITUDE);
        this.latitude = jsonObject.getString(LATITUDE);
        this.language = jsonObject.getString(LANGUAGE);
    }

    public Clinic(Cursor cursor) {
        super(cursor);
        this.name = getString(cursor, NAME);
        this.longitude = getString(cursor, LONGITUDE);
        this.latitude = getString(cursor, LATITUDE);
        this.language = getString(cursor, LANGUAGE);
    }

    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(NAME, name);
        contentValues.put(LONGITUDE, longitude);
        contentValues.put(LATITUDE, latitude);
        contentValues.put(LANGUAGE, language);
        return contentValues;
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValues = new ContentValues[length];
        for(int i = 0; i<length; i++){
            Clinic clinic = new Clinic(jsonArray.getJSONObject(i));
            contentValues[i] = clinic.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValues);
    }

    public static Clinic get(Context context, String uuid){
        String whereClause = String.format(" %s = '%s' ", UUID, uuid);
        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        Clinic clinic = cursor.moveToNext() ? new Clinic(cursor) : null;
        if(!cursor.isClosed()) cursor.close();
        return clinic;
    }

    public static List<Clinic> getClinics(Cursor cursor) {
        List<Clinic> clinics = new ArrayList<Clinic>();

        while (cursor.moveToNext()) {
            clinics.add(new Clinic(cursor));
        }
        cursor.close();
        return clinics;
    }

    static final Clinic[] demos = new Clinic[]{
            new Clinic("cb91eb82a359417a9a734b4637766f85",
                    "2016-02-01'T'03:49:46",
                    "2016-02-01'T'03:56:44",
                    "", "Test(En)", "", "", "en"),
            new Clinic( "366924b31f1a46c898931aa46190b8df",
                    "2016-02-01'T'03:48:46", "2016-02-01'T'03:56:46", "",
                    "Test(Ar)","", "", "ar")
    } ;
    public static List<Clinic> getDemoClinics(){

        List<Clinic> clinics = new ArrayList<>();
        for(Clinic clinic:demos){
            clinics.add(clinic);
        }
        return clinics;
    }


    public static final Model.Builder<Clinic> BUILDER = new Model.Builder<Clinic>() {
        public Clinic build(Cursor cursor) {
            return new Clinic(cursor);
        }
    };
}
