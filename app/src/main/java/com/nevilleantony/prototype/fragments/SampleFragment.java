package com.nevilleantony.prototype.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.nevilleantony.prototype.R;

public class SampleFragment extends Fragment {
	private static final String ARG_PARAM1 = "param1";

	private String mParam1;

	public SampleFragment() {
	}

	public static SampleFragment newInstance(String param1) {
		SampleFragment fragment = new SampleFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sample, container, false);
		Button button = view.findViewById(R.id.frag_button);
		button.setText(mParam1);
		return view;
	}
}