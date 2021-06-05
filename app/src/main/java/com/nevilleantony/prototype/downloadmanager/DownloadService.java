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

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class DownloadService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_TIMEOUT = 500;
    private static final String TAG = "Download Service";
    private PendingIntent actionIntent = null;
    private NotificationCompat.Builder notificationBuilder = null;
    private NotificationManager notificationManager = null;
    private Notification notification = new Notification();
    private String groupId;
    private Disposable disposable;

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
        PublishSubject<Integer> publishSubject = PublishSubject.create();
        disposable = publishSubject.throttleLast(NOTIFICATION_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribe(progress -> {
                    notificationBuilder.setProgress(100, progress, false);
                    notification = notificationBuilder.build();
                    notification.actions[0].title =
                            FileDownload.DownloadState.getNotificationAction(fileDownload.getState());
                    notificationManager.notify(NOTIFICATION_ID, notification);
                });

        fileDownload.addOnStateChangedCallback(new FileDownload.OnStateChangedCallback() {
            @Override
            public void onStateChanged(FileDownload.DownloadState state) {
                notification.actions[0].title = FileDownload.DownloadState.getNotificationAction(state);
                notificationManager.notify(NOTIFICATION_ID, notification);
                Log.d(TAG, "Download" + notification.actions[0].title);
            }

            @Override
            public void onDownloadComplete() {
                stopSelf();
                Log.d(TAG, "Download Completed");
            }

            @Override
            public void onProgressChanged(int progress) {
                publishSubject.onNext(progress);
            }
        });

        fileDownload.startDownload(this);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        disposable.dispose();
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
