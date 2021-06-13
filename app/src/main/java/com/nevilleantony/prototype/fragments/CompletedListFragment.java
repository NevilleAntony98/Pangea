package com.nevilleantony.prototype.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.adapters.CompletedListAdapter;
import com.nevilleantony.prototype.downloadmanager.DownloadRepo;

public class CompletedListFragment extends Fragment {
    public RecyclerView completedView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_download_cards_list, container, false);

        completedView = view.findViewById(R.id.recyclerView);
        completedView.setLayoutManager(new LinearLayoutManager(getActivity()));

        DownloadRepo downloadRepo = DownloadRepo.getInstance(getContext());
        CompletedListAdapter recyclerViewAdapter = new CompletedListAdapter(view.getContext(), downloadRepo.getFinishedDownloads());
        completedView.setAdapter(recyclerViewAdapter);

        return view;


    }
}
