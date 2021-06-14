package com.nevilleantony.prototype.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.downloadmanager.DownloadService;
import com.nevilleantony.prototype.downloadmanager.FileDownload;
import com.nevilleantony.prototype.utils.Utils;

import java.util.List;
import java.util.Locale;

public class DownloadsViewAdapter extends RecyclerView.Adapter<DownloadsViewAdapter.DownloadsViewHolder> {

    private final List<FileDownload> downloads;
    private final Intent downloadIntent;
    private final Context context;

    public DownloadsViewAdapter(Context context, List<FileDownload> fileDownloadList) {
        this.downloads = fileDownloadList;
        downloadIntent = new Intent(context, DownloadService.class);
        this.context = context;
    }

    @NonNull
    @Override
    public DownloadsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.fragment_recyclerview_row, parent, false);
        return new DownloadsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadsViewHolder holder, int position) {
        FileDownload fileDownload = downloads.get(position);
        FileDownload.DownloadState downloadState = fileDownload.getState();

        holder.downloadsName.setText(fileDownload.fileName);
        holder.downloadsStatus.setText(FileDownload.DownloadState.toString(downloadState));
        holder.downloadsSize.setText(Utils.getHumanReadableSize(fileDownload.totalFileSize));
        holder.downloadsPercentage.setText(String.format(Locale.getDefault(), "%d%%", fileDownload.getProgress()));
        holder.progressBar.setProgress(fileDownload.getProgress(), true);
        holder.pauseResumeButton.setVisibility(downloadState == FileDownload.DownloadState.COMPLETED ?
                View.GONE :
                View.VISIBLE);

        fileDownload.addOnStateChangedCallback(new FileDownload.OnStateChangedCallback() {
            @Override
            public void onStateChanged(FileDownload.DownloadState state) {
                holder.downloadsStatus.setText(FileDownload.DownloadState.toString(state));
                holder.pauseResumeButton.setIconResource(state == FileDownload.DownloadState.RUNNING ?
                        R.drawable.ic_round_pause_24 :
                        R.drawable.ic_round_play_arrow_24);
            }

            @Override
            public void onDownloadComplete() {
                new Handler(Looper.getMainLooper()).post(() -> {
                            holder.downloadsStatus.setText(FileDownload.DownloadState.toString(fileDownload.getState()));
                            holder.pauseResumeButton.setVisibility(View.GONE);
                        }
                );
            }

            @Override
            public void onProgressChanged(int progress) {
                new Handler(Looper.getMainLooper()).post(() -> {
                            holder.downloadsPercentage.setText(String.format(Locale.getDefault(), "%d%%", progress));
                            holder.progressBar.setProgress(progress, true);
                        }
                );
            }
        });
        holder.pauseResumeButton.setOnClickListener(v -> {
            FileDownload.DownloadState currentState = fileDownload.getState();

            if (currentState == FileDownload.DownloadState.RUNNING) {
                fileDownload.pauseDownload();
                holder.pauseResumeButton.setIconResource(R.drawable.ic_round_play_arrow_24);
            } else {
                downloadIntent.putExtra("groupId", fileDownload.groupId);
                context.startService(downloadIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    public static class DownloadsViewHolder extends RecyclerView.ViewHolder {

        private final TextView downloadsName;
        private final TextView downloadsStatus;
        private final TextView downloadsSize;
        private final TextView downloadsPercentage;
        private final MaterialButton pauseResumeButton;
        private final LinearProgressIndicator progressBar;

        public DownloadsViewHolder(@NonNull View itemView) {
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
