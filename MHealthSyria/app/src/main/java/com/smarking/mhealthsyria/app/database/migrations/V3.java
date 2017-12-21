package com.smarking.mhealthsyria.app.database.migrations;

import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.model.category.DoseUnitCategory;
import com.smarking.mhealthsyria.app.model.category.IntervalUnitCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationGroupCategory;
import com.smarking.mhealthsyria.app.model.category.*;

import net.sqlcipher.database.SQLiteDatabase;


/**
 * Created by Hok Hei Tam on 1/12/2016
 * Adds medication tables
 */
public class V3 extends Migration {

    @Override
    public void apply(SQLiteDatabase db, DatabaseHandler databaseHandler) {

        db.execSQL("ALTER TABLE " + MedicationGroupCategory.TABLE + " ADD COLUMN " +
                VARCHAR(127, MedicationGroupCategory.DISPLAYNAMEAR));

        db.execSQL("ALTER TABLE " + MedicationCategory.TABLE + " ADD COLUMN " +
                VARCHAR(127, MedicationCategory.DISPLAYNAMEAR));

        db.execSQL("ALTER TABLE " + MedicationCategory.TABLE + " ADD COLUMN " +
                VARCHAR(255, MedicationCategory.INTERACTION_WARNING));

        db.execSQL("ALTER TABLE " + DoseUnitCategory.TABLE + " ADD COLUMN " +
                VARCHAR(127, DoseUnitCategory.DISPLAYNAMEAR));

        db.execSQL("ALTER TABLE " + IntervalUnitCategory.TABLE + " ADD COLUMN " +
                VARCHAR(127, IntervalUnitCategory.DISPLAYNAMEAR));

        db.execSQL("ALTER TABLE " + TestCategory.TABLE + " ADD COLUMN " +
                VARCHAR(127, TestCategory.DISPLAYNAMEAR));

        db.execSQL("ALTER TABLE " + TestCategory.TABLE + " ADD COLUMN " +
                VARCHAR(127, TestCategory.RESULTUNITSAR));

        db.execSQL("ALTER TABLE " + RecordCategory.TABLE + " ADD COLUMN " +
                VARCHAR(127, RecordCategory.DISPLAYNAMEAR));

        db.execSQL("ALTER TABLE " + EncounterCategory.TABLE + " ADD COLUMN " +
                VARCHAR(127, EncounterCategory.DISPLAYNAMEAR));

        db.execSQL("ALTER TABLE " + VisitCategory.TABLE + " ADD COLUMN " +
                VARCHAR(127, VisitCategory.DISPLAYNAMEAR));

        db.execSQL("ALTER TABLE " + Patient.TABLE + " ADD COLUMN" +
                VARCHAR(25, Patient.PHONE));

        db.execSQL("ALTER TABLE " + Patient.TABLE + " ADD COLUMN" +
                VARCHAR(45, Patient.PROVIDERID));

        db.execSQL("ALTER TABLE " + Physician.TABLE + " ADD COLUMN " +
                VARCHAR(1, Physician.TYPE));

        db.execSQL("ALTER TABLE " + Medication.TABLE + " ADD COLUMN " +
                VARCHAR(127, Medication.COMMENT));

        db.execSQL("ALTER TABLE " + Clinic.TABLE + " ADD COLUMN " +
                VARCHAR(2, Clinic.LANGUAGE));

    }

    @Override
    public void revert(SQLiteDatabase db, DatabaseHandler databaseHandler) {

    }

}
