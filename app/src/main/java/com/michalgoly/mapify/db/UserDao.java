package com.michalgoly.mapify.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.michalgoly.mapify.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM user")
    List<User> findAll();

    @Query("SELECT * FROM user WHERE id IN (:ids)")
    List<User> findAllByIds(int[] ids);

    @Query("SELECT * FROM user WHERE id = :id")
    User find(int id);

    @Insert
    void insert(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM user where 1=1")
    void deleteAll();

    @Update
    void update(User user);

}
