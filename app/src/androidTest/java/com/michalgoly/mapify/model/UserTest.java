package com.michalgoly.mapify.model;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;
import com.michalgoly.mapify.R;
import com.michalgoly.mapify.handlers.PersistenceHandler;
import com.michalgoly.mapify.handlers.PersistenceManager;

import junit.framework.Assert;

import org.greenrobot.greendao.database.Database;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class UserTest {

    private Context context = null;
    private DaoSession daoSession = null;
    private PersistenceManager persistenceHandler = null;

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

    @Before
    public void setup() throws TimeoutException {
        System.setProperty("IS_TEST", "true");
        context = InstrumentationRegistry.getTargetContext();
        Assert.assertNotNull(context);
        String dbNameTest = context.getString(R.string.db_name_test);
        Assert.assertNotNull(dbNameTest);
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, dbNameTest);
        Database db = helper.getWritableDb();
        DaoMaster.dropAllTables(db, true);
        DaoMaster.createAllTables(db, true);
        daoSession = new DaoMaster(db).newSession();
    }

    @After
    public void tearDown() {
        System.clearProperty("IS_TEST");
    }

    @Test
    public void testUserPersistence() {
        User user = new User("1234ab", "Bob Smith");
        daoSession.getUserDao().insert(user);
        user = new User("432", "Marry Jane");
        daoSession.getUserDao().insert(user);
        Assert.assertEquals(2, daoSession.getUserDao().count());
    }

    @Test
    public void testPersistenceManager() throws TimeoutException {
        // get hold of the PersistenceHandler
        Intent intent = new Intent(context, PersistenceHandler.class);
        IBinder binder = serviceRule.bindService(intent);
        Assert.assertNotNull(binder);
        persistenceHandler = ((PersistenceHandler.ServiceBinder) binder).getService();
        Assert.assertNotNull(persistenceHandler);
        User user = new User("122211", "Michal Goly");
        persistenceHandler.insert(user);
        Assert.assertNotNull(persistenceHandler.select(user.getId()));
        Assert.assertEquals(user.getName(), persistenceHandler.select(user.getId()).getName());
    }

    @Test
    public void testTrackWrapperPersistence() {
        TrackWrapper tw = new TrackWrapper("Shut up", "Bob", "spotify:12345", "http://sp/dsa/d.png", 5232L);
        daoSession.getTrackWrapperDao().insert(tw);
        tw = new TrackWrapper("Hello", "Marry", "spotify:42", null, null);
        daoSession.getTrackWrapperDao().insert(tw);
        Assert.assertEquals(2, daoSession.getTrackWrapperDao().count());
        tw = daoSession.getTrackWrapperDao().load("spotify:12345");
        Assert.assertNotNull(tw);
        Assert.assertEquals("Shut up", tw.getTitle());
    }

//    @Test
//    public void testRelations() {
//        User user = new User("123", "Michal Goly");
//        daoSession.getUserDao().insert(user);
//        Assert.assertEquals(user.getName(), daoSession.getUserDao().load(user.getId()).getName());
//        List<LatLngWrapper> points = new ArrayList<>();
//        TrackWrapper tw = new TrackWrapper("Hello", "Bob", "spotify:12345", null, 12444L);
//        for (int i = 1; i <= 20; i++)
//            points.add(LatLngWrapper.toWrapper(new LatLng(i, -i)));
//        PolylineWrapper pw = new PolylineWrapper(points, R.color.materialDeepPurple, tw, new Date(),
//                null, user);
//        daoSession.getPolylineWrapperDao().insert(pw);
//        points.add(new LatLngWrapper(new LatLng(12, -54.2243)));
//        pw = new PolylineWrapper(points, R.color.materialGreen, tw, new Date(), null, user);
//        daoSession.getPolylineWrapperDao().insert(pw);
//        List<PolylineWrapper> pws = daoSession.getPolylineWrapperDao().loadAll();
//        Assert.assertEquals(2, pws.size());
//        for (PolylineWrapper polylineWrapper : pws)
//            Assert.assertNotNull(polylineWrapper.getId());
//        List<LatLngWrapper> llws = daoSession.getLatLngWrapperDao().loadAll();
//        Assert.assertEquals(21, llws.size());
//    }
}
