package com.nevilleantony.prototype.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.adapters.DownloadsViewAdapter;
import com.nevilleantony.prototype.downloadmanager.DownloadRepo;

public class DownloadsListFragment extends Fragment {
    public RecyclerView downloadsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_download_cards_list, container, false);

        downloadsView = view.findViewById(R.id.recyclerView);
        downloadsView.setLayoutManager(new LinearLayoutManager(getActivity()));

        DownloadRepo downloadRepo = DownloadRepo.getInstance(getContext());
        DownloadsViewAdapter downloadsViewAdapter = new DownloadsViewAdapter(getContext(), downloadRepo.getDownloads());
        downloadsView.setAdapter(downloadsViewAdapter);

        return view;
    }
}
