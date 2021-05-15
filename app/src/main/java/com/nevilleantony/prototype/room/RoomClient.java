package com.nevilleantony.prototype.room;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RoomClient {
	private static final String TAG = "RoomClient";
	// Group owner address will always be 192.168.49.1
	private static final String groupOwnerAddress = "192.168.49.1";
	private final Socket socket;
	private final OnDataReceived onDataReceived;
	private final HandlerThread handlerThread;
	private DataInputStream inputStream;
	private Handler handler;

	public RoomClient(OnDataReceived onDataReceived) {
		this.onDataReceived = onDataReceived;
		handlerThread = new HandlerThread("Server HandlerThread");
		socket = new Socket();
	}

	public void start() {
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
		handler.post(() -> {
			if (!Thread.currentThread().isInterrupted()) {
				try {
					socket.bind(null);
					socket.connect(new InetSocketAddress(groupOwnerAddress, RoomManager.SERVER_PORT_VAL));
					Log.d(TAG, "Connected to group owner: " + socket.getInetAddress());
				} catch (IOException e) {
					Log.d(TAG, "Failed to connect to group owner");
					e.printStackTrace();
				}
			}

			try {
				inputStream = new DataInputStream(socket.getInputStream());
				while (!Thread.currentThread().isInterrupted()) {
					char t = inputStream.readChar();
					MessageType messageType = MessageType.fromValue(t);
					int size = inputStream.readInt();
					byte[] messageBuffer = new byte[size];
					inputStream.readFully(messageBuffer);

					onDataReceived.dataReceived(messageType, new String(messageBuffer));
				}
			} catch (EOFException e) {
				Log.d(TAG, "Stream received unexpected EOF");
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(TAG, "Failed to get input stream from socket, or stream has been closed, or unexpectedly");
				e.printStackTrace();
			}
		});
	}

	public void tearDown() throws IOException {
		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}

		if (inputStream != null) {
			inputStream.close();
		}

		if (!socket.isClosed()) {
			socket.close();
		}

		handlerThread.quitSafely();
		handlerThread.interrupt();
	}

	/*
		Make sure that what ever processing that will be done in the callback will is posted to the
		correct thread. So if the UI needs to be updated make sure to post it to the main thread.
	 */
	public interface OnDataReceived {
		void dataReceived(MessageType messageType, String data);
	}
}
