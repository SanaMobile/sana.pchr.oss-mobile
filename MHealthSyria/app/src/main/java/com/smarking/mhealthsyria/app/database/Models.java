package com.smarking.mhealthsyria.app.database;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Device;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Model;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.model.category.EncounterCategory;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.model.category.TestCategory;
import com.smarking.mhealthsyria.app.model.category.VisitCategory;

import java.util.ArrayList;
import java.util.List;


/**
 * Utility class for Model class instantiation from the database
 */
public final class Models {

    protected static final String SELECTION = " %s = '?' ";
    protected static final String SELECTION_EXPLICIT = " %s = '%s' ";
    protected static final String UUID_SELECTION = Model.UUID + " = '?' ";
    protected static final String[] UUID_PROJECTION = new String[]{Model.UUID};
    private static final String TAG = Models.class.getSimpleName();

    /**
     * Wrapper around the generalized database calls and hold a type specific
     * Uri and builder. Model classes should implement a {@link com.smarking.mhealthsyria.app.model.Model.Builder Model.Builder}
     * instance as  a static field named BUILDER.
     *
     * @param <T>
     */
    public static class Manager<T extends Model> {
        final Uri uri;
        final Model.Builder<T> builder;

        public Manager(Uri uri, Model.Builder<T> builder) {
            this.uri = uri;
            this.builder = builder;
        }

        /**
         * Returns a unique {@link com.smarking.mhealthsyria.app.model.Model Model}
         * instance identified by a unique id value stored in the {@link com.smarking.mhealthsyria.app.model.Model#UUID Model.UUID}
         * column of the database.
         *
         * @param context The context
         * @param uuid    The unique identifier
         * @return A new instance of the class
         * @throws ObjectDoesNotExistException
         * @throws MultipleObjectsExistException
         */
        public T get(Context context, String uuid) throws ObjectDoesNotExistException, MultipleObjectsExistException {
            T obj = null;
            Cursor cursor = null;
            try {
                cursor = Models.get(context, uri, uuid);
                obj = builder.build(cursor);
            } finally {
                if (cursor != null) cursor.close();
            }
            return obj;
        }

        /**
         * Returns an {@link java.util.List List<T>} of objects which have a
         * relationship to another model through a unique id.
         *
         * @param context      The context
         * @param instanceUUID The id value stored as the foreign key
         * @param relatedCol   The column name where the foreign key is stored
         * @param sortOrder    The order in which the objects are returned
         * @return
         */
        public List<T> all(Context context, String instanceUUID, String relatedCol,
                           String sortOrder) {
            Cursor cursor = null;
            List<T> objects = new ArrayList<T>();
            try {
                cursor = Models.all(context, instanceUUID, uri, relatedCol, sortOrder);
                while (cursor.moveToNext()) {
                    objects.add(builder.build(cursor));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return objects;
        }

        public List<T> all(Context context, String instanceUUID, String relatedCol) {
            return all(context, instanceUUID, relatedCol, null);
        }
    }

    // Model classes
    public static final Manager<Clinic> CLINICS = new Manager<>(Clinic.URI, Clinic.BUILDER);
    public static final Manager<Device> DEVICES = new Manager<>(Device.URI, Device.BUILDER);
    public static final Manager<Encounter> ENCOUNTERS = new Manager<>(Encounter.URI, Encounter.BUILDER);
    public static final Manager<Patient> PATIENTS = new Manager<>(Patient.URI, Patient.BUILDER);
    public static final Manager<Physician> PHYSICIANS = new Manager<>(Physician.URI, Physician.BUILDER);
    public static final Manager<Record> RECORDS = new Manager<>(Record.URI, Record.BUILDER);
    public static final Manager<Test> TESTS = new Manager<>(Test.URI, Test.BUILDER);
    public static final Manager<Visit> VISITS = new Manager<>(Visit.URI, Visit.BUILDER);
    public static final Manager<Medication> MEDICATIONS = new Manager<>(Medication.URI, Medication.BUILDER);
    // Category Classes
    public static final Manager<EncounterCategory> ENCOUNTER_CATEGORIES = new Manager<>(EncounterCategory.URI, EncounterCategory.BUILDER);
    public static final Manager<RecordCategory> RECORD_CATEGORIES = new Manager<>(RecordCategory.URI, RecordCategory.BUILDER);
    public static final Manager<TestCategory> TEST_CATEGORIES = new Manager<>(TestCategory.URI, TestCategory.BUILDER);
    public static final Manager<VisitCategory> VISIT_CATEGORIES = new Manager<>(VisitCategory.URI, VisitCategory.BUILDER);

    protected static String getType(Context context, Uri uri) {
        return context.getContentResolver().getType(uri);
    }

    /**
     * Returns a {@link android.database.Cursor Cursor} with one and only one
     * row where the value stored in the {@link com.smarking.mhealthsyria.app.model.Model#UUID Model.UUID}
     * column.
     *
     * @param context  The context
     * @param modelUri A directory content style Uri
     * @param uuid     The unique id of a model
     * @return A {@link android.database.Cursor Cursor}
     * @throws ObjectDoesNotExistException
     * @throws MultipleObjectsExistException
     */
    public static Cursor get(Context context, Uri modelUri, String uuid) throws ObjectDoesNotExistException, MultipleObjectsExistException {
        Log.d(TAG, "get(Context,Uri modelUri,String )");
        Uri uri = Uri.withAppendedPath(modelUri, uuid.toLowerCase());
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            Log.e(TAG, "Object doesn't exist! uri:" + uri);
            throw new ObjectDoesNotExistException(getType(context, modelUri) + ":" + uuid);
        } else if (cursor.getCount() > 1) {
            Log.e(TAG, "Multiple objects returned(" + cursor.getCount() + ": " + uri);
            cursor.close();
            throw new MultipleObjectsExistException(getType(context, modelUri) + ":" + uuid);
        } else if (!cursor.moveToFirst()) {
            Log.e(TAG, "Object doesn't exist! uri:" + uri);
            cursor.close();
            throw new ObjectDoesNotExistException(getType(context, modelUri) + ":" + uuid);
        }
        return cursor;
    }


    /**
     * Returns a {@link android.database.Cursor Cursor} which represents a
     * many child models hold a foreign key relationship to a parent.
     *
     * @param context      The context
     * @param relatedUri   The directory style content Uri of the child models
     * @param instanceUUID Unique id of the parent model
     * @param relatedCol   The name of the column in the child where the id is stored
     * @param sortOrder    The order in which the rows are returned
     * @return
     */
    public static Cursor all(Context context, String instanceUUID,
                             Uri relatedUri,
                             String relatedCol,
                             String sortOrder) {
        String selection = String.format(SELECTION_EXPLICIT, relatedCol, instanceUUID);
        Cursor cursor = context.getContentResolver().query(relatedUri, null,
                selection, null, sortOrder);
        return cursor;
    }

    private Models() {
    }
}
