package com.smarking.mhealthsyria.app.model.category;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.smarking.mhealthsyria.app.api.sync.ContentProvider;
import com.smarking.mhealthsyria.app.model.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-04.
 */
public class TestCategory extends Category {
    public static String TABLE = "TestCategory";
    public static final Uri URI = Uri.withAppendedPath(ContentProvider.URI, TABLE);

    public static String RESULTTYPE = "resultType";
    public static String RESULTMIN = "resultMin";
    public static String RESULTMAX = "resultMax";
    public static String RESULTUNITS = "resultUnits";
    public static String RESULTUNITSAR = "resultUnitsAr";

    public static final int RESULT_TYPE_TEXT = 0;
    public static final int RESULT_TYPE_NUMBER = 1;
    public static final int RESULT_TYPE_BOOL = 2;
    public static final int RESULT_TYPE_OPTION = 3;


    private int resultType;
    private double resultMin;
    private double resultMax;
    private String resultUnits;
    private String resultUnitsAr;

    public TestCategory(String uuid, String created, String updated, String synchronized_,
                        String displayName, String displayNameAr, int resultType, float resultMin, float resultMax, String resultUnits, String resultUnitsAr) {
        super(uuid, created, updated, synchronized_, displayName, displayNameAr);
        this.resultType = resultType;
        this.resultMin = resultMin;
        this.resultMax = resultMax;
        this.resultUnits = resultUnits;
    }

    public TestCategory(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.resultType = jsonObject.getInt(RESULTTYPE);
        this.resultMin = jsonObject.getDouble(RESULTMIN);
        this.resultMax = jsonObject.getDouble(RESULTMAX);
        this.resultUnits = jsonObject.getString(RESULTUNITS);
        this.resultUnitsAr = jsonObject.getString(RESULTUNITSAR);

    }

    public TestCategory(Cursor cursor) {
        super(cursor);
        this.resultType = getInt(cursor, RESULTTYPE);
        this.resultMin = getDouble(cursor, RESULTMIN);
        this.resultMax = getDouble(cursor, RESULTMAX);
        this.resultUnits = getString(cursor, RESULTUNITS);
        this.resultUnitsAr = getString(cursor, RESULTUNITSAR);
    }

    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(RESULTTYPE, resultType);
        contentValues.put(RESULTMIN, resultMin);
        contentValues.put(RESULTMAX, resultMax);
        contentValues.put(RESULTUNITS, resultUnits);
        contentValues.put(RESULTUNITSAR, resultUnitsAr);
        return contentValues;
    }

    public int getResultType() {
        return resultType;
    }

    public void setResultType(int resultType) {
        this.resultType = resultType;
    }

    public double getResultMin() {
        return resultMin;
    }

    public void setResultMin(float resultMin) {
        this.resultMin = resultMin;
    }

    public double getResultMax() {
        return resultMax;
    }

    public void setResultMax(float resultMax) {
        this.resultMax = resultMax;
    }

    public String getResultUnits() {
        if (Category.language == Language.ARABIC) {
            return blankIfNull(resultUnitsAr);
        } else {
            return blankIfNull(resultUnits);
        }
    }

    public static int save(Context c, JSONArray jsonArray) throws JSONException {
        int length = jsonArray.length();
        ContentValues[] contentValueses = new ContentValues[length];
        for(int i = 0; i<length; i++){
            TestCategory testCategory = new TestCategory(jsonArray.getJSONObject(i));
            contentValueses[i] = testCategory.putContentValues();
        }

        return c.getContentResolver().bulkInsert(URI, contentValueses);
    }

    public static TestCategory get(Context context, String uuid) {
        String whereClause = String.format(" %s = '%s' ", UUID, uuid);
        Cursor cursor = context.getContentResolver().query(URI, null, whereClause, null, null);
        cursor.moveToFirst();
        TestCategory testCategory = new TestCategory(cursor);
        if(!cursor.isClosed()) cursor.close();
        return testCategory;
    }

    public int getPriority(){return this.priority;}

    public static final Model.Builder<TestCategory> BUILDER = new Model.Builder<TestCategory>() {
        public TestCategory build(Cursor cursor) {
            return new TestCategory(cursor);
        }
    };

}
