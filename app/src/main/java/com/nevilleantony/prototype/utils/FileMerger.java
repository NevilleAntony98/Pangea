package com.nevilleantony.prototype.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class FileMerger {
	private static final String TAG = "FileMerger";

	public static void mergeFiles(List<String> partPaths, String outputPath, ProgressCallback callable) {
		if (partPaths.size() < 2) {
			Log.d(TAG, "Less than 2 file parts. No need to merge");
			return;
		}

		final int BUFFER_SIZE = 16 * 1024;

		File output = new File(outputPath);
		try {
			if (!output.createNewFile()) {
				Log.d(TAG, "A file already exists for the given output path. Aborting.");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(output))) {
			long totalBytesRead = 0;

			for (int i = 0; i < partPaths.size(); i++) {
				File part = new File(partPaths.get(i));

				try (InputStream inputStream = new BufferedInputStream(new FileInputStream(part))) {
					int bytesRead;
					byte[] buffer = new byte[BUFFER_SIZE];
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
						totalBytesRead += bytesRead;

						callable.onTotalBytesReadChanged(totalBytesRead);
					}

					callable.onFileMerged(i + 1);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "Failed to merge parts. Deleting the merged file");

			if (output.exists() && !output.delete()) {
				Log.d(TAG, "Failed to delete partially merged file");
			}

			callable.onCompleted(false);
			return;
		}

		callable.onCompleted(true);
	}

	public interface ProgressCallback {
		void onTotalBytesReadChanged(long totalBytesRead);

		void onFileMerged(int totalMerged);

		void onCompleted(boolean success);
	}
}
