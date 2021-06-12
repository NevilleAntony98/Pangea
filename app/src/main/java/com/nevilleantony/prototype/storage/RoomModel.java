package com.nevilleantony.prototype.storage;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

@androidx.room.Entity(tableName = "rooms")
public class RoomModel {

	@NonNull
	@PrimaryKey
	public final String id;

	@NonNull
	public final String url;

	@NonNull
	public final String name;

	@ColumnInfo(name = "total_size")
	public final long totalSize;

	@ColumnInfo(name = "total_parts_no")
	public final long totalPartsNo;

	public RoomModel(@NonNull String id, @NonNull String url, @NonNull String name, long totalSize,
	                 long totalPartsNo) {
		this.id = id;
		this.url = url;
		this.name = name;
		this.totalSize = totalSize;
		this.totalPartsNo = totalPartsNo;
	}
}
