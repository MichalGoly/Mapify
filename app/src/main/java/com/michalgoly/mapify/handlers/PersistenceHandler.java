package com.michalgoly.mapify.handlers;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.michalgoly.mapify.R;
import com.michalgoly.mapify.model.DaoMaster;
import com.michalgoly.mapify.model.DaoSession;
import com.michalgoly.mapify.model.LatLngWrapper;
import com.michalgoly.mapify.model.PolylineWrapper;
import com.michalgoly.mapify.model.TrackWrapper;
import com.michalgoly.mapify.model.User;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

public class PersistenceHandler extends Service implements PersistenceManager {

    private static final String TAG = "PersistenceHandler";

    private IBinder binder = new ServiceBinder();

    private Database database = null;
    private DaoSession daoSession = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called");
        init();
        return binder;
    }

    public class ServiceBinder extends Binder {
        public PersistenceHandler getService() {
            return PersistenceHandler.this;
        }
    }

    @Override
    public void insert(User user) {
        daoSession.getUserDao().insert(user);
    }

    @Override
    public void update(User user) {
        daoSession.getUserDao().update(user);
    }

    @Override
    public User select(String id) {
        return daoSession.getUserDao().load(id);
    }

    @Override
    public void delete(User user) {
        daoSession.getUserDao().delete(user);
    }

    @Override
    public void insert(PolylineWrapper pw) {
        daoSession.getPolylineWrapperDao().insert(pw);
        if (pw.getPoints() != null)
            for (LatLngWrapper point : pw.getPoints())
                daoSession.getLatLngWrapperDao().insertOrReplace(point);
        if (pw.getTrackWrapper() != null)
            daoSession.getTrackWrapperDao().insertOrReplace(pw.getTrackWrapper());
        if (pw.getUser() != null)
            daoSession.getUserDao().insertOrReplace(pw.getUser());
    }

    @Override
    public void update(PolylineWrapper pw) {
        daoSession.getPolylineWrapperDao().update(pw);
        if (pw.getPoints() != null)
            for (LatLngWrapper point : pw.getPoints())
                daoSession.getLatLngWrapperDao().update(point);
        if (pw.getTrackWrapper() != null)
            daoSession.getTrackWrapperDao().update(pw.getTrackWrapper());
        if (pw.getUser() != null)
            daoSession.getUserDao().update(pw.getUser());
    }

    @Override
    public PolylineWrapper select(Long id) {
        return daoSession.getPolylineWrapperDao().load(id);
    }

    @Override
    public void delete(PolylineWrapper pw) {
        daoSession.getPolylineWrapperDao().delete(pw);
    }

    @Override
    public List<PolylineWrapper> listWrappers(User user) {
        // TODO proper query to avoid listing all
        List<PolylineWrapper> out = new ArrayList<>();
        for (PolylineWrapper pw : daoSession.getPolylineWrapperDao().loadAll())
            if (pw.getUser().getId().equals(user.getId()))
                out.add(pw);
        return out;
    }

    private void init() {
        DaoMaster.DevOpenHelper helper = null;
        if (System.getProperty("IS_TEST") != null) {
            // test env
            helper = new DaoMaster.DevOpenHelper(this, getString(R.string.db_name_test));
        } else if (System.getProperty("IS_PRODUCTION") != null) {
            // dev env
            helper = new DaoMaster.DevOpenHelper(this, getString(R.string.db_name));
        } else {
            // dev env
            helper = new DaoMaster.DevOpenHelper(this, getString(R.string.db_name_dev));
        }
        database = helper.getWritableDb();
        daoSession = new DaoMaster(database).newSession();
    }
}
