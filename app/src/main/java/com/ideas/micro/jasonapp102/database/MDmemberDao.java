package com.ideas.micro.jasonapp102.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MDmemberDao {


    @Query("SELECT * FROM MDmember")
    List<MDmember> getmemberjson();

    @Query("INSERT INTO MDmember(memberjson) VALUES (:mem)")
    void insertOneMem(String mem);

    @Query("UPDATE MDmember SET memberjson = :mem")
    void updateOneMem(String mem);

}
