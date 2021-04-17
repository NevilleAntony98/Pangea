package com.nevilleantony.prototype.peer;

import android.net.wifi.p2p.WifiP2pDevice;

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
}
