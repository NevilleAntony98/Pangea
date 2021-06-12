package com.nevilleantony.prototype.downloadmanager;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.core.util.Pair;

import com.nevilleantony.prototype.storage.AvailableDownloadsModel;
import com.nevilleantony.prototype.storage.DownloadsDao;
import com.nevilleantony.prototype.storage.DownloadsModel;
import com.nevilleantony.prototype.storage.StorageApi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private final Map<String, List<Long>> availablePartsMap = new HashMap<>();
    private final List<OnMapChanged> onMapChangedCallbacks;
    private final Map<String, File> completedFileMap = new HashMap<>();

    private DownloadRepo(Context context) {
        onMapChangedCallbacks = new ArrayList<>();
        stgApi = StorageApi.getInstance(context);

        File path = new File(PATH);
        if (!path.exists() && !path.mkdir()) {
            Log.d(TAG, "Failed to create directory: " + path.getAbsolutePath());
        }
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

    public void unLoadDb() {
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

            for (OnMapChanged callbacks : onMapChangedCallbacks) {
                callbacks.onDownloadsMapChanged();
            }
        });

        stgApi.getAvailDownload(availDownloadModel -> {
            for (AvailableDownloadsModel model : availDownloadModel) {
                if (!availablePartsMap.containsKey(model.getId())) {
                    availablePartsMap.put(model.getId(), new ArrayList<>());
                }
                Objects.requireNonNull(availablePartsMap.get(model.getId())).add(model.getParts());
            }
        });

        refreshCompletedMap();

        stgApi.retrieveNameId(nameId -> {
            for (DownloadsDao.NameId model : nameId) {
                File file = new File(PATH + model.id + File.separator + model.file_name);
                if (file.exists()) {
                    completedFileMap.put(model.id, file);
                }
            }

            for (OnMapChanged callback : onMapChangedCallbacks) {
                callback.onCompletedMapChanged();
            }
        });
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

        for (OnMapChanged callbacks : onMapChangedCallbacks) {
            callbacks.onDownloadsMapChanged();
        }

        return downloadMap.get(groupId);
    }

    public void refreshCompletedMap() {
        stgApi.retrieveNameId(nameId -> {
            for (DownloadsDao.NameId model : nameId) {
                File file = new File(PATH + model.id + File.separator + model.file_name);
                if (file.exists()) {
                    completedFileMap.put(model.id, file);
                }
            }

            try {
                Files.walk(Paths.get(DownloadRepo.PATH), 1)
                        .skip(1)
                        .filter(path -> {
                            if (completedFileMap.containsKey(path.getFileName().toString())) {
                                return false;
                            }

                            File file = path.toFile();
                            if (!file.isDirectory()) {
                                return false;
                            }

                            File[] children = file.listFiles();
                            return children.length == 1 && children[0].isFile();
                        })
                        .map(path -> new Pair<>(path.getFileName().toString(), Objects.requireNonNull(path.toFile().listFiles())[0]))
                        .forEach(idFile -> completedFileMap.put(idFile.first, idFile.second));
            } catch (IOException e) {
                Log.d(TAG, "Failed to load non DB completed files");
                e.printStackTrace();
            }

            for (OnMapChanged callback : onMapChangedCallbacks) {
                callback.onCompletedMapChanged();
            }
        });
    }

    public void updateMinRange(String groupId, Long range, Long minRange) {
        stgApi.updateMinRange(groupId, range, minRange);
    }

    public FileDownload getFileDownload(String groupId) {
        return downloadMap.get(groupId);
    }

    public List<Long> getAvailableParts(String groupId) {
        return availablePartsMap.get(groupId);
    }

    public void addAvailablePart(String id, Long partNumber) {
        stgApi.insertAvailDownload(id, partNumber);
        if (!availablePartsMap.containsKey(id)) {
            availablePartsMap.put(id, new ArrayList<>());
        }

        Objects.requireNonNull(availablePartsMap.get(id)).add(partNumber);
    }

    public void addCompletedDownload(String groupId, File file) {
        completedFileMap.put(groupId, file);
        for (OnMapChanged callback : onMapChangedCallbacks) {
            callback.onCompletedMapChanged();
        }
    }

    public void writeAllDownloads(Context context) {
        Log.d(TAG, "Writing all download progress to database");
        for (FileDownload fileDownload : downloadMap.values()) {
            fileDownload.writeProgressToDB(context);
        }
    }

    public Map<String, List<Long>> getAvailablePartsMap() {
        return new HashMap<>(availablePartsMap);
    }

    public void addOnMapChangedCallback(OnMapChanged callback) {
        onMapChangedCallbacks.add(callback);
    }

    public List<FileDownload> getDownloads() {
        return new ArrayList<>(downloadMap.values());
    }

    public interface OnMapChanged {
        void onCompletedMapChanged();

        void onDownloadsMapChanged();
    }

    public List<File> getFinishedDownloads() {
        return new ArrayList<>(completedFileMap.values());
    }

}
