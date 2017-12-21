package com.smarking.mhealthsyria.app.view.custom;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.Reporter;
import com.smarking.mhealthsyria.app.api.auth.SessionManager;
import com.smarking.mhealthsyria.app.api.sync.SyncService;
import com.smarking.mhealthsyria.app.model.category.Category;
import com.smarking.mhealthsyria.app.model.event.Event;

import java.util.Locale;
import java.util.Map;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-03.
 */
public abstract class AuthenticatedActivity extends ActionBarActivity{
    private SessionManager mSessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionManager = SessionManager.get(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.sana_blue)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSessionManager.checkLogin();
    }

    public Map<String, String> getCurrentUserData(){
        return mSessionManager.getCurrentUserData();
    }

    public void logout(){
        Reporter.ok(getApplicationContext(), Event.Code.USER_LOGOUT, "");
        mSessionManager.logoutUser(true);
        String toast = getString(R.string.logging_out_user);
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auth_menu, menu);
        return true;
    }

    public void runSync() {
        String device_key_user_key = mSessionManager.getCurrentUserData().get(SessionManager.KEY_DEVICE_KEY_USER_KEY);
        SyncService.requestSyncAndProcessImmediately(this, device_key_user_key, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_logout: {
                Reporter.ok(getApplicationContext(),Event.Code.USER_LOGOUT,
                        "Logout selected");
                logout();
                break;
            }
            case R.id.action_sync: {
                Reporter.synch(getApplicationContext(), "Synch button pressed");
                runSync();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateLanguage(String lang) {
        if (lang != null && !lang.isEmpty()) {
            Resources res = getResources();
            // Change locale settings in the app.
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();

            if (lang.equals("ar")) {
                conf.locale = new Locale("ar");
                Category.language = Category.Language.ARABIC;
            } else {
                conf.locale = new Locale("en");
                Category.language = Category.Language.ENGLISH;
            }
            conf.setLayoutDirection(conf.locale);
            res.updateConfiguration(conf, dm);
            Configuration config = res.getConfiguration();
        }
    }

}
