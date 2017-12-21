package com.smarking.mhealthsyria.app.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.nfc.Tag;
import android.test.suitebuilder.annotation.Smoke;
import android.util.Log;
import android.util.SparseArray;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.SanaApplication;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Model;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.category.MedicationGroupCategory;
import com.smarking.mhealthsyria.app.model.category.TestCategory;

import org.spongycastle.asn1.cmp.CAKeyUpdAnnContent;
import org.spongycastle.asn1.esf.OtherHash;

import java.lang.reflect.WildcardType;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by tamhok on 1/13/16.
 * <p/>
 * Makes recommendation for decision support system
 */
public class Recommender {
    private static final String TAG = Recommender.class.getSimpleName();

    private static Recommender instance;

    public enum MedMod {
        STOP, CHANGE
    }

    private final Patient mPatient;
    private Encounter mEncounter;
    private List<Test> mTests;
    private List<Medication> mMeds;
    private List<Record> mRecords;
    private List<Medication> currentMedList;
    private HashMap<String, Test> currentTestList;

    private List<Record> mRecommendation = new ArrayList<>();
    private List<MedicationGroupCategory> mMedRecAdd = new ArrayList<>();
    private HashMap<Medication, MedMod> mMedRecMod = new HashMap<>();
    private SparseArray<List<Medication>> currentMeds = new SparseArray<>();

    public int diabetes_status = 0;
    public int diabetes_level = 0;
    public int diabetes_control = -1;
    public int hypertension_level = 0;
    public int hypertension_status = 0;
    public int hypertension_control = -1;
    public int lipid_level = 0;
    public int lipid_status = 0;
    public int lipid_control = -1;
    public int ascvd_risk = 0;


    private int age;
    private Context context = SanaApplication.getAppContext();
    private Resources r = context.getResources();
    private Date followup = new Date(0);

    public static HashMap<String, Integer> testUUIDs;
    public static HashMap<String, Integer> medicationCategoryUUIDs;
    public static HashMap<String, Integer> recordUUIDs;
    public static SparseArray<String> recordUUIDLookup;
    public static SparseArray<String> testUUIDLookup;
    public static SparseArray<String> medicationCategoryUUIDLookup;

    public static SparseArray<String> medDefaults;
    public static SparseArray<List<Float>> testLevels;

    /*
     * ASCVD Risk interpretation
     *
     * [Cholesterol][Age][Diabetes][Smoking][BP]
     * Cholesterol: 0:4, 1:5, 2:6, 3:7, 4:8
     * Age: 0:40, 1:50, 2:60; 3:70
     * Diabetes: 0:No 1:Yes
     * Smoking: 0:No, 1:Yes
     * BP: 0:120, 1:140, 2:160, 3:180
     */
    private static int[][][][][] ascvd_risk_m_c;
    private static int[][][][] ascvd_risk_m;
    private static int[][][][][] ascvd_risk_f_c;
    private static int[][][][] ascvd_risk_f;

    private SparseArray<List<Test>> testList = new SparseArray<>();
    private SparseArray<List<Medication>> medList = new SparseArray<>();
    private SparseArray<List<Record>> recordList = new SparseArray<>();

    public static Comparator<Model> compCreate = new Comparator<Model>() {
        @Override
        public int compare(Model m1, Model m2) {
            Date d1, d2;
            d1 = m1.getCreatedDate();
            d2 = m2.getCreatedDate();
            if (d1 == null) {
                return 1;
            } else if (d2 == null) {
                return -1;
            }
            if (d1.after(d2)) {
                return -1;
            } else if (d1.equals(d2)) {
                return 0;
            } else {
                return 1;
            }
        }
    };

    public static Callable<Boolean> loadFromResources = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            final Context c = SanaApplication.getAppContext();
            testUUIDLookup = parseStringArray(R.array.test, c);
            medicationCategoryUUIDLookup = parseStringArray(R.array.medication, c);
            testLevels = parseLevelArray(R.array.levels, c);
            recordUUIDLookup = parseStringArray(R.array.record, c);
            testUUIDs = createLookup(testUUIDLookup);
            recordUUIDs = createLookup(recordUUIDLookup);
            medicationCategoryUUIDs = createLookup(medicationCategoryUUIDLookup);
            medDefaults = parseStringArray(R.array.med_defaults, c);

