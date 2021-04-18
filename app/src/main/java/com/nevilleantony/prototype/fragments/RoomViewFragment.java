package com.nevilleantony.prototype.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.adapters.PeerListAdapter;
import com.nevilleantony.prototype.dummy.PeerListDummy;
import com.nevilleantony.prototype.peer.DefaultPeer;
import com.nevilleantony.prototype.peer.Peer;
import com.nevilleantony.prototype.room.RoomBroadcastReceiver;
import com.nevilleantony.prototype.room.RoomManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RoomViewFragment extends Fragment {
	private static final String ARG_URL = "url";
	private static final String ARG_ROOM_NAME = "download_name";
	private static final String ARG_DOWNLOAD_SIZE = "download_size";

	private RecyclerView peerListRecyclerView;
	private URL downloadURL;
	private String roomName;
	private String downloadSize;
	private RoomManager networkManager;
	private RoomBroadcastReceiver broadcastReceiver;

	public RoomViewFragment() {
		// Required empty public constructor
	}

	public static RoomViewFragment newInstance(URL url, String roomName, String downloadSize) {
		RoomViewFragment fragment = new RoomViewFragment();
		Bundle args = new Bundle();
		args.putString(ARG_URL, url.toString());
		args.putString(ARG_ROOM_NAME, roomName);
		args.putString(ARG_DOWNLOAD_SIZE, downloadSize);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			String url = getArguments().getString(ARG_URL);

			try {
				downloadURL = new URL(url);
			} catch (MalformedURLException e) {
				// TODO: Inform the user too
				e.printStackTrace();
			}

			roomName = getArguments().getString(ARG_ROOM_NAME);
			downloadSize = getArguments().getString(ARG_DOWNLOAD_SIZE);
		}

		networkManager = RoomManager.getInstance(getContext());
		broadcastReceiver = new RoomBroadcastReceiver(networkManager, getActivity());
		networkManager.initiateDiscovery(getContext(), new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Toast.makeText(getContext(), "discovery started", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(getContext(), "discovery failed", Toast.LENGTH_SHORT).show();
			}
		});
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_room_view, container, false);

		peerListRecyclerView = view.findViewById(R.id.peer_list_recycler_view);
		peerListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		peerListRecyclerView.setAdapter(new PeerListAdapter(new ArrayList<>()));
		broadcastReceiver.setOnPeersChangedCallback(deviceList -> {
			List<Peer> peers = new ArrayList<>();
			for (WifiP2pDevice device : deviceList.getDeviceList()) {
				Peer peer = new DefaultPeer(device);
				peers.add(peer);
			}

			peerListRecyclerView.swapAdapter(new PeerListAdapter(peers), false);
		});

		TextView roomLabelTextView = view.findViewById(R.id.room_name_text_view);
		roomLabelTextView.setText(roomName);

		TextInputEditText urlTextEditText = view.findViewById(R.id.room_url_edit_text);
		TextInputLayout urlTextInputLayout = view.findViewById(R.id.room_url_text_input_layout);
		urlTextInputLayout.setEndIconOnClickListener((v) -> {
			ClipboardManager clipboardManager =
					(ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clipData = ClipData.newPlainText("URL", urlTextEditText.getText());
			clipboardManager.setPrimaryClip(clipData);
			Toast.makeText(getContext(), "URL copied", Toast.LENGTH_SHORT).show();
		});

		urlTextEditText.setText(downloadURL.toString());
		urlTextInputLayout.setHelperText(downloadSize);

		return view;
	}

	private void setupPeerList() {
		List<Peer> peers = PeerListDummy.getPeerList(10);
		peerListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		peerListRecyclerView.setAdapter(new PeerListAdapter(peers));
	}
}