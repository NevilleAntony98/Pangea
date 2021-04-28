package com.nevilleantony.prototype.storage;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface DownloadsDao{

    @Query("SELECT * from downloads")
    List<DownloadsModel> getAll();

    @Query("SELECT id from downloads")
    List<String> retrieveId();

    @Query("SELECT range from downloads")
    List<String> retrieveRange();

    @Query("Select file_url from downloads where id = :groupId and range = :fileRange")
    List<Long> retrieveFileUrl(String groupId, Long fileRange);

    @Query("Select min_range, max_range from downloads where id = :groupId and range = :fileRange")
    List<Long> retrieveMinMaxRange(String groupId, Long fileRange);

    @Query("update downloads set min_range =: minRange where id = :groupId and range = :Drange")
    void updateMinRange(String groupId, Long fileRange, Long minRange);


    @Query("update downloads set max_range =: maxRange where id = :groupId and range = :fileRange")
    void updateMaxRange(String groupId, Long fileRange, Long maxRange);

    @Insert
    void insertDownloads(DownloadsModel downloads);

    @Delete
    void deleteDownloads(DownloadsModel downloads);

}
