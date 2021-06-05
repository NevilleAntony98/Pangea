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
    private DownloadState state = DownloadState.NOT_STARTED;
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

        HandlerThread downloadThread = new HandlerThread(TAG + " " + groupId);
        downloadThread.start();
        handler = new Handler(downloadThread.getLooper());

        this.downloadedSize = 0;
        onStateChangedCallbacks = new ArrayList<>();
    }


    public void startDownload(Context context) {
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
                            long total = downloadedSize;
                            int count;
                            while (state != DownloadState.PAUSED && (count = input.read(data)) != -1) {
                                total += count;
                                downloadedSize = total;
                                progress = (int) (total * 100 / totalFileSize);
                                for (OnStateChangedCallback callback : onStateChangedCallbacks) {
                                    callback.onProgressChanged(progress);
                                }
                                fileOutput.write(data, 0, count);
                            }
                        }
                        if (state == DownloadState.PAUSED) {
                            downloadRepo = DownloadRepo.getInstance(context);
                            if (downloadRepo != null) {
                                downloadRepo.updateMinRange(groupId, partNo, minRange + downloadedSize);
                            }
                        } else {
                            state = DownloadState.COMPLETED;
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
    }

    public interface OnStateChangedCallback {
        void onStateChanged(DownloadState state);

        void onDownloadComplete();

        void onProgressChanged(int progress);
    }
}