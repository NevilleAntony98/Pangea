
package com.nevilleantony.prototype.downloadmanager;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.nevilleantony.prototype.storage.AvailableDownloadsModel;
import com.nevilleantony.prototype.storage.DownloadsDao;
import com.nevilleantony.prototype.storage.DownloadsModel;
import com.nevilleantony.prototype.storage.StorageApi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class DownloadRepo {
    public static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Prototype/";
    private static final String TAG = "DownloadRepo";
    private static DownloadRepo downloadRepo = null;
    private final Map<String, FileDownload> downloadMap = new HashMap<>();
    private final StorageApi stgApi;
    private final Map<String, List<Long>> downloadPartsMap = new HashMap<>();
    private final List<OnDatabaseLoaded> onDatabaseLoadedCallbacks;
    private final Map<String, File> completedFileMap = new HashMap<>();

    private DownloadRepo(Context context) {
        onDatabaseLoadedCallbacks = new ArrayList<>();
        stgApi = StorageApi.getInstance(context);

        File path = new File(PATH);
        if (!path.exists() && !path.mkdir()) {
            Log.d(TAG, "Failed to create directory: " + path.getAbsolutePath());
        }

        stgApi.getAll(downloadsModelList -> {
            for (DownloadsModel model : downloadsModelList) {
                FileDownload fileDownload = new FileDownload(
                        model.getId(),
                        model.getFile_url(),
                        model.getFile_name(),
                        model.getRange(),
                        model.getMin_range(),
                        model.getMax_range(),
                        model.getSize()
                );
                downloadMap.put(model.getId(), fileDownload);
            }

            for (OnDatabaseLoaded callbacks : onDatabaseLoadedCallbacks) {
                callbacks.onDownloadsLoaded();
            }
        });

        stgApi.getAvailDownload(availDownloadModel -> {
            for (AvailableDownloadsModel model : availDownloadModel) {
                if (!downloadPartsMap.containsKey(model.getId())) {
                    downloadPartsMap.put(model.getId(), new ArrayList<>());
                }
                Objects.requireNonNull(downloadPartsMap.get(model.getId())).add(model.getParts());
            }

            for (OnDatabaseLoaded callbacks : onDatabaseLoadedCallbacks) {
                callbacks.onAvailableLoaded();
            }
        });

        stgApi.retrieveNameId(nameId -> {
            for (DownloadsDao.NameId model : nameId) {
                File file = new File(PATH + model.id + File.separator + model.file_name);
                if (file.exists()) {
                    completedFileMap.put(model.id, file);
                }
            }

            for (OnDatabaseLoaded callbacks : onDatabaseLoadedCallbacks) {
                callbacks.onCompletedLoaded();
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
        if (!folder.exists() && !folder.mkdir()) {
            Log.d(TAG, "Failed to create directory: " + folder.getAbsolutePath());
        }

        try {
            if (!file.exists() && !file.createNewFile()) {
                Log.d(TAG, "Failed to create file: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                fileName,
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

    public List<Long> getPartList(String groupId) {
        return downloadPartsMap.get(groupId);
    }

    public void addonPartListCallback(OnDatabaseLoaded callback) {
        onDatabaseLoadedCallbacks.add(callback);
    }

    interface OnDatabaseLoaded {
        void onAvailableLoaded();

        void onCompletedLoaded();

        void onDownloadsLoaded();
    }

}
