package com.nevilleantony.prototype.room;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Peer {
	public final WifiP2pDevice device;
	private String displayName;

	public Peer(WifiP2pDevice device) {
		this.device = device;
	}

	public static List<Peer> getPeerList(Collection<WifiP2pDevice> deviceList) {
		List<Peer> peers = new ArrayList<>();
		for (WifiP2pDevice device : deviceList) {
			peers.add(new Peer(device));
		}

		return peers;
	}

	public static List<Peer> getPeerList(WifiP2pDeviceList deviceList) {
		List<Peer> peers = new ArrayList<>();
		for (WifiP2pDevice device : deviceList.getDeviceList()) {
			peers.add(new Peer(device));
		}

		return peers;
	}

	public static void logPeerList(WifiP2pDeviceList deviceList) {
		String TAG = "PeerLogger";
		for (WifiP2pDevice device : deviceList.getDeviceList()) {
			Log.d(TAG, "peer: " + device.deviceName);
		}
	}

	public static void logPeerList(Collection<WifiP2pDevice> deviceList) {
		String TAG = "PeerLogger";
		for (WifiP2pDevice device : deviceList) {
			Log.d(TAG, "peer: " + device.deviceName);
		}
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

	public boolean isGroupOwner() {
		return device.isGroupOwner();
	}
}
