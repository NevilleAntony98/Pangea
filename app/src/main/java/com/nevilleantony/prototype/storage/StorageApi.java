package com.nevilleantony.prototype.storage;


import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;


public class StorageApi {
    final private String SUCCESS_MESSAGE = "Success";
    private final List<DownloadsModel> downloadsModels = new ArrayList<>();
    private DownloadsDatabase db = null;
    private String downloadModelFileName;
    private String downloadModelFileUrl;
    private Long downloadModelSize;
    private DownloadsDao.RangeTuple rangeTuple;
    List<Long> availParts;

    public String insertRow(Context context, String id, String file_url, String file_name, Long range, Long min_range, Long max_range, Long size) {
        db = DownloadsDatabase.getInstance(context);
        DownloadsModel file = new DownloadsModel(id, file_url, file_name, range, min_range, max_range, size);
        db.getDoa().insertDownloads(file).subscribe();
        return SUCCESS_MESSAGE;
    }

    public String retrieveFileName(Context context, String groupId, Long fileRange) {
        db = DownloadsDatabase.getInstance(context);
        db.getDoa().retrieveFileName(groupId, fileRange).subscribe(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> strings) throws Throwable {
                downloadModelFileName = strings.get(0);
            }
        });
        return downloadModelFileName;
    }

    public String retrieveFileUrl(Context context, String groupId, Long fileRange) {
        db = DownloadsDatabase.getInstance(context);
        db.getDoa().retrieveFileUrl(groupId, fileRange).subscribe(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> strings) throws Throwable {
                downloadModelFileUrl = strings.get(0);
            }
        });
        return downloadModelFileUrl;
    }

    public DownloadsDao.RangeTuple retrieveMinMaxRange(Context context, String groupId, Long fileRange) {
        db = DownloadsDatabase.getInstance(context);
        db.getDoa().retrieveMinMaxRange(groupId, fileRange).subscribe(new Consumer<List<DownloadsDao.RangeTuple>>() {
            @Override
            public void accept(List<DownloadsDao.RangeTuple> rangeTuples) throws Throwable {
                Log.d("ERROR", rangeTuples.toString());
                rangeTuple = rangeTuples.get(0);
            }
        });
        return rangeTuple;
    }

    public Long retrieveSize(Context context, String groupId, Long fileRange) {
        db = DownloadsDatabase.getInstance(context);
        db.getDoa().retrieveSize(groupId, fileRange).subscribe(new Consumer<List<Long>>() {
            @Override
            public void accept(List<Long> longs) throws Throwable {
                downloadModelSize = longs.get(0);
            }
        });
        return downloadModelSize;
    }

    public String updateMinRange(Context context, String groupId, Long fileRange, Long minRange) {
        db = DownloadsDatabase.getInstance(context);
        db.getDoa().updateMinRange(groupId, fileRange, minRange).subscribe();
        return SUCCESS_MESSAGE;
    }

    public String updateMaxRange(Context context, String groupId, Long fileRange, Long minRange) {
        db = DownloadsDatabase.getInstance(context);
        db.getDoa().updateMaxRange(groupId, fileRange, minRange).subscribe();
        return SUCCESS_MESSAGE;
    }

    public List<DownloadsModel> getAll(Context context) {
        db = DownloadsDatabase.getInstance(context);
        db.getDoa().getAll().subscribe(new Consumer<List<DownloadsModel>>() {
            @Override
            public void accept(List<DownloadsModel> downloadsModels) throws Throwable {
                downloadsModels = downloadsModels;
            }
        });
        return downloadsModels;
    }

    public String insertAvailDownload(Context context, String groupid, Long part){
        db = DownloadsDatabase.getInstance(context);
        AvailableDownloadsModel file = new AvailableDownloadsModel(groupid, part);
        db.getavaildao().insertAvailDownloads(file).subscribe();
        return SUCCESS_MESSAGE;
    }

    public List<Long> retriveParts(Context context, String groupid){
        db = DownloadsDatabase.getInstance(context);
        db.getavaildao().retrieveParts(groupid).subscribe(new Consumer<List<Long>>() {
            @Override
            public void accept(List<Long> parts) throws Throwable {
                Log.d("PART_NUM", parts.toString());
                availParts = parts;
            }
        });
        return availParts;
    }
}
