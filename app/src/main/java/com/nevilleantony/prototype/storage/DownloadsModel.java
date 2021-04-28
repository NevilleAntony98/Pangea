package com.nevilleantony.prototype.storage;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.PrimaryKey;
import androidx.room.Entity;
import androidx.room.Query;

import java.util.List;

//https://github.com/googlecodelabs/android-room-with-a-view/tree/master/app/src/main/java/com/example/android/roomwordssample
@androidx.room.Entity(tableName = "downloads")
public class DownloadsModel {
    //hash of the file_url + timestamp
    /*
        The primary key has to be (id,range) as a user can be reassigned to download different range,
        so to uniquely identify a row we need id, range.
     */
    @PrimaryKey
    @NonNull
    private String id;
    private String file_url;
    @PrimaryKey
    private Long range;
    private Long min_range;
    private Long max_range;


    DownloadsModel(String id, String file_url, Long range, Long min_range, Long max_range){
        this.id = id;
        this.file_url = file_url;
        this.range = range;
        this.min_range = min_range;
        this.max_range = max_range;
    }
}

