package com.nevilleantony.prototype.share;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.content.Context.WIFI_SERVICE;

public class ShareNetworkManager {

	private static final String TAG = "ShareNetworkManager";

	public static void connectToWifi(Context context, String ssid, String passphrase,
	                                 ConnectivityManager.NetworkCallback networkCallback) {
		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			try {
				Log.d(TAG, "Using legacy method for connecting to WiFi");
				WifiConfiguration wifiConfig = new WifiConfiguration();
				wifiConfig.SSID = "\"" + ssid + "\"";
				wifiConfig.preSharedKey = "\"" + passphrase + "\"";
				int netId = wifiManager.addNetwork(wifiConfig);
				wifiManager.disconnect();
				wifiManager.enableNetwork(netId, true);
				wifiManager.reconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
					.setSsid(ssid)
					.setWpa2Passphrase(passphrase)
					.build();

			NetworkRequest networkRequest = new NetworkRequest.Builder()
					.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
					.setNetworkSpecifier(wifiNetworkSpecifier)
					.build();

			ConnectivityManager connectivityManager =
					(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			connectivityManager.requestNetwork(networkRequest, networkCallback);
		}
	}

	public static void enableHotspot(Context context, WifiManager.LocalOnlyHotspotCallback localOnlyHotspotCallback) {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}

		WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

		if (manager != null) {
			manager.startLocalOnlyHotspot(localOnlyHotspotCallback, new Handler());
		}
	}

	public static void getDeviceList(Returnable<String> returnable) {
		Completable.fromCallable(() -> {
			try {
				Process process = Runtime.getRuntime().exec("ip neigh");
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					if (!line.contains("192.168.43.1") && line.contains("192.168.43") && line.contains("REACHABLE")) {
						Log.d(TAG, "Found device: " + line);
						break;
					}
				}

				if (line != null) {
					line = line.split(" ")[0];
				}

				returnable.returnData(line);
				bufferedReader.close();
				process.destroy();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}).subscribeOn(Schedulers.computation()).subscribe();
	}

	public interface Returnable<T> {
		void returnData(T data);
	}
}
