package com.nevilleantony.prototype.peer;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.UUID;

public class AdminPeer extends Peer {

	public AdminPeer(WifiP2pDevice device) {
		super(device);
	}

	public AdminPeer(WifiP2pDevice device, UUID uuid) {
		super(device, uuid);
	}

	@Override
	public PeerType getType() {
		return PeerType.ADMIN;
	}
}
