package com.ideas.micro.jasonapp102.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {MDJsonRecord.class, MDmember.class}, version = 4,exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    public static final String DB_NAME = "JsonRecordData.db";   // 資料庫名稱
    private static volatile AppDatabase instance;

    public static synchronized  AppDatabase getInstance(Context context){
        if (instance == null){
            instance = create(context);
        }
        return instance;
    }

    private static AppDatabase create(Context context){
        return Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
//                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build();
    }

    public abstract MDjsonrecordDao getMDjsonrecordDao();       // 設置對外接口
    public abstract MDmemberDao getMDmemberDao();

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE MDJsonRecord ADD COLUMN recordid INTEGER");
        }
    };

}
