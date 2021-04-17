package com.nevilleantony.prototype.peer;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.UUID;

public class DefaultPeer extends Peer {
	public DefaultPeer(WifiP2pDevice device) {
		super(device);
	}

	public DefaultPeer(WifiP2pDevice device, UUID uuid) {
		super(device, uuid);
	}

	@Override
	public PeerType getType() {
		return PeerType.DEFAULT;
	}
}
