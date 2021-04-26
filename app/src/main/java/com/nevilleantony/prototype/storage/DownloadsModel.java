package com.nevilleantony.prototype.storage;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.Entity;

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
    private long range;
    private long min_range;
    private long max_range;

}
