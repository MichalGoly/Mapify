package com.michalgoly.mapify.model;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.michalgoly.mapify.db.AppDatabase;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserTest {

    private Context context = null;
    private AppDatabase db = null;

    @Before
    public void setup() {
        System.setProperty("IS_TEST", "true");
        context = InstrumentationRegistry.getTargetContext();
        Assert.assertNotNull(context);
        db = AppDatabase.getInstance(context);
        db.userDao().deleteAll();
    }

    @Test
    public void testUserPersistence() {
        User user = new User("1234ab", "Bob Smith");
        db.userDao().insert(user);
        user = new User("432", "Marry Jane");
        db.userDao().insert(user);
        Assert.assertEquals(2, db.userDao().findAll().size());
    }

    @After
    public void teardown() {
        System.clearProperty("IS_TEST");
    }
}
