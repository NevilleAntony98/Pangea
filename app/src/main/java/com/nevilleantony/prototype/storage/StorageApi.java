package com.nevilleantony.prototype.storage;


import android.content.Context;

import java.util.List;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class StorageApi {
    private static StorageApi storageApi;
    private final DownloadsDatabase db;

    private StorageApi(Context context) {
        db = DownloadsDatabase.getInstance(context);
    }

    public static StorageApi getInstance(Context context) {
        if (storageApi == null) {
            storageApi = new StorageApi(context);
        }

        return storageApi;
    }

    public void insertRow(String id, String file_url, String file_name, Long range, Long min_range,
                          Long max_range, Long size) {
        DownloadsModel file = new DownloadsModel(id, file_url, file_name, range, min_range, max_range, size);
        db.getDoa().insertDownloads(file).subscribeOn(Schedulers.io()).subscribe();
    }

    public void retrieveFileName(String groupId, Long fileRange,
                                 DatabaseReturnable<String> databaseReturnable) {
        db.getDoa().retrieveFileName(groupId, fileRange).subscribeOn(Schedulers.io())
                .subscribe(strings -> databaseReturnable.returnable(strings.get(0)));
    }

    public void retrieveFileUrl(String groupId, Long fileRange,
                                DatabaseReturnable<String> databaseReturnable) {
        db.getDoa().retrieveFileUrl(groupId, fileRange).subscribeOn(Schedulers.io())
                .subscribe(strings -> databaseReturnable.returnable(strings.get(0)));
    }

    public void retrieveMinMaxRange(String groupId, Long fileRange,
                                    DatabaseReturnable<DownloadsDao.RangeTuple> databaseReturnable) {
        db.getDoa().retrieveMinMaxRange(groupId, fileRange).subscribeOn(Schedulers.io())
                .subscribe(rangeTuples -> databaseReturnable.returnable(rangeTuples.get(0)));
    }

    public void retrieveSize(String groupId, Long fileRange,
                             DatabaseReturnable<Long> databaseReturnable) {
        db.getDoa().retrieveSize(groupId, fileRange).subscribeOn(Schedulers.io())
                .subscribe(longs -> databaseReturnable.returnable(longs.get(0)));
    }

    public void updateMinRange(String groupId, Long fileRange, Long minRange) {
        db.getDoa().updateMinRange(groupId, fileRange, minRange).subscribeOn(Schedulers.io()).subscribe();
    }

    public void updateMaxRange(String groupId, Long fileRange, Long minRange) {
        db.getDoa().updateMaxRange(groupId, fileRange, minRange).subscribeOn(Schedulers.io()).subscribe();
    }

    public void getAll(DatabaseReturnable<List<DownloadsModel>> databaseReturnable) {
        db.getDoa().getAll().subscribeOn(Schedulers.io()).subscribe(databaseReturnable::returnable);
    }

    public void retrieveNameId(DatabaseReturnable<List<DownloadsDao.NameId>> databaseReturnable) {
        db.getDoa().retrieveNameId().subscribeOn(Schedulers.io()).subscribe(databaseReturnable::returnable);
    }

    public void insertAvailDownload(String id, Long parts) {
        AvailableDownloadsModel file = new AvailableDownloadsModel(id, parts);
        db.getAvailDao().insertAvailDownloads(file).subscribeOn(Schedulers.io()).subscribe();
    }

    public void retrieveParts(String groupId, DatabaseReturnable<List<Long>> databaseReturnable) {
        db.getAvailDao().retrieveParts(groupId).subscribeOn(Schedulers.io()).subscribe(databaseReturnable::returnable);
    }

    public void retrieveId(DatabaseReturnable<List<String>> databaseReturnable) {
        db.getAvailDao().retrieveId().subscribeOn(Schedulers.io()).subscribe(databaseReturnable::returnable);
    }

    public void getAvailDownload(DatabaseReturnable<List<AvailableDownloadsModel>> databaseReturnable) {
        db.getAvailDao().getAllAvailDownload().subscribeOn(Schedulers.io()).subscribe(databaseReturnable::returnable);
    }

    public interface DatabaseReturnable<T> {
        void returnable(T result);
    }
}
