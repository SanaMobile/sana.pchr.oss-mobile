package com.smarking.mhealthsyria.app.database.migrations;

import com.smarking.mhealthsyria.app.database.DatabaseHandler;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.model.category.DoseUnitCategory;
import com.smarking.mhealthsyria.app.model.category.EncounterCategory;
import com.smarking.mhealthsyria.app.model.category.IntervalUnitCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationGroupCategory;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.model.category.VisitCategory;

import net.sqlcipher.database.SQLiteDatabase;


/**
 * Created by Hok Hei Tam on 1/12/2016
 * Adds medication tables
 */
public class V5 extends Migration {

    @Override
    public void apply(SQLiteDatabase db, DatabaseHandler databaseHandler) {

        db.execSQL("CREATE INDEX " + Test.TABLE + "_" + Encounter.TABLE + "_idx ON " +
                Test.TABLE + " (" + Test.ENCOUNTER_UUID + ")");

        db.execSQL("CREATE INDEX " + Record.TABLE + "_" + Encounter.TABLE + "_idx ON " +
                Record.TABLE + " (" + Record.ENCOUNTER_UUID + ")");

        db.execSQL("CREATE INDEX " + Medication.TABLE + "_" + Encounter.TABLE + "_idx ON " +
                Medication.TABLE + " (" + Medication.ENCOUNTER_UUID + ")");

        db.execSQL("CREATE INDEX " + Encounter.TABLE + "_" + Visit.TABLE + "_idx ON " +
                Encounter.TABLE + " (" + Encounter.VISIT_UUID + ")");
    }

    @Override
    public void revert(SQLiteDatabase db, DatabaseHandler databaseHandler) {


    }

}
