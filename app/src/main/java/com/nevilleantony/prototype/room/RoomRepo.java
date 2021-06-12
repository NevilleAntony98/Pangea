package com.nevilleantony.prototype.room;

import android.content.Context;

import com.nevilleantony.prototype.storage.RoomModel;
import com.nevilleantony.prototype.storage.StorageApi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RoomRepo {
	private static final Map<String, RoomModel> rooms = new HashMap<>();
	private static StorageApi storageApi;

	public static void unloadFromDb(Context context) {
		storageApi = StorageApi.getInstance(context);
		storageApi.getAllRooms(result -> result.forEach(roomModel -> rooms.put(roomModel.id, roomModel)));
	}

	public static Collection<RoomModel> getAll() {
		return rooms.values();
	}

	public static RoomModel getRoom(String id) {
		return rooms.get(id);
	}

	public static void addRoom(String id, String url, String name, long totalSize, long totalPartsNo) {
		RoomModel room = new RoomModel(id, url, name, totalSize, totalPartsNo);
		storageApi.insertRoom(room);
		rooms.put(id, room);
	}
}
