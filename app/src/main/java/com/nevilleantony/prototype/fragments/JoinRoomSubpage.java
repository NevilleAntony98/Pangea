package com.nevilleantony.prototype.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.activities.RoomActivity;
import com.nevilleantony.prototype.adapters.PeerListAdapter;
import com.nevilleantony.prototype.room.Peer;
import com.nevilleantony.prototype.room.RoomBroadcastReceiver;
import com.nevilleantony.prototype.room.RoomManager;

import java.util.ArrayList;
import java.util.List;

public class JoinRoomSubpage extends Fragment {

	private static final String TAG = "JoinRoomSubpage";
	private RecyclerView roomsRecyclerView;
	private RoomManager roomManager;
	private RoomBroadcastReceiver broadcastReceiver;
	private WifiP2pDnsSdServiceRequest serviceRequest;

	public JoinRoomSubpage() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_join_room_subpage, container, false);
		roomsRecyclerView = view.findViewById(R.id.discover_group_recycler_view);
		roomsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		roomsRecyclerView.setAdapter(new PeerListAdapter(new ArrayList<>()));

		roomManager = RoomManager.getInstance(getContext());
		broadcastReceiver = new RoomBroadcastReceiver(roomManager, getActivity());
		roomManager.initiateDiscovery(getContext(), new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Context context = getContext();
				if (context != null) {
					Toast.makeText(getContext(), "discovery started", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(int reason) {
				Context context = getContext();
				if (context != null) {
					Toast.makeText(getContext(), "discovery failed", Toast.LENGTH_SHORT).show();
				}
			}
		});

		roomManager.setupDiscovery();
		serviceRequest = roomManager.addServiceRequest(getContext());
		roomManager.setOnRoomDiscovered((roomName, device) -> {
			List<Peer> peers = Peer.getPeerList(new ArrayList<>());
			Peer roomPeer = new Peer(device);
			roomPeer.setDisplayName(roomName);
			peers.add(roomPeer);
			PeerListAdapter peerListAdapter = new PeerListAdapter(peers);
			peerListAdapter.setOnPeerClicked(peer -> {
				if (peer == null) {
					return;
				}

				joinRoom(peer.device.deviceAddress, roomName);
				roomManager.connect(getContext(), peer, false);
			});
			roomsRecyclerView.setAdapter(peerListAdapter);
		});

		return view;
	}

	private void joinRoom(String deviceAddress, String roomName) {
		Intent intent = new Intent(getActivity(), RoomActivity.class);
		intent.putExtra("is_owner", false);
		intent.putExtra("room_name", roomName);
		intent.putExtra("device_address", deviceAddress);
		startActivity(intent);
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
}