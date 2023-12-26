package com.ideas.micro.jasonapp102.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;

@Entity (tableName = "MDmember")
public class MDmember {

    public MDmember(String mdmemberjson ){
        this.mdmemberjson = mdmemberjson;
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;    // 自動產生 但是非正式資料庫欄位

    @ColumnInfo(name = "memberjson")
    public String mdmemberjson;
}
