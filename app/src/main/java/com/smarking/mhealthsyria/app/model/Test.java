package com.smarking.mhealthsyria.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.smarking.mhealthsyria.app.SanaApplication;
import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.model.category.EncounterCategory;
import com.smarking.mhealthsyria.app.model.category.TestCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-04.
 */
public class Test extends Model implements Comparable<Test> {
    public static String TABLE = "Test";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static String RESULT = "result";
    public static String CATEGORY_UUID = "category_uuid";
    public static String ENCOUNTER_UUID = "encounter_uuid";

    private String result;
    private String category_uuid;
    private String encounter_uuid;

    public Test(String uuid, String created, String updated, String synchronized_, String result, String category_uuid, String encounter_uuid) {
        super(uuid, created, updated, synchronized_);
        this.result = result;
        this.category_uuid = category_uuid;
        this.encounter_uuid = encounter_uuid;
    }

    public Test(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.result = jsonObject.getString(RESULT);
        this.category_uuid = jsonObject.getString(CATEGORY_UUID);
        this.encounter_uuid = jsonObject.getString(ENCOUNTER_UUID);
    }

    public Test(Cursor cursor) {
        super(cursor);
        this.result = getString(cursor, RESULT);
        this.category_uuid = getString(cursor, CATEGORY_UUID);
        this.encounter_uuid = getString(cursor, ENCOUNTER_UUID);
    }

    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(RESULT, result);
        contentValues.put(CATEGORY_UUID, category_uuid);
        contentValues.put(ENCOUNTER_UUID, encounter_uuid);
        return contentValues;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCategory_uuid() {
        return category_uuid;
    }

    public void setCategory_uuid(String category_uuid) {
        this.category_uuid = category_uuid;
    }

    public String getEncounter_uuid() {
        return encounter_uuid;
    }

    public void setEncounter_uuid(String encounter_uuid) {
        this.encounter_uuid = encounter_uuid;
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValueses = new ContentValues[length];
        for(int i = 0; i<length; i++){
            Test test = new Test(jsonArray.getJSONObject(i));
            contentValueses[i] = test.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }

    @Override
    public JSONObject getJSONObject(){
        JSONObject jsonObject = super.getJSONObject();
        try {
            jsonObject.put(RESULT, result);
            jsonObject.put(CATEGORY_UUID, category_uuid);
            jsonObject.put(ENCOUNTER_UUID, encounter_uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public Test(String result, TestCategory testCategory, Encounter encounter) {
        super("", "", "", "");
        this.result = result;
        this.category_uuid = testCategory.uuid;
        this.encounter_uuid = encounter.uuid;
    }

    public Test(String result, String testCategory, Encounter encounter) {
        super("", "", "", "");
        this.result = result;
        this.category_uuid = testCategory;
        this.encounter_uuid = encounter.uuid;
    }

    public static synchronized Uri create(Context context, Visit visit, EncounterCategory encounterCategory,String result, TestCategory testCategory) throws IllegalAccessException {
        Encounter encounter = new Encounter(context, visit, encounterCategory);
        Uri uri_encounter = context.getContentResolver().insert(Encounter.URI, encounter.putContentValues());
        Cursor cursor = context.getContentResolver().query(uri_encounter, null, null, null, null);
        if(!cursor.moveToFirst()) throw new IllegalAccessException();
        encounter = new Encounter(cursor);
        cursor.close();

        Test test = new Test(result, testCategory, encounter);

        return context.getContentResolver().insert(URI, test.putContentValues());
    }

    public static Test get(Context context, String uuid) {
        Cursor cursor = context.getContentResolver().query(URI, null, UUID + " = " + uuid, null, null);
        Test test = new Test(cursor);
        cursor.close();
        return test;
    }

    public static JSONArray up_sync(Context context){
        JSONArray jsonArray = new JSONArray();
        String whereClause = String.format(" %s = %s OR %s = %s ", SYNCHRONIZED, "'null'", SYNCHRONIZED, "''");

        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        while(cursor.moveToNext()){
            jsonArray.put(new Test(cursor).getJSONObject());
        }
        cursor.close();
        return jsonArray;
    }

    public static final Model.Builder<Test> BUILDER = new Model.Builder<Test>() {

        @Override
        public Test build(Cursor cursor) {
            return new Test(cursor);
        }
    };
    public boolean equals(Test test) {
        return !(test == null || !test.getCategory_uuid().equals(category_uuid));
    }

    public int compareTo(Test t2) {
        if (TestCategory.get(SanaApplication.getAppContext(), category_uuid).getPriority() >
                TestCategory.get(SanaApplication.getAppContext(), t2.getCategory_uuid()).getPriority()) {
            return 1;
        } else {
            return -1;
        }
    }

}
