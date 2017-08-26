package com.michalgoly.mapify.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

import com.michalgoly.mapify.model.TrackWrapper;
import com.michalgoly.mapify.model.User;

@Database(entities = {User.class, TrackWrapper.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";
    private static final String DB_NAME =
            System.getProperty("IS_TEST") == null ? "mapify-db" : "mapify-db-test";
    private static AppDatabase instance = null;

    public abstract UserDao userDao();
    public abstract TrackWrapperDao trackWrapperDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                    .build();
        }
        Log.i(TAG, "Getting instance of the database: " + DB_NAME);
        return instance;
    }
}
