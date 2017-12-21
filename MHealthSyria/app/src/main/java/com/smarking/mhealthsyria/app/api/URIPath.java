package com.smarking.mhealthsyria.app.api;

import com.smarking.mhealthsyria.app.model.Clinic;
import com.smarking.mhealthsyria.app.model.Clinic_Physician;
import com.smarking.mhealthsyria.app.model.Device;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Patient_Physician;
import com.smarking.mhealthsyria.app.model.category.DoseUnitCategory;
import com.smarking.mhealthsyria.app.model.category.EncounterCategory;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.category.IntervalUnitCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationCategory;
import com.smarking.mhealthsyria.app.model.category.MedicationGroupCategory;
import com.smarking.mhealthsyria.app.model.category.RecordCategory;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.category.TestCategory;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.model.category.VisitCategory;
import com.smarking.mhealthsyria.app.model.event.Event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-07.
 */

public enum URIPath {
    PATIENT(10, Patient.TABLE, Patient.URI.getPath() + "/*"),
    PATIENTS(11, Patient.TABLE, Patient.URI.getPath()),

    PHYSICIAN(20, Physician.TABLE, Physician.URI.getPath() + "/*"),
    PHYSICIANS(21, Physician.TABLE, Physician.URI.getPath()),

    VISITCATEGORY(30, VisitCategory.TABLE, VisitCategory.URI.getPath() + "/*"),
    VISITCATEGORIES(31, VisitCategory.TABLE, VisitCategory.URI.getPath()),

    RECORDCATEGORY(40, RecordCategory.TABLE, RecordCategory.URI.getPath() + "/*"),
    RECORDCATEGORIES(41, RecordCategory.TABLE, RecordCategory.URI.getPath()),

    TESTCATEGORY(50, TestCategory.TABLE, TestCategory.URI.getPath() + "/*"),
    TESTCATEGORIES(51, TestCategory.TABLE, TestCategory.URI.getPath()),

    ENCOUNTERCATEGORY(60, EncounterCategory.TABLE, EncounterCategory.URI.getPath() + "/*"),
    ENCOUNTERCATEGORIES(61, EncounterCategory.TABLE, EncounterCategory.URI.getPath()),

    VISIT(70, Visit.TABLE, Visit.URI.getPath() + "/*"),
    VISITS(71, Visit.TABLE, Visit.URI.getPath()),

    RECORD(80, Record.TABLE, Record.URI.getPath() + "/*"),
    RECORDS(81, Record.TABLE, Record.URI.getPath()),

    TEST(90, Test.TABLE, Test.URI.getPath() + "/*"),
    TESTS(91, Test.TABLE, Test.URI.getPath()),

    ENCOUNTER(100, Encounter.TABLE, Encounter.URI.getPath() + "/*"),
    ENCOUNTERS(101, Encounter.TABLE, Encounter.URI.getPath()),

    CLINIC(110, Clinic.TABLE, Clinic.URI.getPath() + "/*"),
    CLINICS(111, Clinic.TABLE, Clinic.URI.getPath()),

    DEVICE(120, Device.TABLE, Device.URI.getPath() + "/*"),
    DEVICES(121, Device.TABLE, Device.URI.getPath()),

    MEDICATION(130, Medication.TABLE, Medication.URI.getPath() + "/*"),
    MEDICATIONS(131, Medication.TABLE, Medication.URI.getPath()),

    MEDICATIONCATEGORY(140, MedicationCategory.TABLE, MedicationCategory.URI.getPath() + "/*"),
    MEDICATIONCATEGORYS(141, MedicationCategory.TABLE, MedicationCategory.URI.getPath()),

    MEDICATIONGROUPCATEGORY(150, MedicationGroupCategory.TABLE, MedicationGroupCategory.URI.getPath() + "/*"),
    MEDICATIONGROUPCATEGORYS(151, MedicationGroupCategory.TABLE, MedicationGroupCategory.URI.getPath()),

    DOSEUNITCATEGORY(160, DoseUnitCategory.TABLE, DoseUnitCategory.URI.getPath() + "/*"),
    DOSEUNITCATEGORYS(161, DoseUnitCategory.TABLE, DoseUnitCategory.URI.getPath()),

    INTERVALUNITCATEGORY(170, IntervalUnitCategory.TABLE, IntervalUnitCategory.URI.getPath() + "/*"),
    INTERVALUNITCATEGORYS(171, IntervalUnitCategory.TABLE, IntervalUnitCategory.URI.getPath()),

    PATIENT_PHYSICIAN(180, Patient_Physician.TABLE, Patient_Physician.URI.getPath() + "/*"),
    PATIENT_PHSYICIANS(181, Patient_Physician.TABLE, Patient_Physician.URI.getPath()),

    CLINIC_PHYSICIAN(190, Clinic_Physician.TABLE, Clinic_Physician.URI.getPath() + "/*"),
    CLINIC_PHSYICIANS(191, Clinic_Physician.TABLE, Clinic_Physician.URI.getPath()),

    EVENT(200, Event.TABLE, Event.URI.getPath() + "/*"),
    EVENTS(201, Event.TABLE, Event.URI.getPath());

    /////////////////////////////////////////////////////////////////

    public static final Map<Integer, URIPath> LOOKUP;
    static {
        Map<Integer, URIPath> lookup = new HashMap<>();
        for (URIPath uriPath : values()) {
            lookup.put(uriPath.id, uriPath);
        }
        LOOKUP = Collections.unmodifiableMap(lookup);
    }

    public final int id;
    public final String table;
    public final String path;

    URIPath(int id, String table, String path) {
        this.id = id;
        this.table = table;
        this.path = path.substring(1); // remove first /
    }

}
