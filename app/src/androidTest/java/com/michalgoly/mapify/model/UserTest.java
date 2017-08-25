package com.michalgoly.mapify.model;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.greenrobot.greendao.database.Database;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserTest {

    private static final String DB_NAME = "mapify-db";
    private Context context = null;
    private DaoSession daoSession = null;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    @Test
    public void testUserPersistence() {
        User user = new User("12345abc", "Bob Smith");
        daoSession.getUserDao().insert(user);
        user = new User("2a", "Marry Jane");
        daoSession.getUserDao().insert(user);
        Assert.assertEquals(2, daoSession.getUserDao().count());
    }
}
