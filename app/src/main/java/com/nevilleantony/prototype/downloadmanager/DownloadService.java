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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class DownloadService extends Service {
    private static final int NOTIFICATION_TIMEOUT = 500;
    private static final String TAG = "Download Service";
    private static int NOTIFICATION_ID = -1;
    private static Map<String, NotificationCompat.Builder> notificationBuilderMap = new HashMap<String, NotificationCompat.Builder>();
    private static NotificationManager notificationManager = null;
    private static NotificationChannel channel = null;
    private static Map<String, Integer> notificationIdMap = new HashMap<String, Integer>();
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        NotificationChannel channel = new NotificationChannel(
                TAG,
                "Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String groupId = intent.getStringExtra("groupId");
        Log.d(TAG, "File Download ID: " + groupId);
        DownloadRepo downloadRepo = DownloadRepo.getInstance(this);
        FileDownload fileDownload = downloadRepo.getFileDownload(groupId);
        PublishSubject<Integer> publishSubject = PublishSubject.create();


        if (!notificationBuilderMap.containsKey(groupId)) {
            notificationIdMap.put(groupId, ++NOTIFICATION_ID);
            createNotification(fileDownload.fileName, fileDownload.groupId);
            fileDownload.addOnStateChangedCallback(new FileDownload.OnStateChangedCallback() {
                @Override
                public void onStateChanged(FileDownload.DownloadState state) {
                    Notification notification = notificationBuilderMap.get(fileDownload.groupId).build();
                    notification.actions[0].title = FileDownload.DownloadState.getNotificationAction(state);
                    notificationManager.notify(notificationIdMap.get(fileDownload.groupId), notification);
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
        }
        disposables.add(publishSubject.throttleLast(NOTIFICATION_TIMEOUT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(progress -> {
                    notificationBuilderMap.get(fileDownload.groupId).setProgress(100, progress, false);
                    Notification notification = notificationBuilderMap.get(fileDownload.groupId).build();
                    notification.actions[0].title =
                            FileDownload.DownloadState.getNotificationAction(fileDownload.getState());
                    notificationManager.notify(notificationIdMap.get(fileDownload.groupId), notification);
                }));
        fileDownload.startDownload(this);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        disposables.dispose();
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification(String fileName, String groupId) {
        Intent intentAction = new Intent(this, NotificationReceiver.class);
        intentAction.putExtra("groupId", groupId);
        PendingIntent actionIntent = PendingIntent.getBroadcast(this, notificationIdMap.get(groupId), intentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilderMap.put(groupId, new NotificationCompat.Builder(getApplicationContext(), TAG)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Downloading " + fileName)
                .setProgress(0, 0, false)
                .addAction(android.R.drawable.ic_media_pause, "Pause", actionIntent)
                .setOnlyAlertOnce(true));

        startForeground(notificationIdMap.get(groupId), notificationBuilderMap.get(groupId).build());
    }

}
