package com.nevilleantony.prototype.storage;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface AvailableDownloadsDao {

	@Query("SELECT * from available_downloads")
	Single<List<AvailableDownloadsModel>> getAllAvailDownload();

	@Query("SELECT DISTINCT id from available_downloads")
	Single<List<String>> retrieveId();

	@Query("SELECT parts from available_downloads where id=:groupId")
	Single<List<Long>> retrieveParts(String groupId);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	Completable insertAvailDownloads(AvailableDownloadsModel availableDownload);

	@Delete
	Completable deleteAvailDownloads(AvailableDownloadsModel availableDownload);
}
