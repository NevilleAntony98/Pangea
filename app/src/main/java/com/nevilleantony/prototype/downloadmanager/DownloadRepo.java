package com.nevilleantony.prototype.downloadmanager;

import android.content.Context;
import android.os.Environment;

import com.nevilleantony.prototype.storage.DownloadsModel;
import com.nevilleantony.prototype.storage.StorageApi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class DownloadRepo {
    private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Prototype/";
    private static DownloadRepo downloadRepo = null;
    private final Map<String, FileDownload> downloadMap = new HashMap<>();
    private final StorageApi stgApi;

    private DownloadRepo(Context context) {
        stgApi = StorageApi.getInstance(context);

        File path = new File(PATH);
        if (!path.exists()) {
            path.mkdir();
        }
        stgApi.getAll(downloadsModelList -> {
            for (DownloadsModel model : downloadsModelList) {
                FileDownload fileDownload = new FileDownload(
                        model.getId(),
                        model.getFile_url(),
                        model.getRange(),
                        model.getMin_range(),
                        model.getMax_range(),
                        model.getSize()
                );
                downloadMap.put(model.getId(), fileDownload);
            }
        });
    }

    public static DownloadRepo getInstance(Context context) {
        if (downloadRepo == null) {
            downloadRepo = new DownloadRepo(context);
        }
        return downloadRepo;
    }

    public static File createFile(String groupId, String partNo) {
        File file = new File(PATH + groupId + File.separator + partNo);
        File folder = new File(PATH + groupId);
        if (!folder.exists()) {
            folder.mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    public FileDownload createFileDownload(
            String groupId,
            String url,
            String fileName,
            Long range,
            Long minRange,
            Long maxRange,
            Long totalFileSize
    ) {
        stgApi.insertRow(
                groupId,
                url,
                fileName,
                range,
                minRange,
                maxRange,
                totalFileSize
        );

        downloadMap.put(groupId, new FileDownload(
                groupId,
                url,
                range,
                minRange,
                maxRange,
                totalFileSize
        ));
        return downloadMap.get(groupId);
    }

    public void updateMinRange(String groupId, Long range, Long minRange) {
        stgApi.updateMinRange(groupId, range, minRange);
    }

    public FileDownload getFileDownload(String groupId) {
        return downloadMap.get(groupId);
    }
}
