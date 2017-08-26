package com.michalgoly.mapify.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.michalgoly.mapify.model.TrackWrapper;
import com.michalgoly.mapify.model.User;

import java.util.List;

@Dao
public interface TrackWrapperDao {

    @Query("SELECT * FROM track_wrapper")
    List<User> findAll();

    @Query("SELECT * FROM track_wrapper WHERE id = :id")
    TrackWrapper find(String id);

    @Insert
    void insert(TrackWrapper trackWrapper);

    @Delete
    void delete(TrackWrapper trackWrapper);

    @Query("DELETE FROM track_wrapper where 1=1")
    void deleteAll();

    @Update
    void update(TrackWrapper trackWrapper);
}
