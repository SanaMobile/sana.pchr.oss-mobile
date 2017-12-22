package com.smarking.mhealthsyria.app.database.migrations;

import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Clinic_Physician;
import com.smarking.mhealthsyria.app.model.Device;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Patient_Physician;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.model.category.EncounterCategory;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.model.category.TestCategory;
import com.smarking.mhealthsyria.app.model.category.VisitCategory;

import net.sqlcipher.database.SQLiteDatabase;


/**
 * Created by Saravanan Vaithianathan (svaithia@uwaterloo.ca) on 3/7/2015.
 */
public class V1 extends Migration {

    @Override
    public void apply(SQLiteDatabase db, DatabaseHandler databaseHandler) {
        db.execSQL("CREATE TABLE " + Patient.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(45, Patient.FIRSTNAME, false) + ", " +
                VARCHAR(45, Patient.LASTNAME, false) + ", " +
                VARCHAR(45, Patient.UNHCR) + ", " +
                VARCHAR(45, Patient.BIRTHYEAR) + ", " +
                VARCHAR(45, Patient.BIRTHCITY) + ", " +
                VARCHAR(1, Patient.GENDER) + ", " +
                VARCHAR(45, Patient.PICTURE) + ")");

        db.execSQL("CREATE TABLE " + Visit.TABLE + " (" +
                MODEL_COLUMNS +
                BLOB(Visit.PATIENT_UUID, false) + ", " +
                BLOB(Visit.CATEGORY_UUID, false) + ")");

        db.execSQL("CREATE TABLE " + RecordCategory.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(127, RecordCategory.DISPLAYNAME) + ", " +
                INT(RecordCategory.RECORDTYPE) + ", " +
                INT(RecordCategory.RECORDDATATYPE) + ")");

        db.execSQL("CREATE TABLE " + Physician.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(45, Physician.FIRSTNAME, false) + ", " +
                VARCHAR(45, Physician.LASTNAME, false) + ", " +
                VARCHAR(45, Physician.PICTURE) + ", " +
                VARCHAR(45, Physician.DEVICE_KEY_USER_KEY, true) + ", " +
                VARCHAR(45, Physician.EMAIL) + ", " +
                VARCHAR(45, Physician.PHONE) + ", " +
                VARCHAR(127, Physician.RECOVERY_QUESTION) + ", " +
                BLOB(Physician.SALT) + ", " +
                BLOB(Physician.RECOVERY_KEY) + "," +
                BLOB(Physician.HASHEDPIN_USER_KEY) + " )");

        db.execSQL("CREATE TABLE " + VisitCategory.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(127, VisitCategory.DISPLAYNAME) + ")");

        db.execSQL("CREATE TABLE " + EncounterCategory.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(127, EncounterCategory.DISPLAYNAME) + ")");

        db.execSQL("CREATE TABLE " + Encounter.TABLE + " (" +
                MODEL_COLUMNS +
                BLOB(Encounter.PHYSICIAN_UUID, false) + ", " +
                BLOB(Encounter.DEVICE_UUID, false) + ", " +
                BLOB(Encounter.CLINIC_UUID, false) + ", " +
                BLOB(Encounter.VISIT_UUID, false) + ", " +
                BLOB(Test.CATEGORY_UUID, false) + ")");

        db.execSQL("CREATE TABLE " + Record.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(45, Record.VALUE) + ", " +
                TEXT(Record.COMMENT) + ", " +
                BLOB(Record.CATEGORY_UUID, false) + ", " +
                BLOB(Record.ENCOUNTER_UUID, false) + ")");

        db.execSQL("CREATE TABLE " + TestCategory.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(127, TestCategory.DISPLAYNAME) + ", " +
                INT(TestCategory.RESULTTYPE) + ", " +
                REAL(TestCategory.RESULTMIN) + ", " +
                REAL(TestCategory.RESULTMAX) + ", " +
                INT(TestCategory.PRIORITY) + ", " +
                VARCHAR(1023, TestCategory.RESULTUNITS) + ")");

        db.execSQL("CREATE TABLE " + Test.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(45, Test.RESULT) + ", " +
                BLOB(Test.CATEGORY_UUID, false) + ", " +
                BLOB(Test.ENCOUNTER_UUID, false) + ")");

        db.execSQL("CREATE TABLE " + Clinic.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(45, Clinic.NAME) + ", " +
                REAL(Clinic.LONGITUDE) + ", " +
                REAL(Clinic.LATITUDE) + " )");

        db.execSQL("CREATE TABLE " + Device.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(45, Device.NAME) + ", " +
                VARCHAR(45, Device.CLINICUUID, false) + ", " +
                VARCHAR(45, Device.DEVICEMAC, false) + " )");

        db.execSQL("CREATE TABLE " + Clinic_Physician.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(45, Clinic_Physician.CLINIC, false) + ", " +
                VARCHAR(45, Clinic_Physician.PHYSICIAN, false) + " )");

        db.execSQL("CREATE TABLE " + Patient_Physician.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(45, Patient_Physician.PATIENT, false) + ", " +
                VARCHAR(45, Patient_Physician.PHYSICIAN, false) + " )");

    }

    @Override
    public void revert(SQLiteDatabase db, DatabaseHandler databaseHandler) {
        db.execSQL(" DROP TABLE " + Patient.TABLE);
        db.execSQL(" DROP TABLE " + Visit.TABLE);
        db.execSQL(" DROP TABLE " + RecordCategory.TABLE);
        db.execSQL(" DROP TABLE " + Physician.TABLE);
        db.execSQL(" DROP TABLE " + VisitCategory.TABLE);
        db.execSQL(" DROP TABLE " + Encounter.TABLE);
        db.execSQL(" DROP TABLE " + EncounterCategory.TABLE);
        db.execSQL(" DROP TABLE " + Record.TABLE);
        db.execSQL(" DROP TABLE " + TestCategory.TABLE);
        db.execSQL(" DROP TABLE " + Test.TABLE);
        db.execSQL(" DROP TABLE " + Clinic.TABLE);
        db.execSQL(" DROP TABLE " + Device.TABLE);
        db.execSQL(" DROP TABLE " + Patient_Physician.TABLE);
        db.execSQL(" DROP TABLE " + Clinic_Physician.TABLE);
    }

}
