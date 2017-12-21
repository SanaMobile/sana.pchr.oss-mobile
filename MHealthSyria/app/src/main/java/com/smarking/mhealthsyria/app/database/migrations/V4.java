package com.smarking.mhealthsyria.app.database.migrations;

import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Physician;
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
public class V4 extends Migration {

    @Override
    public void apply(SQLiteDatabase db, DatabaseHandler databaseHandler) {

        db.execSQL("ALTER TABLE " + MedicationGroupCategory.TABLE + " ADD COLUMN " +
                INT(MedicationGroupCategory.PRIORITY));

        db.execSQL("ALTER TABLE " + MedicationCategory.TABLE + " ADD COLUMN " +
                INT(MedicationCategory.PRIORITY));

        db.execSQL("ALTER TABLE " + DoseUnitCategory.TABLE + " ADD COLUMN " +
                INT(DoseUnitCategory.PRIORITY));

        db.execSQL("ALTER TABLE " + IntervalUnitCategory.TABLE + " ADD COLUMN " +
                INT(IntervalUnitCategory.PRIORITY));

        db.execSQL("ALTER TABLE " + RecordCategory.TABLE + " ADD COLUMN " +
                INT(RecordCategory.PRIORITY));

        db.execSQL("ALTER TABLE " + EncounterCategory.TABLE + " ADD COLUMN " +
                INT(EncounterCategory.PRIORITY));

        db.execSQL("ALTER TABLE " + VisitCategory.TABLE + " ADD COLUMN " +
                INT(VisitCategory.PRIORITY));
    }

    @Override
    public void revert(SQLiteDatabase db, DatabaseHandler databaseHandler) {


    }

}
