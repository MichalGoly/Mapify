package com.michalgoly.mapify.handlers;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class PersistenceHandler extends Service {

    private static final String TAG = "PersistenceHandler";

    private IBinder binder = new ServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called");
        return binder;
    }

    public class ServiceBinder extends Binder {

        public PersistenceHandler getService() {
            return PersistenceHandler.this;
        }
    }
}
