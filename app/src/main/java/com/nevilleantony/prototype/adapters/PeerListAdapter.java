package com.nevilleantony.prototype.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.peer.Peer;

import java.util.List;

public class PeerListAdapter extends RecyclerView.Adapter<PeerListAdapter.ViewHolder> {
	private List<Peer> peers;
	private PeerListAdapter.OnPeerClicked onPeerClicked;

	public PeerListAdapter(List<Peer> peers) {
		this.peers = peers;
	}

	public void setOnPeerClicked(PeerListAdapter.OnPeerClicked onPeerClicked) {
		this.onPeerClicked = onPeerClicked;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.layout_peer_list_item, parent, false);

		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, final int position) {
		viewHolder.getTextView().setText(peers.get(position).getDisplayName());
		if (onPeerClicked != null) {
			viewHolder.textView.setOnClickListener(view -> {
				onPeerClicked.onItemClicked(peers.get(position));
			});
		}
	}

	@Override
	public int getItemCount() {
		return peers.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView textView;

		public ViewHolder(View view) {
			super(view);

			textView = (TextView) view.findViewById(R.id.peer_item_text_view);
		}

		public TextView getTextView() {
			return textView;
		}
	}

	public interface OnPeerClicked {
		void onItemClicked(Peer peer);
	}
}
