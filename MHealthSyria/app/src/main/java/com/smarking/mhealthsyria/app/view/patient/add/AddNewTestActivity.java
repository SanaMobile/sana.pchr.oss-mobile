package com.smarking.mhealthsyria.app.view.patient.add;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Model;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.model.category.TestCategory;
import com.smarking.mhealthsyria.app.view.custom.AuthenticatedActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-24.
 */

public class AddNewTestActivity extends AuthenticatedActivity implements View.OnClickListener {

    public static String TAG = AddNewTestActivity.class.getSimpleName();
    public static final int LOWER_LIMIT = 100;
    public static final int MEDICAL_HISTORY = 400;
    public static final int PHYSICAL_EXAMINATION = 600;
    public static final int LAB_TEST = 800;
    public static final String PRIORITY = "priority";

    private int mPriority;

    public static final int LOADER_ENCOUNTER_CAT = 0;
    public static final int LOADER_TEST_CAT = 1;

    private ArrayList<TestCategory> testCategories = new ArrayList<>();
    private ArrayList<Integer> testCatSections = new ArrayList<>();
    private LinearLayout llTestCategories;
    private Encounter mEncounter;
    private Patient mPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_test_activity);

        llTestCategories = (LinearLayout) findViewById(R.id.llTestCategories);

        Button bAdd = (Button) findViewById(R.id.bAdd);
        Button bCancel = (Button) findViewById(R.id.bClear);

        mEncounter = getIntent().getParcelableExtra(Encounter.TABLE);
        mPatient = getIntent().getParcelableExtra(Patient.TABLE);
        mPriority =getIntent().getIntExtra(AddNewTestActivity.PRIORITY, 0);

        bAdd.setOnClickListener(this);
        bCancel.setOnClickListener(this);


        loadTestCategories();
    }

    private void loadTestCategories(){
        String[] titles = getResources().getStringArray(R.array.test_cat_dividers);
        String gd_uuid = Recommender.getInstance().getUUID(Recommender.testUUIDLookup,
                R.integer.Hadgestationaldiabetes);
        String preg_uuid = Recommender.getInstance().getUUID(Recommender.testUUIDLookup,
                R.integer.Pregnancy);
        String bmi_lookup = Recommender.getInstance().getUUID(Recommender.testUUIDLookup, R.integer.BMI);

        Cursor tcCursor = getContentResolver().query(TestCategory.URI, null, null, null, null);
        while (tcCursor.moveToNext()) {
            TestCategory cat = new TestCategory(tcCursor);
            Log.e(TAG, "Test: " + cat.getDisplayName() + " " + cat.getPriority());
            //Men not pregnant
            if (!mPatient.getGender().equals("M") || (!cat.uuid.equals(gd_uuid) && !cat.uuid.equals(preg_uuid))) {
                if (!cat.uuid.equals(bmi_lookup)) {
                    switch (mPriority) {
                        case MEDICAL_HISTORY:
                            if (cat.getPriority() <= mPriority && cat.getPriority() > LOWER_LIMIT)
                                testCategories.add(cat);
                            break;
                        case PHYSICAL_EXAMINATION:
                            if (cat.getPriority() <= mPriority && cat.getPriority() > MEDICAL_HISTORY)
                                testCategories.add(cat);
                            break;
                        case LAB_TEST:
                            if (cat.getPriority() <= mPriority && cat.getPriority() > PHYSICAL_EXAMINATION)
                                testCategories.add(cat);
                            break;
                    }
                }
            }
        }
        tcCursor.close();

        Collections.sort(testCategories, new Comparator<TestCategory>() {
            @Override
            public int compare(TestCategory lhs, TestCategory rhs) {
                return lhs.getPriority() - rhs.getPriority();
            }
        });

        int new_priority = 0;
        int prev_priority = 0;
        int counter = 0;
        for (TestCategory testCategory : testCategories) {
            new_priority = testCategory.getPriority() / 100;
            if (new_priority > prev_priority) {
                try {
                    llTestCategories.addView(getViewDivider(titles[new_priority - 1]));
                    counter = counter + 1;
                } catch (Exception e) {
                    Log.e(TAG, "Adding divider caused exception: " + e.getClass().getSimpleName() + " " + e.getMessage());
                }
                prev_priority = new_priority;
            }
            testCatSections.add(new Integer(counter));
            addCategoryToLinearLayout(testCategory);
            counter = counter + 1;
        }
    }



    private void addCategoryToLinearLayout(TestCategory category){

        String result = getResultString(category);
        View testView = getTestView(result, category);

        if(testView !=null) {
            llTestCategories.addView(testView);
        }

    }

    @Override
    public void onBackPressed() {
        proccessInputAndSave();
        super.onBackPressed();
    }

    public String getResultString(TestCategory category){
        String result = null;
        String selection = getSelection(category);
        Cursor tCursor = getContentResolver().query(Test.URI, null, selection, null, null);
        if(tCursor.moveToNext()){
            Test currTest = new Test(tCursor);
            result = currTest.getResult();
        }
        tCursor.close();
        return result;
    }

    public String getSelection(TestCategory category){
        String where = " %s = '%s' ";
        String and = " %s AND %s ";

        String sCatWhere = String.format(where, Test.CATEGORY_UUID, category.uuid);
        String sEncWhere = String.format(where,Test.ENCOUNTER_UUID ,mEncounter.uuid);
        String sProjection  = String.format(and, sCatWhere, sEncWhere);
        return sProjection;

    }

    public View getTestView(String result, TestCategory category){
        View testView = null;
        switch (category.getResultType()){
            case TestCategory.RESULT_TYPE_TEXT:
                testView = getViewText(result, category);
                break;
            case TestCategory.RESULT_TYPE_NUMBER:
                testView = getViewNumber(result, category);
                break;
            case TestCategory.RESULT_TYPE_BOOL:
                testView = getViewBool(result, category);
                break;
            case TestCategory.RESULT_TYPE_OPTION:
                testView = getViewOptions(result, category);
                break;
        }
        return testView;
    }


    public View getViewText(String result, Category category){
        View view = View.inflate(this, R.layout.test_category_text, null);
        TextView displayName0 = (TextView) view.findViewById(R.id.tvDisplayName);
        EditText etResult =(EditText) view.findViewById(R.id.etResultTextValue);
        if (result != null && !result.isEmpty())
            etResult.setText(result);
        displayName0.setText(category.getDisplayName());
        return view;
    }

    public View getViewDivider(String text) {
        View view = View.inflate(this, R.layout.test_category_divide, null);
        TextView displayName0 = (TextView) view.findViewById(R.id.tvDisplayName);
        displayName0.setText(text);
        return view;
    }

    public View getViewNumber(String result, TestCategory category){
        View childView = View.inflate(this, R.layout.test_category_number, null);
        TextView displayName1 = (TextView) childView.findViewById(R.id.tvDisplayName);
        displayName1.setText(category.getDisplayName());
        EditText etResult = (EditText) childView.findViewById(R.id.etResultNumberValue);
        if (result != null && !result.isEmpty())
            etResult.setText(result);
        TextView tvUnits = (TextView) childView.findViewById(R.id.tvResultUnits);
        tvUnits.setText(category.getResultUnits());
        return childView;
    }

    private View getViewBool(String result, TestCategory category) {
        View childView = View.inflate(this, R.layout.test_category_boolean, null);
        TextView displayName2 = (TextView) childView.findViewById(R.id.tvDisplayName);
        RadioButton bYes = (RadioButton) childView.findViewById(R.id.rbYes);
        RadioButton bNo = (RadioButton) childView.findViewById(R.id.rbNo);
        final RadioGroup rg = (RadioGroup) childView.findViewById(R.id.radio);

        View.OnTouchListener radioButtonOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (((RadioButton) v).isChecked()) {
                    // If the button was already checked, uncheck them all
                    rg.clearCheck();
                    // Prevent the system from re-checking it
                    return true;
                }
                return false;
            }
        };
        bYes.setOnTouchListener(radioButtonOnTouchListener);
        bNo.setOnTouchListener(radioButtonOnTouchListener);

        displayName2.setText(category.getDisplayName());
        if (result != null && !result.isEmpty()) {
            if (result.equals("0"))
                bNo.setChecked(true);
            else
                bYes.setChecked(true);
        }
        return childView;
    }

    private void setRadioClickListener(final RadioGroup rg, final RadioButton rb){
        rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rb.isChecked())
                    rg.clearCheck();
            }
        });
    }

    public View getViewOptions(String result, TestCategory category){
        Log.e(TAG, "getting options view");
        View childView = View.inflate(this, R.layout.test_category_options, null);
        TextView displayName3 = (TextView) childView.findViewById(R.id.tvDisplayName);
        displayName3.setText(category.getDisplayName());
        Spinner spResults = (Spinner) childView.findViewById(R.id.spinResultOptions);
        String units = category.getResultUnits();
        String[] unitsSplit = units.split("\\|");
        String[] options = Arrays.copyOf(unitsSplit, unitsSplit.length + 1);
        options[options.length - 1] = "";
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, options);
        spResults.setAdapter(adapter);
        spResults.setSelection(options.length - 1);
        if (result != null && !result.isEmpty()) {
            try{
                int index = Integer.parseInt(result);
                if(index >=0)
                    spResults.setSelection(index);
            } catch (NumberFormatException nfe){
                Log.e(TAG, "invalid result for " + category.getDisplayName());
            }

        } else{
            Log.e(TAG, "result is null");
        }
        return childView;
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bAdd:{
                proccessInputAndSave();
                break;
            }
            case R.id.bClear:{
                showClearAlertDialog();
                break;
            }
        }
    }

    private void proccessInputAndSave(){
            ArrayList<Test> tests = getTestsToAdd();
            if (tests != null)
                saveTestsAndFinish(tests);
    }

    private void saveTestsAndFinish(List<Test> tests) {
        ContentValues[] testCV = new ContentValues[tests.size()];
        for (int i = 0; i < tests.size(); i++) {
            testCV[i] = tests.get(i).putContentValues();
            Log.e(TAG, "Saving test: " + tests.get(i).getResult());
        }
        this.getContentResolver().bulkInsert(Test.URI, testCV);
            setResult(RESULT_OK);
            finish();
        }

    private ArrayList<Test> getTestsToAdd() throws NumberFormatException{
        ArrayList<Test> tests = processUserInput();
        if(tests == null)
            return null;
        // add Auxiliary tests - Derived things like BMI.
        List<Test> newtest = Recommender.getInstance().compute_derived_test(tests);
        for (Test test : newtest) {
            Test oldTest = getOldTest(TestCategory.get(this, test.getCategory_uuid()));
            if (oldTest == null)
                tests.add(test);
            else {
                updateTest(oldTest, test.getResult());
            }
        }

        return tests;
    }

    private ArrayList<Test> processUserInput(){
        ArrayList<Test> tests = new ArrayList<>();
        int count = testCategories.size();
        for (int i = 0; i < count; i++) {
            View view = llTestCategories.getChildAt(testCatSections.get(i));
            TestCategory category = testCategories.get(i);
            try {
                String result = getResultInput(view, category);
                if (!result.equals(getString(R.string.none)) && !result.equals("")) {
                    Log.e(TAG, "Adding test: " + category.getDisplayName() + " " + result);
                    Test test = new Test("", Model.ISO8601_FORMAT_TZ.format(Calendar.getInstance().getTime()), "", "", result, category.uuid, mEncounter.uuid);
                    Recommender.getInstance().add_to_cur_test_list(test);
                    Test oldTest = getOldTest(category);
                    if (oldTest == null)
                        tests.add(test);
                    else {
                        updateTest(oldTest, result);
                    }
                }
            }catch (NumberFormatException e){
                showInvalidAlertDialog(i);
                return null;
            }
        }
        return tests;
    }

    private void updateTest(Test test, String result) {
            test.setResult(result);
            getContentResolver().update(Test.URI, test.putContentValues(), "uuid = \'" + test.uuid + "\'", null);
    }


    private Test getOldTest(TestCategory category){
        String selection = getSelection(category);
        Cursor tCursor = getContentResolver().query(Test.URI, null,selection, null,null);
        if(tCursor.moveToNext())
            return new Test(tCursor);
        tCursor.close();
        return null;
    }


    /**
     *
     * @param view
     * @param category
     * @return the result that the user put into the view
     */
    private String getResultInput(View view , TestCategory category) throws NumberFormatException{
        String result = getString(R.string.none);

        switch (category.getResultType()) {
            case TestCategory.RESULT_TYPE_TEXT:
                result = getResulText(view);
                break;
            case 1:
                try {
                    result = getResultNumber(view, category);
                }catch(NumberFormatException e){
                    throw e;
                }
                break;
            case 2:
                result = getResultBool(view, result);
                break;
            case 3:
                result = getResultOptions(view);
                break;
        }
        return result;
    }

    private String getResultOptions(View view){
        try {
            Spinner spResults = (Spinner) view.findViewById(R.id.spinResultOptions);

            String result = Integer.toString(spResults.getSelectedItemPosition());
            if (spResults.getSelectedItem().equals(""))
                result = getString(R.string.none);
            return result;
        } catch (NullPointerException e) {
            return getString(R.string.none);
        }

    }

    private String getResultBool(View view, String result){
        RadioButton rbYes = (RadioButton) view.findViewById(R.id.rbYes);
        RadioButton rbNo = (RadioButton) view.findViewById(R.id.rbNo);
        if (rbYes.isChecked())
            result = "1";
        else if (rbNo.isChecked())
            result = "0";
        return result;
    }

    private String getResultNumber(View view, TestCategory category) throws NumberFormatException {
        EditText number = (EditText) view.findViewById(R.id.etResultNumberValue);
        if (!number.getText().toString().isEmpty()) {
            Float val = Float.parseFloat(number.getText().toString());
            if (val < category.getResultMin() || val > category.getResultMax()) {
                throw new NumberFormatException(getString(R.string.invalid_bounds));
            }
        }
        return number.getText().toString();
    }

    private String getResulText(View view){
        EditText text = (EditText) view.findViewById(R.id.etResultTextValue);
        return text.getText().toString();
    }


    private void showInvalidAlertDialog(int index){
        TestCategory cat = testCategories.get(index);
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.invalid) + " " + cat.getDisplayName())
                .setMessage(getString(R.string.minimum) + cat.getResultMin() + " " + getString(R.string.maximum) + cat.getResultMax())
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showClearAlertDialog(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.clear)
                .setMessage(R.string.confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadTestCategories();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    public class ToggleRadioButton extends RadioButton{

        public ToggleRadioButton(Context context) {
            super(context);
        }

        @Override
        public void toggle() {
            if(isChecked()) {
                if(getParent() instanceof RadioGroup) {
                    ((RadioGroup)getParent()).clearCheck();
                }
            } else {
                setChecked(true);
            }
        }
    }

}
