package com.nevilleantony.prototype.share;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.nevilleantony.prototype.downloadmanager.DownloadRepo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShareServer {
	public static final int SERVER_PORT = 3629;
	private static final String TAG = "ShareServer";
	private final ServerSocket serverSocket;
	private Socket client;
	private ShareUtils.FileShareCallback fileShareCallback;
	private HandlerThread handlerThread;
	private Handler handler;

	public ShareServer() throws IOException {
		serverSocket = new ServerSocket(SERVER_PORT);
	}

	public void setFileShareCallback(ShareUtils.FileShareCallback fileShareCallback) {
		this.fileShareCallback = fileShareCallback;
	}

	public void waitForClient(Runnable runnable) {
		handlerThread = new HandlerThread("File Share HandlerThread");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
		handler.post(() -> {
			try {
				Log.d(TAG, "Waiting for client");
				client = serverSocket.accept();
				Log.d(TAG, "Client has connected");
				runnable.run();
			} catch (IOException e) {
				Log.d(TAG, "Failed to accept client");
				e.printStackTrace();
			}
		});
	}

	public void start(Context context) {
		handler.post(() -> {
			if (!Thread.currentThread().isInterrupted()) {
				Log.d(TAG, "Beginning sync");

				DownloadRepo downloadRepo = DownloadRepo.getInstance(context);
				Map<String, List<Long>> serverParts = downloadRepo.getAvailablePartsMap();
				Map<String, List<Long>> clientParts;

				try (OutputStream clientOutputStream = client.getOutputStream();
				     InputStream clientInputStream = client.getInputStream()
				) {
					// First server sends the parts it has while client listens
					String encoded = ShareUtils.encodePartsMessage(serverParts);
					ShareUtils.sendMessage(encoded, clientOutputStream);

					// Then the server wait and listens for the client to send its parts
					String received = ShareUtils.receiveMessage(clientInputStream);
					clientParts = ShareUtils.decodePartsMessage(received);

					if (fileShareCallback != null) {
						fileShareCallback.onHandshakeSuccessful();
					}

					DataOutputStream dataOutputStream = new DataOutputStream(clientOutputStream);
					DataInputStream dataInputStream = new DataInputStream(clientInputStream);

					// Server then sends all the parts that it has which the client doesn't have
					Map<String, List<Long>> requiredSendParts = ShareUtils.getRequiredParts(clientParts,
							serverParts);
					Map<String, List<Long>> requiredReceiveParts = ShareUtils.getRequiredParts(serverParts,
							clientParts);

					int maxProgress = 0;
					for (String key : requiredSendParts.keySet()) {
						maxProgress += Objects.requireNonNull(serverParts.get(key)).size();
					}

					for (String key : requiredReceiveParts.keySet()) {
						maxProgress += Objects.requireNonNull(serverParts.get(key)).size();
					}

					int currentProgress = 0;
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

							ShareUtils.sendFile(file, clientOutputStream, null);
							if (!dataInputStream.readUTF().equals("SYNC")) {
								Log.e(TAG, "Server and client out of sync");
							}

							if (fileShareCallback != null) {
								fileShareCallback.onShareProgressChanged((++currentProgress * 100) / maxProgress);
							}
						}
					}

					// Server then receives all the parts the client has but the server doesn't have
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
										"exists. Deleting existing file");
								if (!file.delete()) {
									Log.e(TAG, "Failed to remove existing file");
								}
							}

							ShareUtils.receiveFile(file, clientInputStream, totalSize, null);
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

					// Server replies with a DONE once it receives everything
					dataOutputStream.writeUTF("DONE");

					// Server listens for a DONE from client and then closes connection.
					String response = dataInputStream.readUTF();
					if (!response.equals("DONE")) {
						Log.e(TAG, "Mismatch in response, this session might be corrupted");
					}

					Log.d(TAG, "sync complete");
					client.close();

					if (fileShareCallback != null) {
						fileShareCallback.onShareCompleted();
					}
				} catch (IOException e) {
					Log.e(TAG, "Failed to open or close one or more streams");
					e.printStackTrace();
				}
			}

			cleanup();
		});
	}

	public void receiveCompleteFile(ShareUtils.FileShareCallback fileShareCallback) {
		handler.post(() -> {
			try (OutputStream clientOutputStream = client.getOutputStream();
			     InputStream clientInputStream = client.getInputStream()
			) {
				DataInputStream dataInputStream = new DataInputStream(clientInputStream);
				DataOutputStream dataOutputStream = new DataOutputStream(clientOutputStream);

				String groupId = dataInputStream.readUTF();
				String fileName = dataInputStream.readUTF();
				long totalSize = dataInputStream.readLong();

				File file = new File(DownloadRepo.PATH + groupId + File.separator + fileName);

				ShareUtils.receiveFile(file, clientInputStream, totalSize, fileShareCallback);

				if (file.length() != totalSize) {
					Log.e(TAG, "Mismatch in file size");
				}

				dataOutputStream.writeUTF("DONE");

				String response = dataInputStream.readUTF();
				if (!response.equals("DONE")) {
					Log.e(TAG, "Mismatch in response, this session might be corrupted");
				}

				Log.d(TAG, "sync complete");
				client.close();
			} catch (IOException e) {
				Log.e(TAG, "Failed to open or close one or more streams");
				e.printStackTrace();
			}

			cleanup();
		});
	}

	public void cleanup() {
		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}

		try {
			if (!serverSocket.isClosed()) {
				serverSocket.close();
			}

			if (client != null && !client.isClosed()) {
				client.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (handlerThread != null) {
			handlerThread.quitSafely();
			handlerThread.interrupt();
		}
	}
}
