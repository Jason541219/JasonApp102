package com.ideas.micro.jasonapp102.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MDjsonrecordDao {
    // 讀取所有的資料
    // MDJsonRecord 是 Table 名稱
    @Query("SELECT * FROM MDJsonRecord")
    List<MDJsonRecord> findAll();

    // 讀取一筆資料
    @Query("SELECT * FROM MDjsonrecord WHERE recordid = :recordID")
    MDJsonRecord findByID(long recordID);

    // 刪除一筆資料
    @Query("Delete FROM MDjsonrecord WHERE recordid = :uid")
    void deleteOneRecord(long uid);

    //刪除資料表
    @Query("Delete FROM MDjsonrecord")
    void delete();

    // 新增一筆資料
    @Insert
    void insertOneRecord(MDJsonRecord onerecord);


}
