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
		long contentLength = 0;

		try {
			URL url = new URL(urlString);
			httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestProperty("Range", "bytes=0-0");

			int responseCode = httpURLConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				isReachable = true;

				String contentLengthField = httpURLConnection.getHeaderField("content-length");
				if (contentLengthField != null) {
					contentLength = Long.parseLong(contentLengthField);
				}
			}

			if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
				isReachable = true;
				canAcceptRanges = true;

				String contentRangeField = httpURLConnection.getHeaderField("content-range");
				if (contentRangeField != null) {
					String size = contentRangeField.split("/")[1];
					if (!size.equals("*")) {
						contentLength = Long.parseLong(size);
						Log.d(TAG, "getProperties: size: " + Utils.getHumanReadableSize(contentLength));
					}
				}
			}

		} catch (IOException e) {
			Log.d(TAG, "getURLProperties: Failed URL: " + urlString);
			e.printStackTrace();
		} finally {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}
		}

		return new URLProperties(isReachable, canAcceptRanges)
				.setContentLength(contentLength);
	}

	public static class URLProperties {
		public final boolean isReachable;
		public final boolean canAcceptRanges;
		private long contentLength;

		public URLProperties(boolean isReachable, boolean canAcceptRanges) {
			this.isReachable = isReachable;
			this.canAcceptRanges = canAcceptRanges;
			contentLength = 0;
		}

		public URLProperties from(URLProperties urlProperties) {
			return new URLProperties(urlProperties.isReachable, urlProperties.canAcceptRanges)
					.setContentLength(urlProperties.contentLength);
		}

		public URLProperties setContentLength(long contentLength) {
			this.contentLength = contentLength;

			return this;
		}

		public long getContentLength() {
			return contentLength;
		}
	}
}
