package com.nevilleantony.prototype.share;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.nevilleantony.prototype.downloadmanager.DownloadRepo;
import com.nevilleantony.prototype.room.RoomRepo;
import com.nevilleantony.prototype.storage.RoomModel;
import com.nevilleantony.prototype.utils.FileMerger;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class MergeService extends Service {
	private static final String TAG = "MergeService";
	private static final int NOTIFICATION_ID = 136291;
	private static final int NOTIFICATION_TIMEOUT = 500;
	private Disposable disposable;
	private RoomModel room;
	private HandlerThread handlerThread;
	private Handler handler;

	public static void start(Context context, String roomId) {
		Intent mergeIntent = new Intent(context, MergeService.class);
		mergeIntent.putExtra("room_id", roomId);
		context.startService(mergeIntent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int ret = super.onStartCommand(intent, flags, startId);

		Log.d(TAG, "Started Merge Service");

		String roomId = intent.getStringExtra("room_id");
		File file = new File(DownloadRepo.PATH + roomId);
		if (!file.exists()) {
			Log.e(TAG, "Folder does not exist for the given room ID");
			stopSelf();

			return START_NOT_STICKY;
		}

		room = RoomRepo.getRoom(roomId);
		if (room == null) {
			Log.e(TAG, "Failed to retrieve room for given ID");
			stopSelf();

			return START_NOT_STICKY;
		}

		NotificationChannel channel = new NotificationChannel(
				TAG,
				"File Merge Service Notification",
				NotificationManager.IMPORTANCE_HIGH
		);
		NotificationManager notificationManager = getSystemService(NotificationManager.class);
		notificationManager.createNotificationChannel(channel);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), TAG)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setSmallIcon(android.R.drawable.ic_dialog_info)
				.setContentTitle("Merging Files for " + room.name)
				.setProgress(0, 0, false)
				.setOnlyAlertOnce(true)
				.setOngoing(true);

		long freeSpace = (new File(DownloadRepo.PATH)).getFreeSpace() - 100 * 1024 * 1024;
		if (room.totalSize > freeSpace) {
			Notification notification =
					notificationBuilder.setContentTitle("Cannot merge, not enough space in device").setOngoing(false).build();

			notificationManager.notify(NOTIFICATION_ID, notification);
		}

		handlerThread = new HandlerThread("File Merger Thread");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());

		PublishSubject<Integer> publishSubject = PublishSubject.create();
		disposable = publishSubject.throttleLast(NOTIFICATION_TIMEOUT, TimeUnit.MILLISECONDS)
				.subscribe(progress -> notificationManager.notify(NOTIFICATION_ID,
						notificationBuilder.setProgress(100, progress, progress == 0).build()));

		publishSubject.onNext(0);

		handler.post(() -> {
			File merged = FileMerger.mergeDownload(room.id, room.name, room.totalPartsNo,
					new FileMerger.ProgressCallback() {
						@Override
						public void onTotalBytesReadChanged(long totalBytesRead) {
							publishSubject.onNext((int) ((totalBytesRead * 100) / room.totalSize));
						}

						@Override
						public void onFileMerged(int totalMerged) {
						}

						@Override
						public void onCompleted(boolean success) {
							publishSubject.onNext(100);

						}
					});

			Notification notification;
			if (merged == null || merged.length() != room.totalSize) {
				notification = notificationBuilder.setContentTitle("Failed to merged").setOngoing(false).build();
				Log.e(TAG, "Mismatch in file size, file might be corrupted");
			} else {
				notification = notificationBuilder.setContentTitle("Successfully merged " + room.name)
						.setOngoing(false).build();
				DownloadRepo.getInstance(this).addCompletedDownload(room.id, merged);
			}

			notificationManager.notify(NOTIFICATION_ID, notification);
			stopSelf();
		});

		return ret;
	}

	private void cleanup() {
		if (disposable != null && !disposable.isDisposed()) {
			disposable.dispose();
		}

		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}

		if (handlerThread != null) {
			handlerThread.quitSafely();
			handlerThread.interrupt();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		cleanup();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
