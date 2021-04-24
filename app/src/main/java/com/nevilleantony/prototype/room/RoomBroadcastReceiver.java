package com.nevilleantony.prototype.room;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import com.nevilleantony.prototype.peer.Peer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoomBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "RoomBroadcastReceiver";
	private final RoomManager roomManager;
	private final Activity activity;
	private OnPeersChanged onPeersChanged;
	private OnGroupFormed onGroupFormed;
	private OnGroupJoined onGroupJoined;
	private OnMembersChanged onMembersChanged;

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
			Log.d(TAG, "Peers changed");
			if (roomManager != null && onPeersChanged != null) {
				roomManager.requestPeers(context, peers -> onPeersChanged.call(peers));
			}

		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			// Respond to new connection or disconnections
			Log.d(TAG, "Peer connections changed");

			if (roomManager == null || onMembersChanged == null) {
				return;
			}

			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if (networkInfo != null && networkInfo.isConnected()) {
				Log.d(TAG, "Connected to a group");
				roomManager.manager.requestConnectionInfo(roomManager.channel, info -> {
					if (info.groupFormed) {
						roomManager.requestGroupInfo(context, groupInfo -> {
							Collection<WifiP2pDevice> deviceList = groupInfo.getClientList();
							Log.d(TAG, "This device is group owner: " + groupInfo.isGroupOwner());
							Peer.logPeerList(deviceList);
							List<Peer> peers = Peer.getPeerList(deviceList);

							onMembersChanged.call(peers);
						});
					}
				});
			} else {
				// Last member left causing group to be destroyed, so pass an empty list.
				onMembersChanged.call(new ArrayList<>());
			}

		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			// Respond to this device's wifi state changing
			Log.d(TAG, "This device connection changed");

			if (roomManager == null || onGroupJoined == null) {
				return;
			}

			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if (networkInfo != null && networkInfo.isConnected()) {

				roomManager.requestConnectionInfo(info -> {
					if (info.groupFormed) {
						roomManager.requestGroupInfo(context, groupInfo -> {
							WifiP2pDevice owner = groupInfo.getOwner();
							Log.d(TAG, "isOwner: " + groupInfo.isGroupOwner());

							if (onGroupFormed != null && info.isGroupOwner) {
								Log.d(TAG, "This device connected as owner");
								onGroupFormed.call(owner);
							} else if (onGroupJoined != null && !info.isGroupOwner) {
								Log.d(TAG, "This device connected as client");
								onGroupJoined.call(owner);
							}
						});
					}
				});
			}
		}
	}

	public void setOnGroupFormed(OnGroupFormed onGroupFormed) {
		this.onGroupFormed = onGroupFormed;
	}

	public void setOnPeersChanged(OnPeersChanged onPeersChanged) {
		this.onPeersChanged = onPeersChanged;
	}

	public void setOnGroupJoined(OnGroupJoined onGroupJoined) {
		this.onGroupJoined = onGroupJoined;
	}

	public void setOnMembersChanged(OnMembersChanged onMembersChanged) {
		this.onMembersChanged = onMembersChanged;
	}

	public void register() {
		activity.registerReceiver(this, roomManager.intentFilter);
	}

	public void unregister() {
		activity.unregisterReceiver(this);
	}

	public interface OnPeersChanged {
		void call(WifiP2pDeviceList deviceList);
	}

	public interface OnGroupFormed {
		void call(WifiP2pDevice owner);
	}

	public interface OnGroupJoined {
		void call(WifiP2pDevice owner);
	}

	public interface OnMembersChanged {
		void call(List<Peer> peerList);
	}
}
