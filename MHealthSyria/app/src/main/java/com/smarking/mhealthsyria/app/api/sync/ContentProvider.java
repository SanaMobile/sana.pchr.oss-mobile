package com.smarking.mhealthsyria.app.api.sync;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.smarking.mhealthsyria.app.api.URIPath;
import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.Model;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-07.
 */
public class ContentProvider extends android.content.ContentProvider {
    public static final String TAG = android.content.ContentProvider.class.getSimpleName();

    public static final String AUTHORITY = "edu.mit.mhealthsyria";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY);

    public static final String CONTENT_LIST = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY;
    public static final String CONTENT_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        for (URIPath route : URIPath.values()) {
            uriMatcher.addURI(AUTHORITY, route.path, route.id);
        }
    }

    private DatabaseHandler mDatabaseHandler;

    @Override
    public boolean onCreate() {
        mDatabaseHandler = DatabaseHandler.get(getContext());
        return true;
    }

    public URIPath getUriPath(Uri uri) {
        int uriMatch = uriMatcher.match(uri);

        if (UriMatcher.NO_MATCH == uriMatch) {
            throw new IllegalArgumentException(uri.toString() + " is not a supported URI");
        }
        return URIPath.LOOKUP.get(uriMatch);
    }

    @Override
    public String getType(Uri uri) {
        URIPath uriPath = getUriPath(uri);
        if(uriPath.path.endsWith("*")){
            return CONTENT_ITEM;
        }
        else{
            return CONTENT_LIST;
        }
    }

    public static String parseUuid(Uri contentUri) {
        return contentUri.getLastPathSegment();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        URIPath uriPath = getUriPath(uri);

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(uriPath.table);

        if(getType(uri).equals(CONTENT_ITEM)){
            sqlBuilder.appendWhere(String.format(" %s = '%s' ", Model.UUID, parseUuid(uri)));
        }

        Cursor returnCursor = null;
        try{
            SQLiteDatabase db = mDatabaseHandler.openDatabase();
            returnCursor = sqlBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (Exception e) {
            Log.e(TAG, "DB Cursor error: " + e.getClass().getSimpleName() + " " + e.getMessage());
            if (returnCursor != null) {
                if (returnCursor.isClosed() == false) {
                    returnCursor.close();
                }
            }
            mDatabaseHandler.closeDatabase();
        } finally {
            //TODO IF DB IS CLOSED, THEN THE CURSOR RETURN NO RESULTS! CLIENT SHOULD CLOSE THE DB.
            /*

            mDatabaseHandler.closeDatabase();*/
        }

        return returnCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        URIPath uriPath = getUriPath(uri);

        String uuid = values.getAsString(Model.UUID);

        long id;
        try{
            SQLiteDatabase db = mDatabaseHandler.openDatabase();
            if(uuid == null || uuid.isEmpty()){
                uuid = UUID.randomUUID().toString().replaceAll("-", "");
                String currentTime = Model.ISO8601_FORMAT_TZ.format(new Date());

                values.put(Model.UUID, uuid);
                values.put(Model.CREATED, currentTime);
                values.put(Model.UPDATED, currentTime);
            }

            Cursor existing = query(Uri.withAppendedPath(uri, uuid), null, null, null, null);
            if(existing.moveToFirst()){
                Set<String> existingKeys = new HashSet<>();
                for(int i = 0, len = existing.getColumnCount(); i<len; i++){
                    existingKeys.add(existing.getColumnName(i));
                }
                existingKeys.removeAll(values.keySet());
                for(String key : existingKeys){
                    int columnIndex = existing.getColumnIndex(key);
                    switch(existing.getType(columnIndex)){
                        case Cursor.FIELD_TYPE_BLOB:
                            values.put(key, existing.getBlob(columnIndex));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            values.put(key, existing.getFloat(columnIndex));
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            values.put(key, existing.getInt(columnIndex));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            values.put(key, existing.getString(columnIndex));
                            break;
                    }
                }
            }
            existing.close();
            id = db.insertWithOnConflict(uriPath.table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } finally{
            mDatabaseHandler.closeDatabase();
        }

        if(id < 1){
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null, true);
        return Uri.withAppendedPath(uri, uuid);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        URIPath uriPath = getUriPath(uri);

        if(getType(uri).equals(CONTENT_ITEM)){
            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            uri = Uri.parse(uri.toString().replaceAll("/" + uri.getLastPathSegment() + "$", ""));
        }

        int delete_count;
        try{
            SQLiteDatabase db = mDatabaseHandler.openDatabase();
            delete_count = db.delete(uriPath.table, selection, selectionArgs);
        } finally{
            mDatabaseHandler.closeDatabase();
        }

        if(0 != delete_count) {
            getContext().getContentResolver().notifyChange(uri, null, true);
        }

        return delete_count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        URIPath uriPath = getUriPath(uri);

        if (getType(uri).equals(CONTENT_ITEM)) {
            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            uri = Uri.parse(uri.toString().replaceAll("/" + uri.getLastPathSegment() + "$", ""));
        }

        int num_rows;
        try {
            SQLiteDatabase db = mDatabaseHandler.openDatabase();
            num_rows = db.update(uriPath.table, values, selection, selectionArgs);
        } finally {
            mDatabaseHandler.closeDatabase();
        }

        if (0 != num_rows) {
            getContext().getContentResolver().notifyChange(uri, null, true);
        }

        return num_rows;
    }
}
