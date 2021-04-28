package com.nevilleantony.prototype.storage;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DownloadsModel.class}, version = 1)
public abstract class DownloadsDatabase extends RoomDatabase{
    public abstract DownloadsDao getDoa();
    public static DownloadsDatabase dwld_db;

    public static DownloadsDatabase getInstance(Context context){
        if(dwld_db == null){
            dwld_db = buildDatabaseInstance(context);
        }
        return dwld_db;
    }

    public static DownloadsDatabase buildDatabaseInstance(Context context){
        return Room.databaseBuilder(context, DownloadsDatabase.class, "Sample.db").allowMainThreadQueries().build();
    }

    public void cleanUp(){
        dwld_db = null;
    }
}
