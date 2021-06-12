package com.nevilleantony.prototype.share;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ShareUtils {
	private static final int BUFFER_SIZE = 16 * 1024;
	private static final String DOWNLOADS_DELIMITER = "@;@";
	private static final String PARTS_DELIMITER = "#;#";

	/*
	 *  Message send and received will be of the form:
	 *       (<Download><DOWNLOADS_DELIMITER>)*<Download>
	 *  Download -> (<Hash><PARTS_DELIMITER>(<PartNo><PARTS_DELIMITER>)*<PartNo>
	 *
	 *  Where,
	 *       Download represents each Hash represents hash of the download
	 *       PartNo represents the download part number corresponding to that download
	 *
	 **/
	public static Map<String, List<Long>> decodePartsMessage(String message) {
		String[] downloads = message.split(DOWNLOADS_DELIMITER);
		Map<String, List<Long>> hashPartsMap = new HashMap<>();
		for (String download : downloads) {
			String[] decodedDownload = download.split(PARTS_DELIMITER);
			String downloadHash = decodedDownload[0];
			List<Long> partNumbers = new ArrayList<>();
			for (int i = 1; i < decodedDownload.length; i++) {
				partNumbers.add(Long.parseLong(decodedDownload[i]));
			}

			hashPartsMap.put(downloadHash, partNumbers);
		}

		return hashPartsMap;
	}

	public static String encodePartsMessage(Map<String, List<Long>> hashPartsMap) {
		List<String> downloads = new ArrayList<>();
		for (String key : hashPartsMap.keySet()) {
			List<String> partsInfo = new ArrayList<>();
			partsInfo.add(key);
			partsInfo.addAll(Objects.requireNonNull(hashPartsMap.get(key))
					.stream().map(obj -> Long.toString(obj))
					.collect(Collectors.toList()));

			downloads.add(String.join(PARTS_DELIMITER, partsInfo));
		}

		return String.join(DOWNLOADS_DELIMITER, downloads);
	}

	/*
	 * Returns part numbers that the other table has but current doesn't
	 * */
	public static Map<String, List<Long>> getRequiredParts(Map<String, List<Long>> current, Map<String,
			List<Long>> other) {
		Map<String, List<Long>> remaining = new HashMap<>();
		for (String key : other.keySet()) {
			if (!current.containsKey(key)) {
				continue;
			}

			for (long partNumber : Objects.requireNonNull(other.get(key))) {
				if (Objects.requireNonNull(current.get(key)).contains(partNumber)) {
					continue;
				}

				if (!remaining.containsKey(key)) {
					remaining.put(key, new ArrayList<>());
				}

				Objects.requireNonNull(remaining.get(key)).add(partNumber);
			}
		}

		return remaining;
	}

	public static void sendFile(File file, OutputStream outputStream, FileShareCallback fileShareCallback) throws IOException {
		BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
		BufferedOutputStream socketOutputStream = new BufferedOutputStream(outputStream);

		int currentRead;
		byte[] buffer = new byte[BUFFER_SIZE];
		long totalSize = file.length();
		long totalRead = 0;
		while ((currentRead = fileInputStream.read(buffer)) != -1) {
			socketOutputStream.write(buffer, 0, currentRead);
			totalRead += currentRead;
			if (fileShareCallback != null) {
				fileShareCallback.onShareProgressChanged((int) ((totalRead * 100)/totalSize));
			}
		}

		socketOutputStream.flush();
	}

	public static void receiveFile(File file, InputStream inputStream, long totalSize, FileShareCallback fileShareCallback) throws IOException {
		BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(file));
		BufferedInputStream socketInputStream = new BufferedInputStream(inputStream);

		int currentRead;
		long totalRead = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		while (totalRead < totalSize && (currentRead = socketInputStream.read(buffer)) != -1) {
			fileOutputStream.write(buffer, 0, currentRead);
			totalRead += currentRead;
			if (fileShareCallback != null) {
				fileShareCallback.onShareProgressChanged((int) ((totalRead * 100)/totalSize));
			}
		}

		fileOutputStream.flush();
	}

	public static void sendMessage(String message, OutputStream outputStream) throws IOException {
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		dataOutputStream.writeInt(message.getBytes().length);
		dataOutputStream.writeBytes(message);
		dataOutputStream.flush();
	}

	public static String receiveMessage(InputStream inputStream) throws IOException {
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		int messageSize = dataInputStream.readInt();
		byte[] messageBuffer = new byte[messageSize];
		dataInputStream.readFully(messageBuffer);

		return new String(messageBuffer);
	}

	/*
	 * This callback will not be executed in the main thread
	 * */
	public interface FileShareCallback {
		void onHandshakeSuccessful();

		void onShareProgressChanged(int progress);

		void onShareCompleted();
	}
}
