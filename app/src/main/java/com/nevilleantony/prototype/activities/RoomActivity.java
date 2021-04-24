package com.nevilleantony.prototype.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.adapters.PeerListAdapter;
import com.nevilleantony.prototype.room.Peer;
import com.nevilleantony.prototype.room.RoomBroadcastReceiver;
import com.nevilleantony.prototype.room.RoomManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class RoomActivity extends AppCompatActivity {
	private static final String TAG = "RoomActivity";
	private static final int REFRESH_INTERVAL_SECONDS = 5;

	private final CompositeDisposable disposables;
	private URL url;
	private String roomName;
	private String downloadSize;
	private boolean isOwner;
	private RoomManager roomManager;
	private RoomBroadcastReceiver broadcastReceiver;
	private RecyclerView peerListRecyclerView;

	public RoomActivity() {
		disposables = new CompositeDisposable();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room);

		roomManager = RoomManager.getInstance(this);
		broadcastReceiver = new RoomBroadcastReceiver(roomManager, this);

		isOwner = getIntent().getBooleanExtra("is_owner", false);
		roomName = getIntent().getStringExtra("room_name");
		if (isOwner) {
			try {
				url = new URL(getIntent().getStringExtra("url"));
			} catch (MalformedURLException e) {
				Log.d(TAG, "onCreate: Failed to create URL object. Illegal state.");
				e.printStackTrace();
			}

			downloadSize = getIntent().getStringExtra("download_size");

			roomManager.registerService(this, roomName);
		}

		roomManager.initiateDiscovery(this, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Toast.makeText(getContext(), "discovery started", Toast.LENGTH_SHORT).show();

				if (!isOwner) {
					String deviceAddress = getIntent().getStringExtra("device_address");
					WifiP2pDevice device = new WifiP2pDevice();
					device.deviceAddress = deviceAddress;
					Peer peer = new Peer(device);
					roomManager.connect(getContext(), peer, false);
				}
			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(getContext(), "discovery failed. Reason: " + reason, Toast.LENGTH_SHORT).show();
			}
		});

		peerListRecyclerView = findViewById(R.id.peer_list_recycler_view);
		peerListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		peerListRecyclerView.setAdapter(new PeerListAdapter(new ArrayList<>()));
		broadcastReceiver.setOnMembersChanged(this::updatePeerList);

		Button syncButton = findViewById(R.id.sync_button);
		syncButton.setEnabled(isOwner);

		TextView roomLabelTextView = findViewById(R.id.room_name_text_view);
		roomLabelTextView.setText(roomName);

		TextInputEditText urlTextEditText = findViewById(R.id.room_url_edit_text);
		TextInputLayout urlTextInputLayout = findViewById(R.id.room_url_text_input_layout);
		urlTextInputLayout.setEndIconOnClickListener((v) -> {
			ClipboardManager clipboardManager =
					(ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clipData = ClipData.newPlainText("URL", urlTextEditText.getText());
			clipboardManager.setPrimaryClip(clipData);
			Toast.makeText(getContext(), "URL copied", Toast.LENGTH_SHORT).show();
		});

		if (isOwner) {
			urlTextEditText.setText(url.toString());
			urlTextInputLayout.setHelperText(downloadSize);
		}
	}

	private void subscribePeerUpdate() {
		disposables.add(Observable.interval(REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(aLong -> {
					if (roomManager != null && peerListRecyclerView != null) {
						Log.d(TAG, "Periodic refresh");
						roomManager.requestGroupMembers(getContext(), this::updatePeerList);
					}
				}));
	}

	private void updatePeerList(List<Peer> peerList) {
		Log.d(TAG, "Member list updated");
		PeerListAdapter adapter = new PeerListAdapter(peerList);
		peerListRecyclerView.setAdapter(adapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		broadcastReceiver.register();
		subscribePeerUpdate();
	}

	@Override
	public void onPause() {
		super.onPause();
		broadcastReceiver.unregister();
		disposables.clear();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		roomManager.requestConnectionInfo(connectionInfo -> {
			if (connectionInfo.groupFormed) {
				roomManager.removeGroup(new WifiP2pManager.ActionListener() {
					@Override
					public void onSuccess() {
						Log.d(TAG, "Group removed successfully");
					}

					@Override
					public void onFailure(int reason) {
						Log.d(TAG, "Group removal failed. Reason: " + reason);
					}
				});

			}
		});

		disposables.dispose();
	}

	private Context getContext() {
		return (Context) this;
	}
}