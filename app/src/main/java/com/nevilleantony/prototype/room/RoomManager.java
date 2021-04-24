package com.nevilleantony.prototype.room;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.nevilleantony.prototype.peer.Peer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class RoomManager {
	public static final boolean LEGACY = false;
	public static final String SERVER_PORT_KEY = "server_port";
	public static final int SERVER_PORT_VAL = 12987;
	public static final String SERVICE_NAME_KEY = "service_name";
	public static final String ROOM_NAME_KEY = "room_name";
	private static final String SERVICE_NAME_VAL = "PANGAEA";
	private static final String TAG = "RoomManager";

	private static RoomManager roomManager = null;

	public final IntentFilter intentFilter;
	public final WifiP2pManager manager;
	public final WifiP2pManager.Channel channel;

	private OnRoomDiscovered onRoomDiscovered;

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

	// This only works if you are group owner
	public void requestGroupMembers(Context context, OnPeerListReceived onPeerListReceived) {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e(TAG, "requestPeers: How did you even reach here?");

			return;
		}

		requestConnectionInfo(connectionInfo -> {
			if (connectionInfo.groupFormed) {
				manager.requestGroupInfo(channel, groupInfo -> {
					List<Peer> peerList = Peer.getPeerList(groupInfo.getClientList());
					onPeerListReceived.call(peerList);
				});
			}
		});
	}

	public void registerService(Context context, String roomName) {
		Map<String, String> record = new HashMap<>();
		record.put(SERVER_PORT_KEY, String.valueOf(SERVER_PORT_VAL));
		record.put(ROOM_NAME_KEY, roomName);
		record.put(SERVICE_NAME_KEY, SERVICE_NAME_VAL);

		WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(roomName, "_presence._tcp",
				record);

		// We need to make sure our previous local services have been cleared.
		manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "How did you reach here?");

					return;
				}

				manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
					@Override
					public void onSuccess() {
						Log.d(TAG, "Successfully added local service");
					}

					@Override
					public void onFailure(int reason) {
						Log.d(TAG, "Failed to add local service. Reason:  " + reason);
					}
				});
			}

			@Override
			public void onFailure(int reason) {

			}
		});
	}

	public void setupDiscovery() {
		WifiP2pManager.DnsSdServiceResponseListener serviceListener =
				(instanceName, registrationType, srcDevice) -> Log.d(TAG,
						"Room " + instanceName + " hosted by: " + srcDevice.deviceName);

		WifiP2pManager.DnsSdTxtRecordListener textListener =
				(fullDomain, record, device) -> {
					Log.d(TAG, "DnsSdTxtRecord available: " + record.toString());

					if (onRoomDiscovered != null && record.containsKey(SERVICE_NAME_KEY) &&
							Objects.equals(record.get(SERVICE_NAME_KEY), SERVICE_NAME_VAL)) {
						onRoomDiscovered.call(record.get(ROOM_NAME_KEY), device);
					}
				};

		manager.setDnsSdResponseListeners(channel, serviceListener, textListener);
	}

	public WifiP2pDnsSdServiceRequest addServiceRequest(Context context) {
		WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

		manager.addServiceRequest(roomManager.channel,
				serviceRequest,
				new WifiP2pManager.ActionListener() {
					@Override
					public void onSuccess() {
						Log.d(TAG, "Successfully added service request");
					}

					@Override
					public void onFailure(int reason) {
						Log.d(TAG, "Failed to add service request. Reason: " + reason);
					}
				});

		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "How did you reach here?");

			return serviceRequest;
		}

		discoverServices(context);
		return serviceRequest;
	}

	public void removeGroup(WifiP2pManager.ActionListener actionListener) {
		manager.removeGroup(channel, actionListener);
	}

	public void setOnRoomDiscovered(OnRoomDiscovered onRoomDiscovered) {
		this.onRoomDiscovered = onRoomDiscovered;
	}

	private void discoverServices(Context context) {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "How did you get here?");

			return;
		}

		manager.discoverServices(roomManager.channel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "Successfully started service discovery");
			}

			@Override
			public void onFailure(int reason) {
				Log.d(TAG, "Failed to start service discovery. Reason: " + reason);
			}
		});
	}

	public interface OnRoomDiscovered {
		void call(String roomName, WifiP2pDevice device);
	}

	public interface OnPeerListReceived {
		void call(List<Peer> peerList);
	}
}
