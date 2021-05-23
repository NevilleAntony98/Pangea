package com.nevilleantony.prototype.storage;


import android.content.Context;

import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;


public class StorageApi {
    final static private String SUCCESS_MESSAGE = "Success";
    private static DownloadsDatabase db = null;
    private static String downloadModel_id;
    private static String downloadModel_file_name;
    private static String downloadModel_file_url;
    private static Long downloadModel_range;
    private static DownloadsDao.RangeTuple rangeTuple;

    public static String insertRow(String id, String file_url, String file_name, Long range, Long min_range, Long max_range){
        DownloadsModel file = new DownloadsModel(id, file_url, file_name, range, min_range, max_range);
        db.getDoa().insertDownloads(file).subscribe();
        return SUCCESS_MESSAGE;
    }

    public static String retrieveFileName(String groupId, Long fileRange){
        db.getDoa().retrieveFileName(groupId, fileRange).subscribe(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> strings) throws Throwable {
                downloadModel_file_name = strings.get(0);
            }
        });
        return downloadModel_file_name;
    }

    public static String retrieveFileUrl(String groupId, Long fileRange){
        db.getDoa().retrieveFileUrl(groupId, fileRange).subscribe(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> strings) throws Throwable {
                downloadModel_file_url = strings.get(0);
            }
        });
        return downloadModel_file_url;
    }

    public static DownloadsDao.RangeTuple retrieveMinMaxRange(String groupId, Long fileRange){
        db.getDoa().retrieveMinMaxRange(groupId, fileRange).subscribe(new Consumer<List<DownloadsDao.RangeTuple>>() {
            @Override
            public void accept(List<DownloadsDao.RangeTuple> rangeTuples) throws Throwable {
                rangeTuple = rangeTuples.get(0);
            }
        });
        return rangeTuple;
    }

    public static String updateMinRange(String groupId, Long fileRange, Long minRange){
        db.getDoa().updateMinRange(groupId, fileRange, minRange).subscribe();
        return SUCCESS_MESSAGE;
    }

    public static String updateMaxRange(String groupId, Long fileRange, Long minRange){
        db.getDoa().updateMaxRange(groupId, fileRange, minRange).subscribe();
        return SUCCESS_MESSAGE;
    }

    public static void setContext(Context context){
        db = DownloadsDatabase.getInstance(context);
    }


}
