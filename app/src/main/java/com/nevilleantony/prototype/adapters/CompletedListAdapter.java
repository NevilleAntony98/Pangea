package com.nevilleantony.prototype.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.activities.ShareActivity;

import java.io.File;
import java.util.List;

public class CompletedListAdapter extends RecyclerView.Adapter<CompletedListAdapter.RecyclerViewHolder> {

    private final List<File> downloads;
    private final Context context;

    public CompletedListAdapter(Context context, List<File> completedList) {
        this.downloads = completedList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.fragment_completed_list_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        File file = downloads.get(position);
        final Intent shareIntent = new Intent(context, ShareActivity.class);

        holder.downloadsName.setText(file.getName());

        holder.shareButton.setOnClickListener(v -> {
            shareIntent.putExtra("send_path", file.getAbsolutePath());
            context.startActivity(shareIntent);
        });
    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView downloadsName;
        private final Button shareButton;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            downloadsName = itemView.findViewById(R.id.completed_filename);
            shareButton = itemView.findViewById(R.id.share_button);
        }
    }
}
