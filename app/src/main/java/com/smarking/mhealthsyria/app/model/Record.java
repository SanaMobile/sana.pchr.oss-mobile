package com.smarking.mhealthsyria.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.model.category.EncounterCategory;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-04.
 */
public class Record extends Model {
    public static String TABLE = "Record";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static String VALUE = "value";
    public static String COMMENT = "comment";
    public static String CATEGORY_UUID = "category_uuid";
    public static String ENCOUNTER_UUID = "encounter_uuid";

    private String value;
    private String value_ar = "";
    private String comment;
    private String comment_ar = "";
    private String category_uuid;
    private String encounter_uuid;

    public Record(String uuid, String created, String updated, String synchronized_, String value, String comment, String category_uuid, String encounter_uuid) {
        super(uuid, created, updated, synchronized_);
        this.value = value;
        this.comment = comment;
        this.category_uuid = category_uuid;
        this.encounter_uuid = encounter_uuid;
    }

    public Record(String value, String comment, String category_uuid, String encounter_uuid, String value_ar, String comment_ar) {
        super("", "", "", "");
        this.value = value;
        this.comment = comment;
        this.category_uuid = category_uuid;
        this.encounter_uuid = encounter_uuid;
        this.value_ar = value_ar;
        this.comment_ar = comment_ar;
    }

    public Record(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.value = jsonObject.getString(VALUE);
        this.comment = jsonObject.getString(COMMENT);
        this.category_uuid = jsonObject.getString(CATEGORY_UUID);
        this.encounter_uuid = jsonObject.getString(ENCOUNTER_UUID);
    }

    public Record(Cursor cursor) {
        super(cursor);
        this.value = getString(cursor, VALUE);
        this.comment = getString(cursor, COMMENT);
        this.category_uuid = getString(cursor, CATEGORY_UUID);
        this.encounter_uuid = getString(cursor, ENCOUNTER_UUID);
    }

    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(VALUE, value);
        contentValues.put(COMMENT, comment);
        contentValues.put(CATEGORY_UUID, category_uuid);
        contentValues.put(ENCOUNTER_UUID, encounter_uuid);
        return contentValues;
    }

    public String getValue() {
        return blankIfNull(value);
    }

    public String getValueAr() {
        return blankIfNull(value_ar);
    }

    public void setValueAr(String ar) {
        this.value_ar = blankIfNull(ar);
    }

    public void setCommentAr(String ar) {
        this.comment_ar = blankIfNull(ar);
    }


    public String getCommentAr() {
        return blankIfNull(comment_ar);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return blankIfNull(comment);
    }

    public void setComment(String comment) {
        this.comment = comment;
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
            Record record = new Record(jsonArray.getJSONObject(i));
            contentValueses[i] = record.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }

    public Record(String value, String comment, RecordCategory category, Encounter encounter) {
        super("", "", "", "");
        this.value = value;
        this.comment = comment;
        this.category_uuid = category.uuid;
        this.encounter_uuid = encounter.uuid;
    }

    public Record(String value, String comment, String category_uuid, Encounter encounter) {
        super("", "", "", "");
        this.value = value;
        this.comment = comment;
        this.category_uuid = category_uuid;
        this.encounter_uuid = encounter.uuid;
    }

    public static synchronized Uri create(Context context, Visit visit, EncounterCategory encounterCategory, String value, String comment, RecordCategory recordCategory) throws IllegalAccessException {
        Encounter encounter = new Encounter(context, visit, encounterCategory);
        Uri uri_encounter = context.getContentResolver().insert(Encounter.URI, encounter.putContentValues());
        Cursor cursor = context.getContentResolver().query(uri_encounter, null, null, null, null);
        if(!cursor.moveToFirst()) throw new IllegalAccessException();
        encounter = new Encounter(cursor);
        cursor.close();

        Record record = new Record(value, comment, recordCategory, encounter);

        return context.getContentResolver().insert(URI, record.putContentValues());
    }

    public static Record get(Context context, String uuid){
        Cursor cursor = context.getContentResolver().query(URI, null, UUID + " = " + uuid, null, null);
        Record record = new Record(cursor);
        cursor.close();
        return record;
    }

    public static JSONArray up_sync(Context context){
        JSONArray jsonArray = new JSONArray();
        String whereClause = String.format(" %s = %s OR %s = %s ", SYNCHRONIZED, "'null'", SYNCHRONIZED, "''");

        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        while(cursor.moveToNext()){
            jsonArray.put(new Record(cursor).jsonObject());
        }
        cursor.close();
        return jsonArray;
    }

    public JSONObject jsonObject(){
        JSONObject jsonObject = super.getJSONObject();
        try {
            jsonObject.put(VALUE, value);
            jsonObject.put(COMMENT, comment);
            jsonObject.put(CATEGORY_UUID, category_uuid);
            jsonObject.put(ENCOUNTER_UUID, encounter_uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static final Model.Builder<Record> BUILDER = new Model.Builder<Record>() {
        @Override
        public Record build(Cursor cursor) {
            return new Record(cursor);
        }
    };
}
