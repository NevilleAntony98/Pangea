package com.nevilleantony.prototype.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.downloadmanager.DownloadRepo;
import com.nevilleantony.prototype.downloadmanager.FileDownload;
import com.nevilleantony.prototype.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

    private final DownloadRepo downloadRepo;
    private final List<FileDownload> downloads;

    public RecyclerViewAdapter(Context context) {
        downloads = new ArrayList<>();
        downloadRepo = DownloadRepo.getInstance(context);
        downloads.addAll(downloadRepo.getDownloads());
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.fragment_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        FileDownload fileDownload = downloads.get(position);
        holder.downloadsName.setText(fileDownload.fileName);
        holder.downloadsStatus.setText(R.string.download_status);
        holder.downloadsSize.setText(Utils.getHumanReadableSize(fileDownload.totalFileSize));
        holder.downloadsPercentage.setText(R.string.downloads_percent_initial);
        fileDownload.addOnStateChangedCallback(new FileDownload.OnStateChangedCallback() {
            @Override
            public void onStateChanged(FileDownload.DownloadState state) {
                if (state == FileDownload.DownloadState.RUNNING) {
                    holder.pauseResumeButton.setTextOff("PAUSE");
                } else if (state == FileDownload.DownloadState.PAUSED) {
                    holder.pauseResumeButton.setTextOn("RESUME");
                }
            }

            @Override
            public void onDownloadComplete() {
                holder.downloadsStatus.setText(R.string.downloads_status_completed);
            }

            @Override
            public void onProgressChanged(int progress) {
                String pg = Integer.toString(progress);
                holder.downloadsPercentage.setText(String.format("%s%%", pg));
                holder.progressBar.setProgress(progress);
            }
        });
        holder.pauseResumeButton.setOnClickListener((View view) -> {
            if (holder.pauseResumeButton.isChecked()) {
                fileDownload.pauseDownload();
            } else {

                fileDownload.startDownload(holder.downloadsName.getContext());
            }
        });

    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TextView downloadsName;
        private final TextView downloadsStatus;
        private final TextView downloadsSize;
        private final TextView downloadsPercentage;
        private final ToggleButton pauseResumeButton;
        private final ProgressBar progressBar;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            downloadsName = itemView.findViewById(R.id.download_name);
            downloadsStatus = itemView.findViewById(R.id.download_status);
            downloadsSize = itemView.findViewById(R.id.download_size);
            downloadsPercentage = itemView.findViewById(R.id.download_percentage);
            pauseResumeButton = itemView.findViewById(R.id.pause_resume_button);
            progressBar = itemView.findViewById(R.id.download_progress_bar);

        }
    }
}
