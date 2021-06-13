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
import com.nevilleantony.prototype.downloadmanager.DownloadService;

import java.io.File;
import java.util.List;

public class CompletedListAdapter extends RecyclerView.Adapter<CompletedListAdapter.RecyclerViewHolder> {

    private final List<File> downloads;
    private final Intent downloadIntent;
    private final Context context;

    public CompletedListAdapter(Context context, List<File> completedList) {
        this.downloads = completedList;
        downloadIntent = new Intent(context, DownloadService.class);
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
        File fileDownload = downloads.get(position);
        holder.downloadsName.setText(fileDownload.getName());
    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView downloadsName;
        private final Button stopButton;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            downloadsName = itemView.findViewById(R.id.completed_filename);
            stopButton = itemView.findViewById(R.id.stop_download_button);
        }
    }
}
