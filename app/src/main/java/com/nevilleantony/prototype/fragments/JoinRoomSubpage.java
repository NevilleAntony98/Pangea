package com.nevilleantony.prototype.fragments;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.adapters.PeerListAdapter;
import com.nevilleantony.prototype.peer.Peer;
import com.nevilleantony.prototype.room.RoomBroadcastReceiver;
import com.nevilleantony.prototype.room.RoomManager;

import java.util.ArrayList;
import java.util.List;

public class JoinRoomSubpage extends Fragment {

	private static final String TAG = "JoinRoomSubpage";
	private RecyclerView roomsRecyclerView;
	private RoomManager roomManager;
	private RoomBroadcastReceiver broadcastReceiver;

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
				Toast.makeText(getContext(), "discovery started", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(getContext(), "discovery failed", Toast.LENGTH_SHORT).show();
			}
		});

		broadcastReceiver.setOnPeersChanged(deviceList -> {
			List<Peer> peers = Peer.getPeerList(deviceList);
			PeerListAdapter peerListAdapter = new PeerListAdapter(peers);
			peerListAdapter.setOnPeerClicked(peer -> roomManager.connect(getContext(), peer, false));
			roomsRecyclerView.setAdapter(peerListAdapter);
		});

		broadcastReceiver.setOnMembersChanged(peerList -> {

		});

		return view;
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
	public void onDestroy() {
		super.onDestroy();
		roomManager.removeGroup(new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "Successfully left the group");
			}

			@Override
			public void onFailure(int reason) {
				Log.d(TAG, "Failed to leave the group");
			}
		});
	}
}