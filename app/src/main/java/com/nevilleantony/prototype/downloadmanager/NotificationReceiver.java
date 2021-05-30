package com.nevilleantony.prototype.downloadmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;

public class NotificationReceiver extends BroadcastReceiver {
    FileDownload fileDownload = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadRepo downloadRepo = DownloadRepo.getInstance(context);
        String groupId = intent.getStringExtra("groupId");
        fileDownload = downloadRepo.getFileDownload(groupId);
        if (fileDownload.getState() == FileDownload.DownloadState.RUNNING) {
            fileDownload.pauseDownload();
        } else if (fileDownload.getState() == FileDownload.DownloadState.PAUSED) {
            try {
                fileDownload.startDownload(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
