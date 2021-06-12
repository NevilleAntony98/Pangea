package com.nevilleantony.prototype.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.adapters.RecyclerViewAdapter;
import com.nevilleantony.prototype.downloadmanager.FileDownload;

import java.util.List;

public class DownloadsListFragment extends Fragment {
    private List<FileDownload> downloadList;

    public DownloadsListFragment(List<FileDownload> fileDownloadList) {
        this.downloadList = fileDownloadList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_download_cards_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(getContext(), downloadList);
        recyclerView.setAdapter(recyclerViewAdapter);

        return view;


    }
}
