package com.nevilleantony.prototype.peer;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class Peer {
	private final WifiP2pDevice device;
	private UUID uuid;
	private String displayName;

	public Peer(WifiP2pDevice device) {
		this.device = device;
		uuid = UUID.randomUUID();
	}

	public Peer(WifiP2pDevice device, UUID uuid) {
		this(device);
		this.uuid = uuid;
	}

	public UUID getId() {
		return uuid;
	}

	public String getDisplayName() {
		if (displayName != null && !displayName.equals("")) {
			return displayName;
		}

		return device.deviceName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public WifiP2pDevice getDevice() {
		return device;
	}

	abstract public PeerType getType();

	public static List<Peer> getPeerList(Collection<WifiP2pDevice> deviceList) {
		List<Peer> peers = new ArrayList<>();
		for (WifiP2pDevice device : deviceList) {
			if (device.isGroupOwner()) {
				peers.add(new AdminPeer(device));
			} else {
				peers.add(new DefaultPeer(device));
			}
		}

		return peers;
	}

	public static List<Peer> getPeerList(WifiP2pDeviceList deviceList) {
		List<Peer> peers = new ArrayList<>();
		for (WifiP2pDevice device : deviceList.getDeviceList()) {
			if (device.isGroupOwner()) {
				peers.add(new AdminPeer(device));
			} else {
				peers.add(new DefaultPeer(device));
			}
		}

		return peers;
	}

	public static void logPeerList(WifiP2pDeviceList deviceList) {
		String TAG = "PeerLogger";
		for (WifiP2pDevice device: deviceList.getDeviceList()) {
			Log.d(TAG, "peer: " + device.deviceName);
		}
	}

	public static void logPeerList(Collection<WifiP2pDevice> deviceList) {
		String TAG = "PeerLogger";
		for (WifiP2pDevice device: deviceList) {
			Log.d(TAG, "peer: " + device.deviceName);
		}
	}
}