            ascvd_risk_f = loadASCVDRiskNC(R.array.ascvd_risk_nc_f, c);
            ascvd_risk_f_c = loadASCVDRisk(R.array.ascvd_risk_yc_f, c);
            ascvd_risk_m = loadASCVDRiskNC(R.array.ascvd_risk_nc_m, c);
            ascvd_risk_m_c = loadASCVDRisk(R.array.ascvd_risk_yc_m, c);
            return true;
        }
    };

    public static int[][][] parseASCVDRiskString(String entry) {
        int[][][] output = new int[2][2][4];
        String[] diabetesSplit = entry.split("@", 2);
        for (int dctr = 0; dctr < 2; dctr++) {
            String[] smokeSplit = diabetesSplit[dctr].split(";", 2);
            for (int sctr = 0; sctr < 2; sctr++) {
                String[] bpSplit = smokeSplit[sctr].split(",", 4);
                for (int bctr = 0; bctr < 4; bctr++) {
                    output[dctr][sctr][bctr] = Integer.parseInt(bpSplit[bctr]);
                }
            }
        }
        return output;
    }

    public static int[][][][] loadASCVDRiskNC(int stringArrayResourceId, Context c) {
        String[] stringArray = c.getResources().getStringArray(stringArrayResourceId);
        int[][][][] outputArray = new int[4][2][2][4];
        int[][][] out;
        for (String entry : stringArray) {
            String[] splitResult = entry.split("\\|", 2);
            String[] cholSplit = splitResult[0].split(",");
            out = parseASCVDRiskString(splitResult[1]);
            outputArray[Integer.parseInt(cholSplit[0])] = out;
        }
        return outputArray;
    }

    public static int[][][][][] loadASCVDRisk(int stringArrayResourceId, Context c) {
        String[] stringArray = c.getResources().getStringArray(stringArrayResourceId);
        int[][][][][] outputArray = new int[5][4][2][2][4];
        int[][][] out;
        for (String entry : stringArray) {
            String[] splitResult = entry.split("\\|", 2);
            String[] cholSplit = splitResult[0].split(",");
            out = parseASCVDRiskString(splitResult[1]);
            outputArray[Integer.parseInt(cholSplit[1])][Integer.parseInt(cholSplit[0])] = out;
        }
        return outputArray;
    }

    public static SparseArray<List<Float>> parseLevelArray(int stringArrayResourceId, Context c) {
        String[] stringArray = c.getResources().getStringArray(stringArrayResourceId);
        SparseArray<List<Float>> outputArray = new SparseArray<>(stringArray.length);
        for (String entry : stringArray) {
            String[] splitResult = entry.split("\\|", 2);
            String[] splitResult2 = splitResult[1].split(":");
            List<Float> lims = new ArrayList<>(splitResult2.length);
            for (String str : splitResult2) {
                lims.add(Float.parseFloat(str));
            }
            outputArray.put(Integer.parseInt(splitResult[0]), lims);
        }
        return outputArray;
    }

    public static HashMap<String, Integer> createLookup(SparseArray<String> in) {
        HashMap<String, Integer> outputArray = new HashMap<>(in.size());
        int key;
        for (int i = 0; i < in.size(); i++) {
            key = in.keyAt(i);
            outputArray.put(in.get(key), key);
        }
        return outputArray;
    }

    public static SparseArray<String> parseStringArray(int stringArrayResourceId, Context c) {
        String[] stringArray = c.getResources().getStringArray(stringArrayResourceId);
        SparseArray<String> outputArray = new SparseArray<>();
        for (String entry : stringArray) {
            String[] splitResult = entry.split("\\|", 2);
            outputArray.put(Integer.parseInt(splitResult[0]), splitResult[1]);
        }
        return outputArray;
    }

    /*
     * Returns a SparseArray of Medication objects from the list given, sorted by date so that
     * the newest is first. ID's corresponding to UUIDs of MedicationGroupCategories are defined in
     * res/values/recommend_fields.xml and loaded by loadFromResource upon initialization.
     */
    public SparseArray<List<Medication>> getRelevantMedication(List<Medication> meds) {
        SparseArray<List<Medication>> outputArray = new SparseArray<>(medicationCategoryUUIDs.keySet().size());
        String med_uuid;
        int curType;
        MedicationGroupCategory tempcat;
        for (Integer cat : medicationCategoryUUIDs.values()) {
            outputArray.put(cat, new ArrayList<Medication>());
        }

        for (Medication med : meds) {
            tempcat = med.getMedication(context).getMedicationCategory(context);
            med_uuid = tempcat.getUUID();

            if (medicationCategoryUUIDs.containsKey(med_uuid)) {
                curType = medicationCategoryUUIDs.get(med_uuid);
                outputArray.get(curType).add(med);
            }
        }

        for (Integer cat : medicationCategoryUUIDs.values()) {
            Collections.sort(outputArray.get(cat), compCreate);
        }
        return outputArray;
    }

    /*
 * Returns a SparseArray of Record objects from the list given, sorted by date so that
 * the newest is first. ID's corresponding to UUIDs are defined in res/values/recommend_fields.xml
 * and loaded by loadFromResource upon initialization.
 */
    public SparseArray<List<Record>> getRelevantRecord(List<Record> records) {
        SparseArray<List<Record>> outputArray = new SparseArray<>(recordUUIDs.keySet().size());
        String record_uuid;
        Integer curType;

        for (Integer cat : recordUUIDs.values()) {
            outputArray.put(cat, new ArrayList<Record>());
        }

        for (Record record : records) {
            record_uuid = record.getCategory_uuid();

            if (recordUUIDs.containsKey(record_uuid)) {
                curType = recordUUIDs.get(record_uuid);
                outputArray.get(curType).add(record);
            }
        }

        for (Integer cat : recordUUIDs.values()) {
            Collections.sort(outputArray.get(cat), compCreate);
        }
        return outputArray;
    }
    
    /*
     * Returns a SparseArray of Test objects from the list given, sorted by date so that
     * the newest is first. ID's corresponding to UUIDs are defined in res/values/recommend_fields.xml
     * and loaded by loadFromResource upon initialization.
     */
    public SparseArray<List<Test>> getRelevantTest(List<Test> tests) {
        SparseArray<List<Test>> outputArray = new SparseArray<>(testUUIDs.keySet().size());
        String test_uuid;
        Integer curType;

        for (Integer cat : testUUIDs.values()) {
            outputArray.put(cat, new ArrayList<Test>());
        }

        for (Test test : tests) {
            test_uuid = test.getCategory_uuid();

            if (testUUIDs.containsKey(test_uuid)) {
                curType = testUUIDs.get(test_uuid);
                outputArray.get(curType).add(test);
            }
        }

        for (Integer cat : testUUIDs.values()) {
            Collections.sort(outputArray.get(cat), compCreate);
        }
        return outputArray;
    }

    //AKA one hour ago
    private static Date thisVisit() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        return cal.getTime();
    }

    private static Date weeksBefore(int i) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -i);
        return cal.getTime();
    }

    private static Date weeksBefore(int i, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.WEEK_OF_YEAR, -i);
        return cal.getTime();
    }

    private static Date monthsBefore(int i) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -i);
        return cal.getTime();
    }

    private static Date monthsBefore(int i, Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MONTH, -i);
        return cal.getTime();
    }

    public void addTestHash(Test test) {
        String test_uuid;
        int curType;
        test_uuid = test.getCategory_uuid();

        if (testUUIDs.containsKey(test_uuid)) {
            curType = testUUIDs.get(test_uuid);
            testList.get(curType).add(0, test);
        }
    }

    public void addMedicationHash(Medication med) {
        MedicationGroupCategory tempcat = med.getMedication(context).getMedicationCategory(context);
        String med_uuid = tempcat.getUUID();
        int curType;

        if (medicationCategoryUUIDs.containsKey(med_uuid)) {
            curType = medicationCategoryUUIDs.get(med_uuid);
            medList.get(curType).add(med);
        }

        mMeds.add(med);
    }

    public static Recommender getInstance() {
        return instance;
    }

    public static void start(Patient patient) {
        instance = new Recommender(patient);
    }

    private Recommender(Patient patient) {
        mPatient = patient;
        mTests = new ArrayList<>();
        mMeds = new ArrayList<>();
        mRecords = new ArrayList<>();
        currentMedList = new ArrayList<>();
        currentTestList = new HashMap<>();
    }

    public void setEncounter(Encounter encounter) {
        mEncounter = encounter;
    }

    public void addTest(Test tests) {
        mTests.add(tests);
    }

    public void addMed(Medication meds) {
        mMeds.add(meds);
    }

    public void addRec(Record recs) {
        mRecords.add(recs);
    }

    public void addCurMed(Medication curMeds) {
        currentMedList.add(curMeds);
    }
    
    public void addTestsAll(List<Test> tests) {
        mTests.addAll(tests);
    }

    public void addMedsAll(List<Medication> meds) {
        mMeds.addAll(meds);
    }

    public void addRecsAll(List<Record> recs) {
        mRecords.addAll(recs);
    }

    public void addCurMedsAll(List<Medication> curMeds) {
        currentMedList.addAll(curMeds);
    }

    private Recommender(Patient patient, List<Test> tests, List<Medication> meds, List<Medication> currentMeds, List<Record> records, Encounter encounter) {
        mPatient = patient;
        mTests = tests;
        mMeds = meds;
        mRecords = records;
        mEncounter = encounter;
        this.currentMedList = currentMeds;

        compute_lists();

        recompute();
    }

    public void reset_main_lists() {
        mTests = new ArrayList<>();
        mMeds = new ArrayList<>();
        mRecords = new ArrayList<>();
    }

    public String getUUID(SparseArray<String> map, int i) {
        return map.get(getInt(i));
    }

    public int getInt(int i) {
        return context.getResources().getInteger(i);
    }

    public String getString(int i) {
        return context.getString(i);
    }

    public void write_level_tests(Context context) {
        List<ContentValues> summaries = new ArrayList<>(4);
        if (diabetes_level > 0 && diabetes_control > -1) {
            summaries.add(new Test(Integer.toString(Math.min(diabetes_status, 2)),
                    testUUIDLookup.get(getInt(R.integer.diabetes_lvl)),
                    mEncounter).putContentValues());
        }
        if (lipid_level > 0 && lipid_control > -1) {
            summaries.add(new Test(Integer.toString(Math.min(lipid_status, 2)),
                    testUUIDLookup.get(getInt(R.integer.dyslipidemia_lvl)),
                    mEncounter).putContentValues());
        }
        if (hypertension_level > 0 && hypertension_control > -1) {
            summaries.add(new Test(Integer.toString(Math.min(hypertension_status, 2)),
                    testUUIDLookup.get(getInt(R.integer.hypertension_lvl)),
                    mEncounter).putContentValues());
        }
        if (ascvd_risk > -1) {
            summaries.add(new Test(Integer.toString(Math.min(ascvd_risk, 4)),
                    testUUIDLookup.get(getInt(R.integer.ascvd_lvl)),
                    mEncounter).putContentValues());
        }
        if (summaries != null && !summaries.isEmpty()) {
            ContentValues[] arr = summaries.toArray(new ContentValues[summaries.size()]);
            context.getContentResolver().bulkInsert(Test.URI, arr);
        }
    }

    public void reset_cur_test_list() {
        currentTestList = new HashMap<>();
    }

    public void reset_cur_med_list() {
        currentMedList = new ArrayList<>();
    }

    public void compute_lists() {
        this.currentMeds = getRelevantMedication(currentMedList);

        testList = getRelevantTest(mTests);
        for (Test test : currentTestList.values())
            addTestHash(test);

        medList = getRelevantMedication(mMeds);
        recordList = getRelevantRecord(mRecords);

    }

    public void recompute() {
        compute_basics();
        compute_diabetes();
        compute_ascvd_risk();
        compute_hypertension();
        compute_dyslipidemia();
        make_recs();
    }

    public void clear_recs() {
        mRecommendation = new ArrayList<>();
        mMedRecMod = new HashMap<>();
        mMedRecAdd = new ArrayList<>();
    }

    public void add_to_cur_test_list(Test test) {
        currentTestList.put(test.getCategory_uuid(), test);
    }

    public List<MedicationGroupCategory> getRecommendedMedications() {
        return mMedRecAdd;
    }

    public HashMap<Medication, MedMod> getChangedMedications() {
        return mMedRecMod;
    }

    public List<Record> getRecommendation() {
        return mRecommendation;
    }

    //Note, ints here are actual R.int ints, not the getInt ints
    private void makeRecBinary(int testId, int[] recordId, String[] val, String[] comm, float limit, int greater, int lesser, int def) {

        testId = getInt(testId);
        List<Test> tempList;
        tempList = testList.get(testId);
        float result;
        try {
            if (!tempList.isEmpty()) {
                result = Float.parseFloat(tempList.get(0).getResult());
                if (result >= limit) {
                    if (recordId[greater] > -1) {
                        mRecommendation.add(new Record(val[greater], comm[greater], recordUUIDLookup.get(recordId[greater]), mEncounter));
                    }
                } else {
                    if (recordId[lesser] > -1) {
                        mRecommendation.add(new Record(val[lesser], comm[lesser], recordUUIDLookup.get(recordId[lesser]), mEncounter));
                    }
                }
            } else if (recordId[def] > -1) {
                mRecommendation.add(new Record(val[def], comm[def], recordUUIDLookup.get(recordId[def]), mEncounter));
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Number Format Exception: " + tempList.get(0).getResult());
        }
    }

    private void compute_basics() {
        age = Calendar.getInstance().get(Calendar.YEAR) - Integer.parseInt(mPatient.getBirthYear());

    }

    private void make_recs() {
        // Parse dietary modifications
        makeRecBinary(R.integer.Vegetableconsumption, new int[]{getInt(R.integer.dietmod), -1},
                new String[]{context.getString(R.string.diet_mod_val), ""},
                new String[]{context.getString(R.string.diet_mod_com), ""}, 1, 0, 1, 0);

        makeRecBinary(R.integer.Exercise30minsday, new int[]{getInt(R.integer.exercisemod), -1},
                new String[]{context.getString(R.string.exercise_mod_val), ""},
                new String[]{context.getString(R.string.exercise_mod_com), ""}, 1, 1, 0, 0);

        makeRecBinary(R.integer.Smoking, new int[]{getInt(R.integer.quitsmoke), -1},
                new String[]{context.getString(R.string.smoking_mod_val), ""},
                new String[]{context.getString(R.string.smoking_mod_com), ""}, 2, 0, 1, 1);

        makeRecBinary(R.integer.Alcoholconsumption, new int[]{getInt(R.integer.alcoholmod), -1},
                new String[]{context.getString(R.string.alcohol_mod_val), ""},
                new String[]{context.getString(R.string.alcohol_mod_com), ""}, 1, 1, 0, 1);

        // Check vaccinations: if in tests or record

        int month = Calendar.getInstance().get(Calendar.MONTH);

        if (month > Calendar.AUGUST && month < Calendar.FEBRUARY) {
            mRecommendation.add(new Record(context.getString(R.string.infl_vac_val), "",
                    recordUUIDLookup.get(getInt(R.integer.vacc_infl)), mEncounter.uuid,
                    context.getString(R.string.infl_vac_val_ar), ""));
        }
        mRecommendation.add(new Record(context.getString(R.string.pneum_vac_val), "",
                recordUUIDLookup.get(getInt(R.integer.vacc_pneum)), mEncounter.uuid,
                context.getString(R.string.pneum_vac_val_ar), ""));

        //Assign follow up visits
        if (diabetes_level > 0 || hypertension_level > 0) {
            if (followup.before(Calendar.getInstance().getTime())) {
                if (diabetes_status > 1 || hypertension_status > 1) {
                    mRecommendation.add(new Record(Model.LEAST_DATE.format(monthsBefore(-3)), "",
                            getUUID(recordUUIDLookup, R.integer.followup), mEncounter));
                } else {
                    mRecommendation.add(new Record(Model.LEAST_DATE.format(monthsBefore(-6)), "",
                            getUUID(recordUUIDLookup, R.integer.followup), mEncounter));
                }
            } else {
                mRecommendation.add(new Record(Model.LEAST_DATE.format(followup), "",
                        getUUID(recordUUIDLookup, R.integer.followup), mEncounter));
            }

            //Recommend urine test
            if (!hasTestWithin(R.integer.Spoturinemicroalbumin, monthsBefore(12))) {
                addTestRec(R.integer.Spoturinemicroalbumin, "");
            }

            if (!hasTestWithin(R.integer.Cholesterol, monthsBefore(12))) {
                addTestRec(R.integer.Cholesterol, "");
            }

            if (!hasTestWithin(R.integer.LDL, monthsBefore(12))) {
                addTestRec(R.integer.LDL, "");
            }

        }
    }

    /*
     * Adds to the list if not empty
     */

    private void addIfNotNull(List<Test> result, SparseArray<List<Test>> list, int argument, int index) {
        if (list.get(argument) != null) {
            if (list.get(argument).size() < (index + 1)) {
                return;
            } else {
                result.add(list.get(argument).get(index));
            }
        }
    }

    /*
     * Takes sorted list of tests and gets the tests prior to time given
     */
    private List<? extends Model> getWithinDate(List<? extends Model> tests, Date endDate) {
        List<Model> output = new ArrayList<>();
        if (tests != null) {
            for (int i = 0; i < tests.size(); i++) {
                if (tests.get(i).getCreatedDate().after(endDate)) {
                    output.add(tests.get(i));
                } else {
                    return output;
                }
            }
            return output;
        } else {
            return new ArrayList<>();
        }
    }

    public int checkTestLevel(float result, int id) {
        if (testLevels.get(getInt(id)) != null && result > 0) {
            List<Float> levels = testLevels.get(getInt(id));
            int i;
            for (i = 0; i < levels.size(); i = i + 1) {
                if (levels.get(i) > result) {
                    return i;
                }
            }
            return i;
        }
        return -1;
    }

    public static int checkTestLevel(Test test) {
        try {
            if (testUUIDs.containsKey(test.getCategory_uuid())) {
                int cat = testUUIDs.get(test.getCategory_uuid());
                if (testLevels.get(cat) != null) {
                    List<Float> levels = testLevels.get(cat);
                    try {
                        float result = Float.parseFloat(test.getResult());
                        int i;
                        for (i = 0; i < levels.size(); i = i + 1) {
                            if (levels.get(i) > result) {
                                return i;
                            }
                        }
                        return i;
                    } catch (NumberFormatException | NullPointerException exception) {
                        Log.e(TAG, "Parsing problem: " + test.getResult() + ", " + exception.getClass().getSimpleName());
                        return -1;
                    }
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "Null Pointer: " + e.getMessage());
        }
        return -1;
    }

    public int getNumberAdjustments(int id) {
        if (medList.get(getInt(id)) != null)
            return medList.get(getInt(id)).size();
        return -1;
    }

    public Date getLastDrugChange(int[] ids) {
        Date lastDate = new Date(0);
        Date other;
        List<Medication> templlist;
        for (int id : ids) {
            templlist = medList.get(getInt(id));
            if (templlist != null && !templlist.isEmpty()) {
                Collections.sort(templlist, compCreate);
                other = templlist.get(0).getCreatedDate();
                if (other.after(lastDate))
                    lastDate = other;
            }
        }
        return lastDate;
    }

    public void addTestRec(int id, String comment) {
        addTestRec(id, comment, "");
    }

    public void addTestRec(int id, String comment, String commentAr) {
        int pos = hasAssignedTest(id, thisVisit());
        if (pos < 0) {
            TestCategory cat = TestCategory.get(context, testUUIDLookup.get(getInt(id)));
            mRecommendation.add(new Record(cat.getDisplayName(),
                    comment, recordUUIDLookup.get(getInt(R.integer.test)), mEncounter.uuid, cat.getDisplayNameAr(), commentAr));
        } else {
            Record rec = recordList.get(getInt(R.integer.test)).get(pos);
            rec.setComment(String.format("%s %s", rec.getComment(), comment));
            recordList.get(getInt(R.integer.test)).set(pos, rec);
        }
    }

    private void compute_diabetes() {
        //0 = No Diabetes, 1 = Pre-Diabetes, 2 = Full-blown diabetes, 3 = Uncontrolled diabetes
        Date lastDrugDate = null;

        int[] levels = new int[4];
        int[] tests = {R.integer.FastingBloodGlucose, R.integer.RandomBloodGlucose, R.integer.HbA1c,
                R.integer.OralGlucoseToleranceTest};

        diabetes_status = getMaxLevel(tests, monthsBefore(1), 1, 10);

        if (hasValTest(R.integer.Diagnoseddiabetes, 1)) {
            diabetes_level = 1;
        }
        if (hasMed(R.integer.metformin)) {
            diabetes_level = 2;
        }
        if (!medList.get(getInt(R.integer.sulfonylurea)).isEmpty()) {
            diabetes_level = 3;
        }
        if (!medList.get(getInt(R.integer.insulin)).isEmpty()) {
            diabetes_level = 4;
        }

        lastDrugDate = getLastDrugChange(new int[]{R.integer.metformin, R.integer.sulfonylurea, R.integer.insulin});
        //Adding recommendations
        if (diabetes_level > 1) {
            if (diabetes_status > 0) {
                if (!hasTestWithin(R.integer.HbA1c, monthsBefore(3))) {
                    addTestRec(R.integer.HbA1c, "");
                }
            } else {
                if (!hasTestWithin(R.integer.HbA1c, monthsBefore(6))) {
                    addTestRec(R.integer.HbA1c, "");
                }
            }

            // Make Recommendations
            if (!hasTestWithin(R.integer.EyeExam, monthsBefore(12))) {
                mRecommendation.add(new Record(getString(R.string.opthalmologist),
                        getString(R.string.referral_opthalmologist), getUUID(recordUUIDLookup, R.integer.referral), mEncounter.uuid,
                        getString(R.string.opthalmologist_ar), getString(R.string.referral_opthalmologist_ar)));
            }
            if (!hasTestWithin(R.integer.DentalExam, monthsBefore(12))) {
                mRecommendation.add(new Record(getString(R.string.dentist),
                        getString(R.string.referral_dentist), getUUID(recordUUIDLookup, R.integer.referral), mEncounter.uuid,
                        getString(R.string.dentist_ar), getString(R.string.referral_dentist_ar)));
            }
            if (!hasTestWithin(R.integer.ECG, monthsBefore(12)) && !hasTestWithin(R.integer.Echocardiogram, monthsBefore(12))) {
                addTestRec(R.integer.ECG, "");
            }

            diabetes_control = -1;

            //If it's been a month since the last drug change, and we're above 7, uncontrolled
            //If last visit was also uncontrolled, we're uncontrolled.
            if (lastDrugDate.getTime() > 0) {
                float hba1c = avgTestWithin(R.integer.HbA1c, monthsBefore(-1, lastDrugDate), 1, 1);
                diabetes_control = checkTestLevel(hba1c, R.integer.HbA1c);
                if (hba1c > 7) {
                    diabetes_control = 2;
                } else if (hba1c > 10) {
                    diabetes_control = 3;
                }
            }

            //Add medications
            if (diabetes_control > 2) {
                manage_meds(R.integer.insulin);
            } else if (diabetes_control > 1) {
                if (diabetes_level == 1) {
                    manage_meds(R.integer.metformin);
                }
                if (diabetes_level == 2) {
                    manage_meds(R.integer.sulfonylurea);
                } else if (diabetes_level == 3) {
                    manage_meds(R.integer.insulin);
                }
            }

        } else {
            //New Patients
            diabetes_status = getMaxLevel(tests, monthsBefore(1), 2, 10);
            if (diabetes_status > 1) {
                manage_meds(R.integer.metformin);
            } else {
                int todayLevel = getMaxLevel(tests, thisVisit(), 1, 1);
                if (todayLevel > 1) {
                    addTestRec(R.integer.FastingBloodGlucose, "");
                    followup = weeksBefore(-2);
                }
            }
        }
    }

    public float minTestWithin(int i, Date d, int atLeast, int num) {
        List<Test> list = (List<Test>) getWithinDate(testList.get(getInt(i)), d);
        float f = 10000;
        int ctr = 0;
        Date lastDate = new Date(0);

        String str = "";
        if (list.size() >= atLeast) {
            Collections.sort(list, compCreate);
            for (Test test : list) {
                if (!Model.LEAST_DATE.format(lastDate).equals(
                        Model.LEAST_DATE.format(test.getCreatedDate()))) {
                    try {
                        if (ctr < num) {
                            str = test.getResult();
                            f = Math.min(f, Float.parseFloat(str));
                        } else
                            break;
                        ctr++;
                    } catch (Exception e) {
                        Log.e(TAG, "BAD NUMBER PARSE: " + str);
                        atLeast = atLeast - 1;
                        if (list.size() < atLeast)
                            return -1;
                    }
                }
            }
            return f;
        }

        return -1;
    }

    //Returns the maximum level using checkTestLevel of avgTestWithin for a set of Tests.
    public int getMinLevel(int[] items, Date time, int num, int max) {
        int[] results = new int[items.length];
        int maxLevel = 10000;
        for (int i = 0; i < items.length; i++) {
            results[i] = checkTestLevel(minTestWithin(items[i], time, num, max), items[i]);
            if (results[i] > -1 && results[i] < maxLevel)
                maxLevel = results[i];
        }
        if (maxLevel == 10000)
            maxLevel = -1;
        return maxLevel;
    }

    public float getMinVal(int[] items, Date time, int num, int max) {
        float[] results = new float[items.length];
        float maxLevel = 10000;
        for (int i = 0; i < items.length; i++) {
            results[i] = minTestWithin(items[i], time, num, max);
            if (results[i] > -1 && results[i] < maxLevel)
                maxLevel = results[i];
        }
        if (maxLevel == 10000)
            maxLevel = -1;
        return maxLevel;
    }


    //Returns the maximum level using checkTestLevel of avgTestWithin for a set of Tests.
    public int getMaxLevel(int[] items, Date time, int num, int max) {
        int[] results = new int[items.length];
        int maxLevel = -1;
        for (int i = 0; i < items.length; i++) {
            results[i] = checkTestLevel(avgTestWithin(items[i], time, num, max), items[i]);
            if (results[i] > maxLevel)
                maxLevel = results[i];
        }
        return maxLevel;
    }

    public int hasAssignedTest(int id, Date within) {
        List<Record> recs = (List<Record>) getWithinDate(recordList.get(getInt(R.integer.test)), within);
        TestCategory cat = TestCategory.get(context, testUUIDLookup.get(getInt(id)));
        String name = cat.getDisplayName();
        for (int i = 0; i < recs.size(); i++) {
            if (recs.get(i).getValue().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void compute_hypertension() {
        Date lastDrugDate = null;

        int[] bp_sys = {R.integer.BPSys1, R.integer.BPSys2};
        int[] bp_dia = {R.integer.BPDia1, R.integer.BPDia2};
        if (hasMed(R.integer.Diagnosedhypertension)) {
            hypertension_level = 1;
        }
        if (hasMed(R.integer.thiazide)) {
            hypertension_level = 2;
        }
        if (hasMed(R.integer.arblock) || hasMed(R.integer.ace_inhibitor)) {
            hypertension_level = 3;
        }
        if (hasMed(R.integer.ccblock)) {
            hypertension_level = 4;
        }
        if (hasMed(R.integer.betablock)) {
            hypertension_level = 5;
        }

        int todayLevel = Math.max(getMinLevel(bp_sys, thisVisit(), 1, 1), getMinLevel(bp_dia, thisVisit(), 1, 1));
        //for undiagnosed patients
        if (hypertension_level == 0) {


            int level = Math.max(getMinLevel(bp_sys, monthsBefore(2), 2, 2), getMinLevel(bp_dia, monthsBefore(2), 2, 2));

            if (level > 2) {
                manage_meds(R.integer.thiazide);
                manage_meds(R.integer.ace_inhibitor);
                hypertension_status = 3;
            } else if (level > 1) {
                manage_meds(R.integer.thiazide);
                hypertension_status = 2;
            } else if (todayLevel > 1) {
                if (hasAssignedTest(R.integer.BPSys1, weeksBefore(4)) > -1) {
                    addTestRec(R.integer.BPSys1, context.getString(R.string.get_hypertension_test));
                    followup = weeksBefore(-1);
                }
            }
        } else {
            lastDrugDate = getLastDrugChange(new int[]{R.integer.thiazide, R.integer.arblock,
                    R.integer.ace_inhibitor, R.integer.ccblock, R.integer.betablock});

            hypertension_control = Math.max(getMinLevel(bp_sys, weeksBefore(-1, lastDrugDate), 2, 4),
                    getMinLevel(bp_dia, weeksBefore(-1, lastDrugDate), 2, 4));

            if (!hasTestWithin(R.integer.SerumCreatinine, monthsBefore(12))) {
                addTestRec(R.integer.SerumCreatinine, "");
            }
            if (!hasTestWithin(R.integer.SerumPotassium, monthsBefore(12)) && onMed(R.integer.thiazide)) {
                addTestRec(R.integer.SerumPotassium, "");
            }

            if (todayLevel > 1 && hypertension_control == -1) {
                addTestRec(R.integer.BPSys1, "");
                followup = weeksBefore(-1);
            }

            if (hypertension_control > 1) {
                switch (hypertension_level) {
                    case 1:
                        manage_meds(R.integer.thiazide);
                        break;
                    case 2:
                        if (getNumberAdjustments(R.integer.thiazide) > 1) {
                            manage_meds(R.integer.ace_inhibitor);
                        } else {
                            if (hasMed(R.integer.thiazide))
                                manage_meds(R.integer.thiazide);
                        }
                        break;
                    case 3:
                        if (getNumberAdjustments(R.integer.ace_inhibitor) > 1 || getNumberAdjustments(R.integer.arblock) > 1) {
                            manage_meds(R.integer.ccblock);
                        } else {
                            if (hasMed(R.integer.ace_inhibitor))
                                manage_meds(R.integer.ace_inhibitor);
                            if (hasMed(R.integer.arblock))
                                manage_meds(R.integer.arblock);
                        }
                        break;
                    case 4:
                        if (getNumberAdjustments(R.integer.ccblock) > 1) {
                            manage_meds(R.integer.betablock);
                        } else {
                            if (hasMed(R.integer.ccblock))
                                manage_meds(R.integer.betablock);
                        }
                        break;
                    case 5:
                        if (getNumberAdjustments(R.integer.betablock) > 1) {
                            if (!hasMed(R.integer.ccblock))
                                manage_meds(R.integer.ccblock);
                        }
                        manage_meds(R.integer.betablock);
                        break;
                }
            }
        }

    }

    public List<Test> compute_derived_test(List<Test> tests) {
        SparseArray<List<Test>> relTest = getRelevantTest(tests);
        List<Test> outputArray = new ArrayList<>();
        if (relTest.get(getInt(R.integer.Weight)) != null) {
            if (!relTest.get(getInt(R.integer.Weight)).isEmpty()) {
                float weight = Float.parseFloat(relTest.get(getInt(R.integer.Weight)).get(0).getResult());
                float height = -1;
                if (!relTest.get(getInt(R.integer.Height)).isEmpty()) {
                    height = Float.parseFloat(relTest.get(getInt(R.integer.Height)).get(0).getResult());
                } else {
                    if (!testList.get(getInt(R.integer.Height)).isEmpty()) {
                        height = Float.parseFloat(testList.get(getInt(R.integer.Height)).get(0).getResult());
                    }
                }
                if (height > 0) {
                    height = height / 100f;
                    Float bmi = weight / (height * height);
                    Test test = new Test(String.format("%.2f", bmi), testUUIDLookup.get(getInt(R.integer.BMI)), mEncounter);
                    outputArray.add(test);
                    currentTestList.put(testUUIDLookup.get(getInt(R.integer.BMI)), test);
                }
            }
        }

        return outputArray;
    }

    public boolean hasValTestWithin(int i, int v, Date date) {
        List<Test> list = (List<Test>) getWithinDate(testList.get(getInt(i)), date);
        if (list != null) {
            for (Test test : list) {
                try {
                    if (Integer.parseInt(test.getResult()) == v) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Bad parse: " + test.getResult());
                }
            }
        }
        return false;
    }

    public boolean hasValTest(int i, int v) {
        List<Test> list = testList.get(getInt(i));
        if (list != null) {
            for (Test test : list) {
                try {
                    if (Integer.parseInt(test.getResult()) == v) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Bad parse: " + test.getResult());
                }
            }
        }
        return false;
    }

    public boolean hasTestWithin(int i, Date d) {
        return !getWithinDate(testList.get(getInt(i)), d).isEmpty();
    }


    public float avgTestWithin(int i, Date d, int atLeast, int num) {
        List<Test> list = (List<Test>) getWithinDate(testList.get(getInt(i)), d);
        float f = 0;
        int ctr = 0;
        Date lastDate = new Date(0);

        String str = "";
        if (list.size() >= atLeast) {
            Collections.sort(list, compCreate);
            for (Test test : list) {
                if (!Model.LEAST_DATE.format(lastDate).equals(
                        Model.LEAST_DATE.format(test.getCreatedDate()))) {
                    try {
                        if (ctr < num) {
                            str = test.getResult();
                            f = f + Float.parseFloat(str);
                        } else
                            break;
                        ctr++;
                    } catch (Exception e) {
                        Log.e(TAG, "BAD NUMBER PARSE: " + str);
                        atLeast = atLeast - 1;
                        if (list.size() < atLeast)
                            return -1;
                    }
                }
            }
            return f / (float) ctr;
        }

        return -1;
    }

    public MedicationGroupCategory getMed(int i) {
        return MedicationGroupCategory.get(context, medicationCategoryUUIDLookup.get(getInt(i)));
    }

    public boolean hasMed(int i) {
        return medList.get(getInt(i)) == null ? false : !medList.get(getInt(i)).isEmpty();
    }

    public boolean onMed(int i) {
        return currentMeds.get(getInt(i)) == null ? false : !currentMeds.get(getInt(i)).isEmpty();
    }

    public Medication getCurMed(int i) {
        return currentMeds.get(getInt(i)) == null ? null : currentMeds.get(getInt(i)).get(0);
    }

    public int manage_meds(int type) {
        if (onMed(type)) {
            mMedRecMod.put(getCurMed(type), MedMod.CHANGE);
            return 0;
        }
        switch (type) {
            case R.integer.lo_statin:
                if (onMed(R.integer.hi_statin)) {
                    mMedRecMod.put(getCurMed(R.integer.hi_statin), MedMod.STOP);
                }
                mMedRecAdd.add(getMed(R.integer.lo_statin));
                return 1;
            case R.integer.hi_statin:
                if (onMed(R.integer.lo_statin)) {
                    mMedRecMod.put(getCurMed(R.integer.lo_statin), MedMod.STOP);
                }
                mMedRecAdd.add(getMed(R.integer.hi_statin));
                return 1;
            case R.integer.insulin:
                if (onMed(R.integer.sulfonylurea)) {
                    mMedRecMod.put(getCurMed(R.integer.sulfonylurea), MedMod.STOP);
                }
                mMedRecAdd.add(getMed(R.integer.insulin));
                return 1;
            case R.integer.ace_inhibitor:
                if (hasValTestWithin(R.integer.Pregnancy, 1, monthsBefore(9))) {
                    if (manage_meds(R.integer.ccblock) == 0) {
                        manage_meds(R.integer.betablock);
                    }
                } else {
                    mMedRecAdd.add(getMed(type));
                }
                return 1;
            case R.integer.ccblock:
                if (hasValTest(R.integer.Hadmyocardialinfraction, 1)) {
                    manage_meds(R.integer.betablock);
                } else {
                    mMedRecAdd.add(getMed(R.integer.ccblock));
                }
            default:
                mMedRecAdd.add(getMed(type));
                return 1;
        }
    }

    public void compute_dyslipidemia() {
        Date usedate = monthsBefore(-1, getLastDrugChange(new int[]{R.integer.hi_statin, R.integer.lo_statin}));
        if (usedate.before(monthsBefore(12))) {
            usedate = monthsBefore(12);
        }
        lipid_control = checkTestLevel(avgTestWithin(R.integer.LDL, usedate, 1, 10), R.integer.LDL);

        if (hasMed(R.integer.lo_statin)) {
            lipid_level = 1;
        }
        if (hasMed(R.integer.hi_statin)) {
            lipid_level = 2;
        }

        float tempResult;
        // Make Recommendations, primarily medications
        if (hasValTest(R.integer.Hadmyocardialinfraction, 1) || hasValTest(R.integer.Hadacerebrovasculareventstroke, 1)
                || hasValTest(R.integer.Hadcardiacfailure, 1)) {
            if (age > 75) {
                manage_meds(R.integer.lo_statin);
            } else {
                manage_meds(R.integer.hi_statin);
            }
        } else {
            tempResult = minTestWithin(R.integer.Cholesterol, usedate, 1, 1);
            if (tempResult > 190) {
                manage_meds(R.integer.hi_statin);
            } else if (tempResult > 0) {
                if (diabetes_level > 0 || diabetes_status > 0) {
                    if (ascvd_risk > 0) {
                        manage_meds(R.integer.hi_statin);
                    } else {
                        manage_meds(R.integer.lo_statin);
                    }
                } else {
                    if (ascvd_risk > 0 && (age > 40 && age < 75)) {
                        manage_meds(R.integer.lo_statin);
                    }
                }
            } else {
                if (diabetes_level > 0 || diabetes_status > 0) {
                    if (ascvd_risk > 0) {
                        manage_meds(R.integer.hi_statin);
                    } else {
                        manage_meds(R.integer.lo_statin);
                    }
                } else {
                    if (ascvd_risk > 0 && (age > 40 && age < 75)) {
                        manage_meds(R.integer.lo_statin);
                    }
                }
            }
        }
    }

    public void compute_ascvd_risk() {
        String gender = mPatient.getGender();
        int agecat = age / 10;

        if (agecat < 4) {
            agecat = 0;
        } else if (agecat < 8) {
            agecat = agecat - 4;
        } else {
            agecat = 3;
        }

        int diacat = diabetes_level > 0 ? 1 : 0;
        int smokecat;
        int bpcat;
        int cholcat = -1;

        List<Test> tempList;
        Float tempResult;
        tempList = (List<Test>) getWithinDate(testList.get(getInt(R.integer.Smoking)), monthsBefore(12)); //any in last year
        if (tempList != null && !tempList.isEmpty()) {
            try {
                smokecat = 0;
                for (Test test : tempList) {
                    if (Integer.parseInt(test.getResult()) > 0) {
                        smokecat = 1;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Bad integer parse: " + e.getMessage());
                smokecat = 0;
            }
        } else { //assume nonsmoker
            ascvd_risk = -1;
            addTestRec(R.integer.Smoking, "");
            return;
        }

        int[] bp_sys = new int[]{R.integer.BPSys1, R.integer.BPSys2};
        tempResult = getMinVal(bp_sys, monthsBefore(2), 1, 1);
        if (tempResult < 0) {
            ascvd_risk = -1;
            addTestRec(R.integer.BPSys1, "");
            return;
        }
        try {
            if (tempResult < 130) {
                bpcat = 0;
            } else if (tempResult < 150) {
                bpcat = 1;
            } else if (tempResult < 170) {
                bpcat = 2;
            } else {
                bpcat = 3;
            }
        } catch (NumberFormatException e) {
            bpcat = 0;
            Log.e(TAG, "Bad Float Parse: " + tempList.get(0).getResult());
            ascvd_risk = -1;
            return;
        }

        tempResult = minTestWithin(R.integer.Cholesterol, monthsBefore(12), 1, 1);
        if (tempList != null && !tempList.isEmpty()) {
            if (tempResult < 171) {
                cholcat = 0;
            } else if (tempResult < 209) {
                cholcat = 1;
            } else if (tempResult < 247) {
                cholcat = 2;
            } else if (tempResult < 285) {
                cholcat = 3;
            } else {
                cholcat = 4;
            }
        }

        if (gender.equals("M")) {
            if (cholcat < 0) {
                ascvd_risk = ascvd_risk_m[agecat][diacat][smokecat][bpcat];
            } else {
                ascvd_risk = ascvd_risk_m_c[cholcat][agecat][diacat][smokecat][bpcat];
            }
        } else {
            if (cholcat < 0) {
                ascvd_risk = ascvd_risk_f[agecat][diacat][smokecat][bpcat];
            } else {
                ascvd_risk = ascvd_risk_f_c[cholcat][agecat][diacat][smokecat][bpcat];
            }
        }
    }

}
