package com.nevilleantony.prototype.downloadmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class FileDownload {
    private static final String TAG = "FILE DOWNLOAD";
    private static final int BYTE_ARRAY_SIZE = 16 * 1024;
    public final String groupId;
    public final String fileUrl;
    public final Long partNo;
    public final Long totalFileSize;
    public final String fileName;
    private final Long minRange;
    private final Long maxRange;
    private final Handler handler;
    private final List<OnStateChangedCallback> onStateChangedCallbacks;
    HttpURLConnection urlConnection = null;
    FileOutputStream fileOutput = null;
    InputStream input = null;
    private DownloadState state;
    private int progress;
    private long downloadedSize;
    private DownloadRepo downloadRepo = null;


    FileDownload(
            String groupId,
            String fileUrl,
            String fileName,
            Long partNo,
            Long minRange,
            Long maxRange,
            Long totalFileSize
    ) {
        this.groupId = groupId;
        this.fileUrl = fileUrl;
        this.partNo = partNo;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.totalFileSize = totalFileSize;
        this.fileName = fileName;

        if (minRange.equals(maxRange)) {
            this.state = DownloadState.COMPLETED;
            progress = 100;
        } else {
            this.state = DownloadState.PAUSED;
            progress = (int) ((minRange - (maxRange - totalFileSize)) * 100 / totalFileSize);
        }
        HandlerThread downloadThread = new HandlerThread(TAG + " " + groupId);
        downloadThread.start();
        handler = new Handler(downloadThread.getLooper());

        this.downloadedSize = 0;
        onStateChangedCallbacks = new ArrayList<>();
    }


    public void startDownload(Context context) {
        if (state == DownloadState.COMPLETED) {
            return;
        }
        state = DownloadState.RUNNING;

        for (OnStateChangedCallback callback : onStateChangedCallbacks) {
            callback.onStateChanged(state);
        }
        handler.post(
                () -> {
                    @SuppressLint("DefaultLocale")
                    String rangeRequest = String.format("bytes=%d-%d", minRange + downloadedSize, maxRange);
                    try {
                        URL url = new URL(fileUrl);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestProperty("Range", rangeRequest);
                        urlConnection.connect();
                        Log.d(TAG, "Response code: " + urlConnection.getResponseCode());

                        input = urlConnection.getInputStream();
                        fileOutput = new FileOutputStream(DownloadRepo.createFile(groupId, String.valueOf(partNo)),
                                true);

                        if (minRange + downloadedSize <= maxRange) {
                            byte[] data = new byte[BYTE_ARRAY_SIZE];
                            int count;
                            while (state != DownloadState.PAUSED && (count = input.read(data)) != -1) {
                                fileOutput.write(data, 0, count);
                                downloadedSize += count;
                                progress =
                                        (int) ((minRange + downloadedSize - (maxRange - totalFileSize)) * 100 / totalFileSize);

                                for (OnStateChangedCallback callback : onStateChangedCallbacks) {
                                    callback.onProgressChanged(progress);
                                }
                            }
                        }
                        if (state == DownloadState.PAUSED) {
                            writeProgressToDB(context);
                        } else {
                            state = DownloadState.COMPLETED;
                            writeProgressToDB(context);
                            downloadRepo = DownloadRepo.getInstance(context);
                            downloadRepo.addAvailablePart(groupId, partNo);
                            for (OnStateChangedCallback callback : onStateChangedCallbacks) {
                                callback.onDownloadComplete();
                            }
                            Log.d(TAG, "Success");
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } catch (Exception e) {
                        Log.d(TAG, "Download failed due to: " + e);
                    } finally {
                        try {
                            if (input != null) {
                                input.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            if (fileOutput != null) {
                                fileOutput.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        urlConnection.disconnect();
                    }
                }
        );
    }

    public void pauseDownload() {
        state = DownloadState.PAUSED;

        for (OnStateChangedCallback callback : onStateChangedCallbacks) {
            callback.onStateChanged(state);
        }
    }

    public int getProgress() {
        return progress;
    }

    public void writeProgressToDB(Context context) {
        downloadRepo = DownloadRepo.getInstance(context);
        if (downloadRepo != null) {
            downloadRepo.updateMinRange(groupId, partNo, minRange + downloadedSize);
        }
    }

    public DownloadState getState() {
        return this.state;
    }

    public void addOnStateChangedCallback(OnStateChangedCallback callback) {
        onStateChangedCallbacks.add(callback);
    }

    public enum DownloadState {
        NOT_STARTED,
        RUNNING,
        PAUSED,
        COMPLETED;

        public static String getNotificationAction(DownloadState state) {
            String action;

            switch (state) {
                case NOT_STARTED:
                    action = "Start";
                    break;
                case RUNNING:
                    action = "Pause";
                    break;
                case PAUSED:
                    action = "Resume";
                    break;
                default:
                    action = "";
            }

            return action;
        }

        public static String toString(DownloadState state) {
            String stateString;

            switch (state) {
                case RUNNING:
                    stateString = "Downloading";
                    break;
                case PAUSED:
                    stateString = "Paused";
                    break;
                case COMPLETED:
                    stateString = "Completed";
                    break;
                default:
                    stateString = "";
            }

            return stateString;
        }
    }

    public interface OnStateChangedCallback {
        void onStateChanged(DownloadState state);

        void onDownloadComplete();

        void onProgressChanged(int progress);
    }
}