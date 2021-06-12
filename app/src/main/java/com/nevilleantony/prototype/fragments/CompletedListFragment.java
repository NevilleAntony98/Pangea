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

public class CompletedListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_download_cards_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        CompletedListAdapter recyclerViewAdapter = new CompletedListAdapter(view.getContext());
        recyclerView.setAdapter(recyclerViewAdapter);

        return view;


    }
}
