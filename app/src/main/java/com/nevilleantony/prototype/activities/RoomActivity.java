package com.nevilleantony.prototype.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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
import com.nevilleantony.prototype.downloadmanager.DownloadRepo;
import com.nevilleantony.prototype.room.MessageType;
import com.nevilleantony.prototype.room.Peer;
import com.nevilleantony.prototype.room.RoomBroadcastReceiver;
import com.nevilleantony.prototype.room.RoomClient;
import com.nevilleantony.prototype.room.RoomManager;
import com.nevilleantony.prototype.room.RoomRepo;
import com.nevilleantony.prototype.room.RoomServer;
import com.nevilleantony.prototype.room.SyncManager;
import com.nevilleantony.prototype.utils.Range;
import com.nevilleantony.prototype.utils.Utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class RoomActivity extends AppCompatActivity {
	private static final String TAG = "RoomActivity";
	private static final int REFRESH_INTERVAL_SECONDS = 5;

	private final CompositeDisposable disposables;
	private URL url;
	private String downloadSize;
	private boolean isOwner;
	private RoomManager roomManager;
	private RoomBroadcastReceiver broadcastReceiver;
	private RecyclerView peerListRecyclerView;
	private TextInputEditText urlTextEditText;
	private TextInputLayout urlTextInputLayout;
	private RoomServer roomServer;
	private RoomClient roomClient;
	private String ownerName;
	private String thisDeviceName;
	private String roomName;
	private Handler handler;

	public RoomActivity() {
		disposables = new CompositeDisposable();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room);

		roomManager = RoomManager.getInstance(this);
		broadcastReceiver = new RoomBroadcastReceiver(roomManager, this);
		broadcastReceiver.setOnThisDeviceChanged(device -> {
			thisDeviceName = device.deviceName;

			// Trigger a fake update if in owner device
			if (isOwner) {
				updatePeerList(new ArrayList<>());
			}
		});

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

			broadcastReceiver.setOnGroupFormed(owner -> {
				Log.d(TAG, "Group has been formed");

				try {
					roomServer = new RoomServer();
					roomServer.start();
				} catch (IOException e) {
					Log.d(TAG, "Failed to start create room server");
					e.printStackTrace();
				}
			});
		} else {
			broadcastReceiver.setOnGroupJoined(owner -> {
				ownerName = owner.deviceName;
				handler = new Handler(getMainLooper());
				roomClient = new RoomClient(this::onSocketResponse);
				roomClient.start();
			});
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
		if (isOwner) {
			// WifiP2PManager informs clients only to group owner so no point in listening to member changes
			// in client devices
			broadcastReceiver.setOnMembersChanged(this::updatePeerList);
		}

		Button syncButton = findViewById(R.id.sync_button);
		syncButton.setVisibility(isOwner ? View.VISIBLE : View.INVISIBLE);
		syncButton.setOnClickListener(v -> {
			PeerListAdapter adapter = (PeerListAdapter) peerListRecyclerView.getAdapter();
			if (adapter == null || adapter.getItemCount() < 2) {
				Toast.makeText(this, "No members to sync url details", Toast.LENGTH_SHORT).show();
				return;
			}

			String url = Objects.requireNonNull(urlTextEditText.getText()).toString();
			String urlDigest = Utils.getDigest(String.format("%s %s", Calendar.getInstance().getTime(), url));

			long nPeers = adapter.getItemCount();
			List<Range> ranges = SyncManager.getRanges(Long.parseLong(downloadSize), nPeers);
			// Removes a range from list to be assigned to this device
			Range myRange = ranges.remove(0);
			List<String> uniqueMessages = new ArrayList<>();

			long partNumber = 1;
			for (Range range : ranges) {
				List<String> payload = new ArrayList<>();
				payload.add(url);
				payload.add(urlDigest);
				payload.add(downloadSize);
				payload.add(range.toString());
				payload.add(Long.toString(partNumber++));
				payload.add(Long.toString(ranges.size() + 1));

				uniqueMessages.add(MessageType.encodeList(payload));
			}

			DownloadRepo.getInstance(this).createFileDownload(urlDigest, url, roomName, (long) 0, myRange.min, myRange.max,
					myRange.max - myRange.min);
			RoomRepo.addRoom(urlDigest, url, roomName, Long.parseLong(downloadSize), partNumber);
			roomServer.distributeMessage(MessageType.ROOM_SYNC, uniqueMessages);
		});

		TextView roomLabelTextView = findViewById(R.id.room_name_text_view);
		roomLabelTextView.setText(roomName);

		urlTextEditText = findViewById(R.id.room_url_edit_text);
		urlTextInputLayout = findViewById(R.id.room_url_text_input_layout);
		urlTextInputLayout.setEndIconOnClickListener((v) -> {
			ClipboardManager clipboardManager =
					(ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clipData = ClipData.newPlainText("URL", urlTextEditText.getText());
			clipboardManager.setPrimaryClip(clipData);
			Toast.makeText(getContext(), "URL copied", Toast.LENGTH_SHORT).show();
		});

		if (isOwner) {
			urlTextEditText.setText(url.toString());
			urlTextInputLayout.setHelperText(Utils.getHumanReadableSize(Long.parseLong(downloadSize)));
		}
	}

	private void subscribePeerUpdate() {
		// requestGroupInfo can work only in group owner devices
		if (isOwner) {
			disposables.add(Observable.interval(REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS)
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(aLong -> {
						if (roomManager != null && peerListRecyclerView != null) {
							roomManager.requestGroupMembers(getContext(), this::updatePeerList);
						}
					}));
		}
	}

	private void updatePeerList(List<Peer> peerList) {
		Log.d(TAG, "Member list updated");
		List<Peer> fullList = new ArrayList<>(peerList);
		if (isOwner) {
			Peer peer = new Peer(new WifiP2pDevice());
			if (thisDeviceName != null && !thisDeviceName.isEmpty()) {
				peer.setDisplayName(String.format("%s %s", thisDeviceName, "(Owner)"));
			} else {
				peer.setDisplayName("Owner");
			}

			fullList.add(0, peer);
		}

		PeerListAdapter adapter = new PeerListAdapter(fullList);
		peerListRecyclerView.setAdapter(adapter);

		if (isOwner && roomServer != null && roomServer.isRunning()) {
			roomServer.broadcastMessage(MessageType.ROOM_MEMBER_UPDATE,
					MessageType.encodeList(fullList));
		}
	}

	private void onSocketResponse(MessageType messageType, String message) {
		if (handler != null) {
			handler.post(() -> {
				if (messageType == MessageType.ROOM_MEMBER_UPDATE) {
					ArrayList<String> peers = new ArrayList<>(Arrays.asList(MessageType.decodeList(message)));
					updatePeerList(Peer.getDummyPeerList(peers));
				} else if (messageType == MessageType.ROOM_SYNC) {
					String[] urlDetails = MessageType.decodeList(message);
					String url = urlDetails[0];
					String urlHash = urlDetails[1];
					String totalSize = urlDetails[2];
					String range = urlDetails[3];
					String partNumber = urlDetails[4];
					String totalParts = urlDetails[5];

					long minRange = Long.parseLong(range.split("-")[0]);
					long maxRange = Long.parseLong(range.split("-")[1]);
					DownloadRepo.getInstance(getContext()).createFileDownload(urlHash, url, roomName,
							Long.parseLong(partNumber), minRange, maxRange, maxRange - minRange);
					RoomRepo.addRoom(urlHash, url, roomName, Long.parseLong(totalSize), Long.parseLong(totalParts));

					urlTextEditText.setText(url);
					urlTextInputLayout.setHelperText(Utils.getHumanReadableSize(Long.parseLong(totalSize)));
				}
			});
		}
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

		if (roomManager.manager != null) {
			roomManager.requestGroupInfo(getContext(), group -> {
				if (group != null && roomManager.manager != null && roomManager.channel != null)
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
			});
		}

		if (roomServer != null) {
			try {
				roomServer.tearDown();
			} catch (IOException e) {
				Log.d(TAG, "Failed to tear down server");
				e.printStackTrace();
			}
		}

		if (roomClient != null) {
			try {
				roomClient.tearDown();
			} catch (IOException e) {
				Log.d(TAG, "Failed to tear down client");
				e.printStackTrace();
			}
		}

		disposables.dispose();
	}

	private Context getContext() {
		return this;
	}
}