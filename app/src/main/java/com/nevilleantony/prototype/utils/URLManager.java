package com.nevilleantony.prototype.utils;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLManager {
	/*
	These are blocking calls, make sure to run it in a different thread.
	 */

	private static final String TAG = "URLManager";

	private URLManager() {
	}

	public static URLProperties getURLProperties(String urlString) {
		HttpURLConnection httpURLConnection = null;
		boolean isReachable = false;
		boolean canAcceptRanges = false;

		try {
			URL url = new URL(urlString);
			httpURLConnection = (HttpURLConnection) url.openConnection();
			isReachable = httpURLConnection.getResponseCode() == 200;
			if (isReachable) {
				String acceptRangesField = httpURLConnection.getHeaderField("Accept-Ranges");
				canAcceptRanges = acceptRangesField != null && acceptRangesField.equals("bytes");
			}
		} catch (IOException e) {
			Log.d(TAG, "getURLProperties: Failed URL: " + urlString);
			e.printStackTrace();
		} finally {
			if (httpURLConnection != null)
				httpURLConnection.disconnect();
		}

		return new URLProperties(isReachable, canAcceptRanges);
	}

	public static class URLProperties {
		public final boolean isReachable;
		public final boolean canAcceptRanges;

		public URLProperties(boolean isReachable, boolean canAcceptRanges) {
			this.isReachable = isReachable;
			this.canAcceptRanges = canAcceptRanges;
		}
	}
}
