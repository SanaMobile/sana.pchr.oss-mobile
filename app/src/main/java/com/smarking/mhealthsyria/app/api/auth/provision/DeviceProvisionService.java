package com.smarking.mhealthsyria.app.api.auth.provision;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-03-28.
 */
public class DeviceProvisionService extends Service {
    public static final String TAG = DeviceProvisionService.class.getSimpleName();
    @Override
    public IBinder onBind(Intent intent) {

        DeviceProvisioner authenticator = new DeviceProvisioner(this);
        return authenticator.getIBinder();
    }

}
