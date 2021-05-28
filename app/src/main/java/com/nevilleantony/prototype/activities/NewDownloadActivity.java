package com.nevilleantony.prototype.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.fragments.CreateRoomSubpage;
import com.nevilleantony.prototype.fragments.JoinRoomSubpage;
import com.nevilleantony.prototype.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class NewDownloadActivity extends AppCompatActivity {

	private Map<Integer, Fragment> fragmentsMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_download);

		fragmentsMap = new HashMap<>();
		fragmentsMap.put(R.id.create_room_button, new CreateRoomSubpage());
		fragmentsMap.put(R.id.join_room_button, new JoinRoomSubpage());

		Button createRoomButton = findViewById(R.id.create_room_button);
		Button joinRoomButton = findViewById(R.id.join_room_button);
		createRoomButton.setOnClickListener(this::onButtonClicked);
		joinRoomButton.setOnClickListener(this::onButtonClicked);
	}

	private void onButtonClicked(View view) {
		if (Utils.isLocationEnabled(this)) {
			getSupportFragmentManager().beginTransaction()
					.setReorderingAllowed(true)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.replace(R.id.new_download_subpage_container, fragmentsMap.get(view.getId()))
					.commit();
		} else {
			Utils.tryRequestLocation(this);
		}
	}
}