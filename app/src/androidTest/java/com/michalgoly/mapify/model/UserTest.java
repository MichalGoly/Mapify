package com.michalgoly.mapify.model;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.greenrobot.greendao.database.Database;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserTest {

    private static final String DB_NAME_TEST = "mapify-db-test";

    private Context context = null;
    private DaoSession daoSession = null;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
        Assert.assertNotNull(context);
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME_TEST);
        Database db = helper.getWritableDb();
        DaoMaster.dropAllTables(db, true);
        DaoMaster.createAllTables(db, true);
        daoSession = new DaoMaster(db).newSession();
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
}
