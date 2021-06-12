package com.nevilleantony.prototype.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface RoomDao {

	@Insert
	Completable insert(RoomModel room);

	@Query("SELECT * FROM rooms")
	Single<List<RoomModel>> getAll();

	@Query("SELECT * FROM rooms WHERE id = :id")
	Single<RoomModel> getRoom(String id);

}
