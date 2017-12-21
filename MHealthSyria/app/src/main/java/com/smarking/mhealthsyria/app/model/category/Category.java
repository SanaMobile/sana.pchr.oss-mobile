package com.smarking.mhealthsyria.app.model.category;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;

import com.smarking.mhealthsyria.app.model.Model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-24.
 */
public abstract class Category extends Model implements Comparable<Category> {
    public static String TAG = Category.class.getSimpleName();

    public enum Language {
        ENGLISH, ARABIC
    }

    public static Language language;

    private String displayName;
    private String displayNameAr;
    protected int priority;

    public static final String DISPLAYNAME = "displayName";
    public static final String DISPLAYNAMEAR = "displayNameAr";
    public static final String PRIORITY = "priority";

    public Category(String uuid, String created, String updated, String synchronized_, String displayName, String displayNameAr, int priority) {
        super(uuid, created, updated, synchronized_);
        this.displayName = displayName;
        this.displayNameAr = displayNameAr;
        this.priority = priority;
    }

    public Category(String uuid, String created, String updated, String synchronized_, String displayName, String displayNameAr) {
        super(uuid, created, updated, synchronized_);
        this.displayName = displayName;
        this.displayNameAr = displayNameAr;
        this.priority = 1;
    }

    public Category(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.displayName = jsonObject.getString(DISPLAYNAME);
        this.displayNameAr = jsonObject.getString(DISPLAYNAMEAR);
        this.priority = jsonObject.getInt(PRIORITY);
    }

    public Category(Cursor cursor) {
        super(cursor);
        this.displayName = getString(cursor, DISPLAYNAME);
        this.displayNameAr = getString(cursor, DISPLAYNAMEAR);
        this.priority = getInt(cursor, PRIORITY);
    }

    public Category(Parcel in) {
        super(in);
        this.displayName = in.readString();
        this.displayNameAr = in.readString();
        this.priority = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeString(displayName);
        parcel.writeString(displayNameAr);
        parcel.writeInt(priority);
    }

    public ContentValues putContentValues(){
        ContentValues contentValues = super.putContentValues();
        contentValues.put(DISPLAYNAME, displayName);
        contentValues.put(DISPLAYNAMEAR, displayNameAr);
        contentValues.put(PRIORITY, priority);
        return contentValues;
    }

    public String getDisplayName() {
        if (language == Language.ARABIC) {
            return displayNameAr;
        } else {
            return displayName;
        }
    }

    public String getDisplayNameAr() {
        return displayNameAr;
    }

    public String getUUID() {
        return this.uuid;
    }

    public int compareTo(Category testCategory) {
        if (this.priority > testCategory.priority) {
            return 1;
        } else {
            return -1;
        }
    }
}
