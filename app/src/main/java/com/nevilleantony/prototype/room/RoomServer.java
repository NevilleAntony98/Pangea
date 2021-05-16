package com.nevilleantony.prototype.room;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RoomServer {

	private static final String TAG = "RoomServer";
	private final ServerSocket serverSocket;
	private final Map<String, ClientHandler> clientHandlerMap;
	private final HandlerThread serverHandlerThread;
	private boolean isRunning;
	private Handler serverHandler;

	public RoomServer() throws IOException {
		serverHandlerThread = new HandlerThread("Server HandlerThread");
		serverSocket = new ServerSocket(RoomManager.SERVER_PORT_VAL);
		clientHandlerMap = new HashMap<>();
	}

	public void start() {
		serverHandlerThread.start();
		serverHandler = new Handler(serverHandlerThread.getLooper());
		serverHandler.post(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Socket client = serverSocket.accept();
					ClientHandler clientHandler = new ClientHandler(client);
					clientHandlerMap.put(client.getInetAddress().toString(), clientHandler);
					clientHandler.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		isRunning = true;
	}

	public void broadcastMessage(MessageType messageType, String message) {
		Log.d(TAG, "Broadcasting message of type: " + messageType);
		for (ClientHandler clientHandler : clientHandlerMap.values()) {
			clientHandler.sendMessage(messageType, message);
		}
	}

	public void distributeMessage(MessageType messageType, List<String> messages) throws IllegalStateException {
		if (clientHandlerMap.size() != messages.size()) {
			throw new IllegalStateException(String.format("Number of messages and clients are not equal: %s vs %s",
					messages.size(), clientHandlerMap.size()));
		}

		int index = 0;
		for (ClientHandler clientHandler : clientHandlerMap.values()) {
			clientHandler.sendMessage(messageType, messages.get(index++));
		}
	}

	public void tearDown() throws IOException {
		isRunning = false;

		for (ClientHandler clientHandler : clientHandlerMap.values()) {
			clientHandler.tearDown();
		}

		clientHandlerMap.clear();

		if (serverHandler != null) {
			serverHandler.removeCallbacksAndMessages(null);
		}

		if (!serverSocket.isClosed()) {
			serverSocket.close();
		}

		serverHandlerThread.quitSafely();
		serverHandlerThread.interrupt();
	}

	public boolean isRunning() {
		return isRunning;
	}

	private static class ClientHandler {

		private static final String TAG = "ClientHandler";
		private final Socket socket;
		private final HandlerThread handlerThread;
		private DataOutputStream outputStream;
		private Handler handler;

		public ClientHandler(Socket socket) {
			this.socket = socket;
			handlerThread = new HandlerThread("Client Handler " + UUID.randomUUID());
		}

		protected void start() {
			handlerThread.start();
			Log.d(TAG, "Client thread started for " + socket.getInetAddress());
			handler = new Handler(handlerThread.getLooper());
			handler.post(() -> {
				try {
					// Creating output stream might block
					outputStream = new DataOutputStream(socket.getOutputStream());
				} catch (IOException e) {
					Log.d(TAG, "Failed to get output stream from socket");
					e.printStackTrace();
				}
			});
		}

		public void sendMessage(MessageType messageType, String message) {
			if (handler == null) {
				Log.d(TAG, "Send message was called before creating a handler");
			} else {
				handler.post(() -> {
					if (message != null) {
						try {
							outputStream.writeChar(messageType.getCharRepr());
							outputStream.writeInt(message.getBytes().length);
							outputStream.writeBytes(message);
						} catch (IOException e) {
							Log.d(TAG, "Failed to write to client output stream");
							e.printStackTrace();
						} catch (NullPointerException e) {
							Log.d(TAG, "OutputStream is NULL");
						}
					} else {
						Log.d(TAG, "Received null message");
					}
				});
			}
		}

		public void tearDown() throws IOException {
			Log.d(TAG, "tearDown for clientHandler was called");

			if (handler != null) {
				handler.removeCallbacksAndMessages(null);
			}

			if (outputStream != null) {
				outputStream.close();
			}

			if (socket != null && !socket.isClosed()) {
				socket.close();
			}

			handlerThread.quitSafely();
			handlerThread.interrupt();
		}
	}
}
