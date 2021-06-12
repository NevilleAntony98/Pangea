package com.nevilleantony.prototype.share;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.nevilleantony.prototype.downloadmanager.DownloadRepo;
import com.nevilleantony.prototype.room.RoomRepo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShareClient {
	private static final String TAG = "ShareClient";
	private final InetSocketAddress address;
	private ShareUtils.FileShareCallback fileShareCallback;
	private Socket socket;
	private HandlerThread handlerThread;
	private Handler handler;

	public ShareClient(String address) {
		this.address = new InetSocketAddress(address, ShareServer.SERVER_PORT);
	}

	public void setFileShareCallback(ShareUtils.FileShareCallback fileShareCallback) {
		this.fileShareCallback = fileShareCallback;
	}

	public void pingServer(Runnable runnable) {
		handlerThread = new HandlerThread("FileShare Handler Thread");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
		handler.post(() -> {
			socket = new Socket();
			try {
				socket.bind(null);
				while (!socket.isConnected()) {
					try {
						socket.connect(address, 0);
					} catch (Exception e) {
						e.printStackTrace();
						Log.d(TAG, "Retry connection to server");
						// Retry connection every 2 sec
						// noinspection BusyWait
						HandlerThread.sleep(2000);
					}
				}
				Log.d(TAG, "Connected to server");
				runnable.run();
			} catch (IOException | InterruptedException e) {
				Log.d(TAG, "Failed to connect to server");
				e.printStackTrace();
			}
		});
	}

	public void start(Context context) {
		handler.post(() -> {
			if (!Thread.currentThread().isInterrupted()) {
				Log.d(TAG, "Beginning sync");

				DownloadRepo downloadRepo = DownloadRepo.getInstance(context);
				Map<String, List<Long>> serverParts;
				Map<String, List<Long>> clientParts = downloadRepo.getAvailablePartsMap();

				try (OutputStream serverOutputStream = socket.getOutputStream();
				     InputStream serverInputStream = socket.getInputStream()) {
					// Client listens to parts sent by server
					String received = ShareUtils.receiveMessage(serverInputStream);
					serverParts = ShareUtils.decodePartsMessage(received);

					// Client sends its parts to server
					String encoded = ShareUtils.encodePartsMessage(clientParts);
					ShareUtils.sendMessage(encoded, serverOutputStream);

					if (fileShareCallback != null) {
						fileShareCallback.onHandshakeSuccessful();
					}

					DataOutputStream dataOutputStream = new DataOutputStream(serverOutputStream);
					DataInputStream dataInputStream = new DataInputStream(serverInputStream);

					// Receive the files send by the server
					Map<String, List<Long>> requiredReceiveParts = ShareUtils.getRequiredParts(clientParts,
							serverParts);
					Map<String, List<Long>> requiredSendParts = ShareUtils.getRequiredParts(serverParts,
							clientParts);

					int maxProgress = 0;
					for (String key : requiredSendParts.keySet()) {
						maxProgress += Objects.requireNonNull(serverParts.get(key)).size();
					}

					for (String key : requiredReceiveParts.keySet()) {
						maxProgress += Objects.requireNonNull(serverParts.get(key)).size();
					}

					int currentProgress = 0;

					// Receive the parts
					for (String key : requiredReceiveParts.keySet()) {
						for (long part : Objects.requireNonNull(requiredReceiveParts.get(key))) {
							String hash = dataInputStream.readUTF();
							long partNumber = dataInputStream.readLong();
							long totalSize = dataInputStream.readLong();

							if (!key.equals(hash) || part != partNumber) {
								Log.e(TAG, "Mismatch in hash or part number received, invalidate this session");
							}

							String path = DownloadRepo.PATH + hash + File.separator + partNumber;
							File file = new File(path);
							if (file.exists()) {
								Log.e(TAG, "File for hash: " + hash + " and part no: " + partNumber + " already " +
										"exists" +
										". " +
										"Deleting existing file");
								if (!file.delete()) {
									Log.e(TAG, "Failed to remove existing file");
								}
							}

							ShareUtils.receiveFile(file, serverInputStream, totalSize, null);
							if (file.length() != totalSize) {
								Log.e(TAG, "File size mismatch");
							} else {
								downloadRepo.addAvailablePart(hash, partNumber);
							}

							dataOutputStream.writeUTF("SYNC");

							if (fileShareCallback != null) {
								fileShareCallback.onShareProgressChanged((++currentProgress * 100) / maxProgress);
							}
						}
					}

					// Send the parts client has
					for (String key : requiredSendParts.keySet()) {
						for (long part : Objects.requireNonNull(requiredSendParts.get(key))) {
							String path = DownloadRepo.PATH + key + File.separator + part;
							File file = new File(path);
							if (!file.exists()) {
								Log.e(TAG, "The file: " + file.getAbsolutePath() + " doesn't exist to be sent");
							}
							// Write the hash, part and file size
							dataOutputStream.writeUTF(key);
							dataOutputStream.writeLong(part);
							dataOutputStream.writeLong(file.length());
							dataOutputStream.flush();

							ShareUtils.sendFile(file, serverOutputStream, null);
							if (!dataInputStream.readUTF().equals("SYNC")) {
								Log.e(TAG, "Server and client out of sync");
							}

							if (fileShareCallback != null) {
								fileShareCallback.onShareProgressChanged((++currentProgress * 100) / maxProgress);
							}
						}
					}

					// Listen for server's closing message
					String response = dataInputStream.readUTF();
					if (!response.equals("DONE")) {
						Log.e(TAG, "Mismatch in response, this session might be corrupted");
					}

					// Send a closing message to server
					dataOutputStream.writeUTF("DONE");

					Log.d(TAG, "sync complete");
					socket.close();

					if (fileShareCallback != null) {
						fileShareCallback.onShareCompleted();
					}

					// Check if anything can be merged
					new Handler(Looper.getMainLooper()).postDelayed(() -> {
						for (String key : requiredReceiveParts.keySet()) {
							Log.d(TAG, "Checking room: " + key);
							if (downloadRepo.getAvailableParts(key).size() == RoomRepo.getRoom(key).totalPartsNo) {
								Log.d(TAG, "Merging file");
								MergeService.start(context, key);
							}
						}
					}, 5000);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			cleanup();
		});
	}

	public void sendCompleteFile(File file, ShareUtils.FileShareCallback fileShareCallback) {
		if (!file.exists()) {
			Log.e(TAG, "The given file does not exist");
			return;
		}

		handler.post(() -> {
			try (OutputStream socketOutputStream = socket.getOutputStream();
			     InputStream socketInputStream = socket.getInputStream()
			) {
				DataInputStream dataInputStream = new DataInputStream(socketInputStream);
				DataOutputStream dataOutputStream = new DataOutputStream(socketOutputStream);

				dataOutputStream.writeUTF(Objects.requireNonNull(file.getAbsoluteFile().getParentFile()).getName());
				dataOutputStream.writeUTF(file.getName());
				dataOutputStream.writeLong(file.length());

				ShareUtils.sendFile(file, socketOutputStream, fileShareCallback);

				String response = dataInputStream.readUTF();
				if (!response.equals("DONE")) {
					Log.e(TAG, "Mismatch in response, this session might be corrupted");
				}

				dataOutputStream.writeUTF("DONE");

				Log.d(TAG, "sync complete");
				socket.close();
			} catch (IOException e) {
				Log.e(TAG, "Failed to open or close one or more streams");
				e.printStackTrace();
			}

			cleanup();
		});
	}

	public void cleanup() {
		if (!socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}

		if (handlerThread != null) {
			handlerThread.quitSafely();
			handlerThread.interrupt();
		}
	}
}
