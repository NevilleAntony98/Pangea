package com.nevilleantony.prototype.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;


public class DownloadService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "Download Service";
    private PendingIntent actionIntent = null;
    private NotificationCompat.Builder notificationBuilder = null;
    private NotificationManager notificationManager = null;
    private Notification notification = new Notification();
    private String groupId;


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        groupId = intent.getStringExtra("groupId");
        Log.d(TAG, "File Download ID: " + groupId);
        createNotification();
        DownloadRepo downloadRepo = DownloadRepo.getInstance(this);
        FileDownload fileDownload = downloadRepo.getFileDownload(groupId);
        fileDownload.addOnStateChangedCallback(new FileDownload.OnStateChangedCallback() {
            @Override
            public void onDownloadStarted() {
                Log.d(TAG, "Download Resumed/Started");
                if (notification.actions != null) {
                    notification.actions[0].title = "Pause";
                    notificationManager.notify(NOTIFICATION_ID, notification);
                }
            }

            @Override
            public void onDownloadPaused(int progress) {
                Log.d(TAG, "Download Paused");
                notification.actions[0].title = "Resume";
                notificationManager.notify(NOTIFICATION_ID, notification);
            }

            @Override
            public void onDownloadComplete() {
                stopSelf();
                Log.d(TAG, "Download Completed");
            }

            @Override
            public void onProgressChanged(int progress) {
                notificationBuilder.setProgress(100, progress, false);
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            }
        });

        try {
            fileDownload.startDownload(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification() {
        NotificationChannel channel = new NotificationChannel(
                TAG,
                "Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Intent intentAction = new Intent(this, NotificationReceiver.class);
        intentAction.putExtra("groupId", groupId);
        actionIntent = PendingIntent.getBroadcast(this, 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), TAG)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Download")
                .setProgress(0, 0, false)
                .addAction(android.R.drawable.ic_media_pause, "Pause", actionIntent)
                .setOnlyAlertOnce(true);
        notification = notificationBuilder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

}
