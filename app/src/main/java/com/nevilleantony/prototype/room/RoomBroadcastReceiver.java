package com.nevilleantony.prototype.room;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class RoomBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "RoomBroadcastReceiver";
	private final RoomManager roomManager;
	private final Activity activity;
	private OnPeersChangedCallback onPeersChangedCallback;

	public RoomBroadcastReceiver(RoomManager roomManager, Activity activity) {
		this.roomManager = roomManager;
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if Wi-Fi is enabled and notify appropriate activity
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// Wifi P2P is enabled
				Toast.makeText(context, "wifip2p enabled", Toast.LENGTH_SHORT).show();
			} else {
				// Wi-Fi P2P is not enabled
				Toast.makeText(context, "wifip2p disabled", Toast.LENGTH_SHORT).show();

			}
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// Call WifiP2pManager.requestPeers() to get a list of current peers
			Toast.makeText(context, "peers changed", Toast.LENGTH_SHORT).show();
			if (roomManager != null) {
				roomManager.requestPeers(context, peers -> onPeersChangedCallback.call(peers));
			}
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			// Respond to new connection or disconnections
			Toast.makeText(context, "connection changed", Toast.LENGTH_SHORT).show();
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			// Respond to this device's wifi state changing
			Toast.makeText(context, "current device state changed", Toast.LENGTH_SHORT).show();
		}
	}

	public void setOnPeersChangedCallback(OnPeersChangedCallback onPeersChangedCallback) {
		this.onPeersChangedCallback = onPeersChangedCallback;
	}

	public void register() {
		activity.registerReceiver(this, roomManager.intentFilter);
	}

	public void unregister() {
		activity.unregisterReceiver(this);
	}

	public static interface OnPeersChangedCallback {
		void call(WifiP2pDeviceList deviceList);
	}
}
