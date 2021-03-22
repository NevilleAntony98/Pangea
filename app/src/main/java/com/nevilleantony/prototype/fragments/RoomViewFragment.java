package com.nevilleantony.prototype.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.utils.URLManager;

import java.net.MalformedURLException;
import java.net.URL;

public class RoomViewFragment extends Fragment {
	private static final String ARG_URL = "url";
	private static final String ARG_ROOM_NAME = "download_name";

	private URL downloadURL;
	private String roomName;

	public RoomViewFragment() {
		// Required empty public constructor
	}

	public static RoomViewFragment newInstance(URL url, String roomName) {
		RoomViewFragment fragment = new RoomViewFragment();
		Bundle args = new Bundle();
		args.putString(ARG_URL, url.toString());
		args.putString(ARG_ROOM_NAME, roomName);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			String url = getArguments().getString(ARG_URL);

			try {
				downloadURL = new URL(url);
			} catch (MalformedURLException e) {
				// TODO: Inform the user too
				e.printStackTrace();
			}

			roomName = getArguments().getString(ARG_ROOM_NAME);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_room_view, container, false);
		TextView roomLabelTextView = view.findViewById(R.id.room_name_text_view);
		roomLabelTextView.setText(roomName);

		TextInputEditText urlTextEditText = view.findViewById(R.id.room_url_edit_text);
		TextInputLayout urlTextInputLayout = view.findViewById(R.id.room_url_text_input_layout);
		urlTextInputLayout.setEndIconOnClickListener((v) -> {
			ClipboardManager clipboardManager =
					(ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clipData = ClipData.newPlainText("URL", urlTextEditText.getText());
			clipboardManager.setPrimaryClip(clipData);
			Toast.makeText(getContext(), "URL copied", Toast.LENGTH_SHORT).show();
		});

		urlTextEditText.setText(downloadURL.toString());

		return view;
	}
}