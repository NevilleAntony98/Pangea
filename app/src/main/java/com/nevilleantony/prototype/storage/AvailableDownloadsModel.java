package com.nevilleantony.prototype.storage;

import androidx.annotation.NonNull;

//https://github.com/googlecodelabs/android-room-with-a-view/tree/master/app/src/main/java/com/example/android/roomwordssample
@androidx.room.Entity(tableName = "available_downloads", primaryKeys = {"id", "parts"})

public class AvailableDownloadsModel {
    @NonNull
    private String id;
    @NonNull
    private Long parts;

    AvailableDownloadsModel(@NonNull String id, @NonNull Long parts){
        this.id = id;
        this.parts = parts;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public Long getParts(){ return parts;}
}
