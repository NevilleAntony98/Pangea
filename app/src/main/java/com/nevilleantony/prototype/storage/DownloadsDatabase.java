
package com.nevilleantony.prototype.storage;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {DownloadsModel.class, AvailableDownloadsModel.class, RoomModel.class}, version = 1, exportSchema = false)
public abstract class DownloadsDatabase extends RoomDatabase {
    public static DownloadsDatabase downloadDb;

    public static DownloadsDatabase getInstance(Context context) {
        if (downloadDb == null) {
            downloadDb = Room.databaseBuilder(context, DownloadsDatabase.class, "Sample").build();
        }
        return downloadDb;
    }

    public abstract DownloadsDao getDoa();

    public abstract AvailableDownloadsDao getAvailDao();

    public abstract RoomDao getRoomDao();

    public void cleanUp() {
        downloadDb = null;
    }
}
