package com.wireless.ambeent.mozillaprototype.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
/**
 * Created by Ambeent Wireless.
 */
public class ServerService extends Service {

    private static final String TAG = "ServerService";


    public ServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }







}
