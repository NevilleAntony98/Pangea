package com.nevilleantony.prototype.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.nevilleantony.prototype.room.RoomBroadcastReceiver;
import com.nevilleantony.prototype.room.RoomManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class RoomActivity extends AppCompatActivity {
	private static final String TAG = "RoomActivity";

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

		try {
			url = new URL(getIntent().getStringExtra("url"));
		} catch (MalformedURLException e) {
			Log.d(TAG, "onCreate: Failed to create URL object");
			e.printStackTrace();
		}

		isOwner = getIntent().getBooleanExtra("is_owner", false);
		if (isOwner) {
			roomName = getIntent().getStringExtra("room_name");
			downloadSize = getIntent().getStringExtra("download_size");
		}

		roomManager = RoomManager.getInstance(this);
		broadcastReceiver = new RoomBroadcastReceiver(roomManager, this);
		roomManager.initiateDiscovery(this, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Toast.makeText(getContext(), "discovery started", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(getContext(), "discovery failed. Reason: " + reason, Toast.LENGTH_SHORT).show();
			}
		});

		peerListRecyclerView = findViewById(R.id.peer_list_recycler_view);
		peerListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		peerListRecyclerView.setAdapter(new PeerListAdapter(new ArrayList<>()));

		Button syncButton = findViewById(R.id.sync_button);
		syncButton.setEnabled(isOwner);

		broadcastReceiver.setOnMembersChanged(peerList -> {
			Log.d(TAG, "Member list updated");
			PeerListAdapter adapter = new PeerListAdapter(peerList);
			adapter.setOnPeerClicked(peer -> {
				roomManager.connect(getContext(), peer, true);
			});

			peerListRecyclerView.setAdapter(adapter);
		});

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

		urlTextEditText.setText(url.toString());
		urlTextInputLayout.setHelperText(downloadSize);
	}

	@Override
	public void onResume() {
		super.onResume();
		broadcastReceiver.register();
	}

	@Override
	public void onPause() {
		super.onPause();
		broadcastReceiver.unregister();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

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

		disposables.dispose();
	}

	private Context getContext() {
		return (Context) this;
	}
}