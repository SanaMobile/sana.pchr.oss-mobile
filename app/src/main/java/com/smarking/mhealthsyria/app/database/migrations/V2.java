package com.smarking.mhealthsyria.app.database.migrations;

import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Device;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.model.category.DoseUnitCategory;
import com.smarking.mhealthsyria.app.model.category.EncounterCategory;
import com.smarking.mhealthsyria.app.model.category.IntervalUnitCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationGroupCategory;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.model.category.TestCategory;
import com.smarking.mhealthsyria.app.model.category.VisitCategory;

import net.sqlcipher.database.SQLiteDatabase;


/**
 * Created by Hok Hei Tam on 1/12/2016
 * Adds medication tables
 */
public class V2 extends Migration {

    @Override
    public void apply(SQLiteDatabase db, DatabaseHandler databaseHandler) {

        db.execSQL("CREATE TABLE " + MedicationGroupCategory.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(127, MedicationGroupCategory.DISPLAYNAME) + ")");

        db.execSQL("CREATE TABLE " + DoseUnitCategory.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(127, DoseUnitCategory.DISPLAYNAME) + ")");

        db.execSQL("CREATE TABLE " + IntervalUnitCategory.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(127, IntervalUnitCategory.DISPLAYNAME) + ")");

        db.execSQL("CREATE TABLE " + Medication.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(45, Medication.DOSE) + ", " +
                VARCHAR(45, Medication.ENCOUNTER_UUID, false) + ", " +
                VARCHAR(45, Medication.INTERVAL) + ", " +
                VARCHAR(45, Medication.TIMES) + ", " +
                VARCHAR(45, Medication.CATEGORY_UUID, false) + ", " +
                VARCHAR(45, Medication.DOSE_UNIT) + ", " +
                VARCHAR(45, Medication.END_DATE) + ", " +
                VARCHAR(45, Medication.INTERVAL_UNIT, false) + ")");

        db.execSQL("CREATE TABLE " + MedicationCategory.TABLE + " (" +
                MODEL_COLUMNS +
                VARCHAR(255, MedicationCategory.DISPLAYNAME) + ", " +
                VARCHAR(45, MedicationCategory.DOSE_DEFAULT) + ", " +
                VARCHAR(45, MedicationCategory.GROUP_UUID, false) + ", " +
                VARCHAR(255, MedicationCategory.OTHERNAME) + ", " +
                VARCHAR(45, MedicationCategory.INTERVAL_DEFAULT) + ", " +
                VARCHAR(45, MedicationCategory.DOSE_UNIT_UUID, false) + ", " +
                VARCHAR(45, MedicationCategory.TIMES_DEFAULT) + ", " +
                VARCHAR(45, MedicationCategory.INTERVAL_UNIT_UUID, false) + ")");




    }

    @Override
    public void revert(SQLiteDatabase db, DatabaseHandler databaseHandler) {
        db.execSQL(" DROP TABLE " + IntervalUnitCategory.TABLE);
        db.execSQL(" DROP TABLE " + DoseUnitCategory.TABLE);
        db.execSQL(" DROP TABLE " + MedicationGroupCategory.TABLE);
        db.execSQL(" DROP TABLE " + MedicationCategory.TABLE);
        db.execSQL(" DROP TABLE " + Medication.TABLE);

    }

}
