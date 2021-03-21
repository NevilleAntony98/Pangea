package com.nevilleantony.prototype.utils;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class URLManager {
	private static final String TAG = "URLManager";

	private URLManager() {
	}

	public static Single<URLProperties> getURLProperties(String url) {
		return Single.fromCallable(() -> getProperties(url))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());
	}

	private static URLProperties getProperties(String urlString) {
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
