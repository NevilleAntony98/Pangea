package com.nevilleantony.prototype.dummy;

import android.net.wifi.p2p.WifiP2pDevice;

import com.nevilleantony.prototype.peer.AdminPeer;
import com.nevilleantony.prototype.peer.DefaultPeer;
import com.nevilleantony.prototype.peer.Peer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PeerListDummy {
	public static List<Peer> getPeerList(int number) {
		List<Peer> peers = new ArrayList<>();

		final String[] displayNames = {
				"GT-I5800L",
				"OnePlus9TMO",
				"SGH-I927",
				"S5T9IND",
				"OnePlus6",
				"OP46F1",
				"ZTE-Grand-S",
				"Q4T10IN",
				"Xperia Z1",
				"bbd100",
				"SOG01",
				"SonyEricssonE10a",
		};

		Peer peer = new AdminPeer(new WifiP2pDevice(), UUID.randomUUID());
		peer.setDisplayName(displayNames[0]);
		peers.add(peer);

		for (int i = 1; i < number && i < displayNames.length; i++) {
			peer = new DefaultPeer(new WifiP2pDevice(), UUID.randomUUID());
			peer.setDisplayName(displayNames[i]);

			peers.add(peer);
		}

		return peers;
	}
}
