package com.nevilleantony.prototype.room;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class P2pRoomManager extends RoomManager {
	private static final int OWNER = 15;
	private static final int CLIENT = 0;

	private static final String TAG = "P2pRoomManager";

	protected P2pRoomManager(Context context) {
		super(context);
	}

	// Call with isOwner = true if called from createRoom or false if from joinRoom
	@Override
	public void connect(Context context, Peer peer, boolean isOwner) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = peer.getDevice().deviceAddress;
		config.groupOwnerIntent = isOwner ? OWNER : CLIENT;

		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e(TAG, "connect: How did you even get here?");

			return;
		}

		manager.connect(channel, config, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "manager.connect: Connection initiated");
			}

			@Override
			public void onFailure(int reason) {
				Log.d(TAG, "manager.connect: Connection initiation failed. Reason: " + reason);
			}
		});
	}
}
