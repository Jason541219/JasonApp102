package com.ideas.micro.jasonapp102.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "MDJsonRecord")
public class MDJsonRecord {

    // Constructor 建構式
    public MDJsonRecord(String mdjsonrecord) {
        this.mdjsonrecord = mdjsonrecord;
        this.mdrecordchecked = true;
    }

    @PrimaryKey(autoGenerate = true)
    public long uid;    // 自動產生 但是非正式資料庫欄位

    @ColumnInfo(name = "recordid")
    public long mdrecordid;

    @ColumnInfo(name = "jsonrecord")
    public String mdjsonrecord;    //

    @ColumnInfo(name = "recordChecked")
    public boolean mdrecordchecked;

    public void setMdrecordid(long localrecordid){
        this.mdrecordid = localrecordid;
    }

    public long getMdrecordid(){
        return this.mdrecordid;
    }

    public String getMdjsonrecord() {
        return mdjsonrecord;
    }

    public void setMdjsonrecord(String mdjsonrecord) {
        this.mdjsonrecord = mdjsonrecord;
    }
}