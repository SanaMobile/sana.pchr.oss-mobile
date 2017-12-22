package com.smarking.mhealthsyria.app.api.sync;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.smarking.mhealthsyria.app.UpdateManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpdateService extends IntentService {

    public static final String TAG = UpdateService.class.getSimpleName();
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CHECK_UPDATE = "com.smarking.mhealthsyria.action.CHECK_UPDATE";

    public UpdateService() {
        super(TAG);
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static Intent getUpdateIntent(Context context) {
        Intent intent = new Intent(context, UpdateService.class);
        intent.setAction(ACTION_CHECK_UPDATE);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_UPDATE.equals(action)) {
                handleActionUpdate();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdate() {
        Log.i(TAG, "handleActionUpdate()");
        try {
            UpdateManager.markUpdateCheckTime(getApplicationContext());
            // Current version
            UpdateInfo updateInfo = UpdateManager.check(this);
            Log.d(TAG, "Update result: " + updateInfo.versionCode
                    + ", " + updateInfo.pkg);
            // If returned apk version > installed version
            if (updateInfo.updatable) {
                Log.w(TAG, "Updating...");
                UpdateManager.setUpdating(getApplicationContext(), true);
                Uri update = UpdateManager.updateDM(this, updateInfo);
                if(!(update == Uri.EMPTY)) {
                    UpdateManager.updatePackage(this, update);
                }
            } else {
                Log.i(TAG, "NOT Updating...");
                UpdateManager.setUpdating(getApplicationContext(), false);
                if(updateInfo.versionCode == 0) {
                    //TODO Do we really want to cancel here, Probably not
                    Log.w(TAG, "Got a version code of 0");
                    //UpdateManager.cancelPeriodicUpdateTrigger(this);
                }
            }
        } catch (Exception e){
            Log.e(TAG, "Update error: message=" + e);
            UpdateManager.setUpdating(getApplicationContext(), false);
            e.printStackTrace();
            //TODO Do we really want to cancel here, Probably not
            //UpdateManager.cancelPeriodicUpdateTrigger(this);
        }
    }
}
