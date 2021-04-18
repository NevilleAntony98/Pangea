package com.nevilleantony.prototype.room;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.nevilleantony.prototype.peer.Peer;

public class LegacyRoomManager extends RoomManager {
	private static final String TAG = "LegacyRoomManager";

	protected LegacyRoomManager(Context context) {
		super(context);
	}

	@Override
	public void connect(AppCompatActivity context, Peer peer) {
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e(TAG, "connect: How did you even get here?");

			return;
		}

		manager.createGroup(channel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {

			}

			@Override
			public void onFailure(int reason) {

			}
		});
	}
}

