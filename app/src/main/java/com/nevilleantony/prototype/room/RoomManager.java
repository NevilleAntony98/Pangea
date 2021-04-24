package com.nevilleantony.prototype.room;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.nevilleantony.prototype.peer.Peer;

public abstract class RoomManager {
	public static final boolean LEGACY = false;
	private static final String TAG = "RoomManager";
	private static RoomManager roomManager = null;

	public final IntentFilter intentFilter;
	public final WifiP2pManager manager;
	public final WifiP2pManager.Channel channel;

	protected RoomManager(Context context) {
		intentFilter = setupIntentFilter();
		manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(context, Looper.getMainLooper(), null);
	}

	public static RoomManager getInstance(Context context) {
		if (roomManager == null) {
			if (LEGACY) {
				roomManager = new LegacyRoomManager(context);
			} else {
				roomManager = new P2pRoomManager(context);
			}
		}

		return roomManager;
	}

	private static IntentFilter setupIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		// Indicates a change in the Wi-Fi P2P status.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		// Indicates a change in the list of available peers.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		// Indicates the state of Wi-Fi P2P connectivity has changed.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		// Indicates this device's details have changed.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		return intentFilter;
	}

	public void initiateDiscovery(Context context, WifiP2pManager.ActionListener actionListener) {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e(TAG, "initiateDiscovery: How did you even reach here?");

			return;
		}

		manager.discoverPeers(channel, actionListener);
	}

	public abstract void connect(Context context, Peer peer, boolean isCallerOwner);

	public void requestGroupInfo(Context context, WifiP2pManager.GroupInfoListener groupInfoListener) {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e(TAG, "initiateDiscovery: How did you even reach here?");

			return;
		}

		manager.requestGroupInfo(channel, groupInfoListener);
	}

	public void requestConnectionInfo(WifiP2pManager.ConnectionInfoListener connectionInfoListener) {
		roomManager.manager.requestConnectionInfo(roomManager.channel, connectionInfoListener);
	}

	public void requestPeers(Context context, WifiP2pManager.PeerListListener listener) {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e(TAG, "requestPeers: How did you even reach here?");

			return;
		}

		manager.requestPeers(channel, listener);
	}

	public void removeGroup(WifiP2pManager.ActionListener actionListener) {
		manager.removeGroup(channel, actionListener);
	}
}
