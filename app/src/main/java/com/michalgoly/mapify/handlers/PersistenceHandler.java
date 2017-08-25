package com.michalgoly.mapify.handlers;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.michalgoly.mapify.model.PolylineWrapper;
import com.michalgoly.mapify.model.User;

import java.util.List;

public class PersistenceHandler extends Service implements PersistenceManager {

    private static final String TAG = "PersistenceHandler";

    private IBinder binder = new ServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called");
        return binder;
    }

    @Override
    public void insert(User user) {

    }

    @Override
    public void update(User user) {

    }

    @Override
    public User select(String id) {
        return null;
    }

    @Override
    public void delete(User user) {

    }

    @Override
    public void insert(PolylineWrapper pw) {

    }

    @Override
    public void update(PolylineWrapper pw) {

    }

    @Override
    public PolylineWrapper select(Long id) {
        return null;
    }

    @Override
    public void delete(PolylineWrapper pw) {

    }

    @Override
    public List<PolylineWrapper> listWrappers(User user) {
        return null;
    }

    public class ServiceBinder extends Binder {

        public PersistenceHandler getService() {
            return PersistenceHandler.this;
        }
    }
}
