package com.nevilleantony.prototype.storage;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.PrimaryKey;
import androidx.room.Entity;
import androidx.room.Query;

import java.util.List;

//https://github.com/googlecodelabs/android-room-with-a-view/tree/master/app/src/main/java/com/example/android/roomwordssample
@androidx.room.Entity(tableName = "downloads", primaryKeys = {"id", "range"})
public class DownloadsModel {
    //hash of the file_url + timestamp
    /*
        The primary key has to be (id,range) as a user can be reassigned to download different range,
        so to uniquely identify a row we need id, range.
     */
    @NonNull
    private String id;
    private String file_url;
    private String file_name;
    @NonNull
    private Long range;
    private Long min_range;
    private Long max_range;


    DownloadsModel(@NonNull String id, String file_url,String file_name,@NonNull Long range, Long min_range, Long max_range){
        this.id = id;
        this.file_url = file_url;
        this.file_name = file_name;
        this.range = range;
        this.min_range = min_range;
        this.max_range = max_range;
    }

    /*
    Getter and Setter methods can be added if needed
     */
    @NonNull
    public String getId(){ return id;}
    public String getFile_url(){ return file_url;}
    public String getFile_name(){ return file_name;}
    @NonNull
    public  Long getRange(){ return range;}
    public Long getMin_range(){ return min_range;}
    public Long getMax_range(){ return max_range;}
}

