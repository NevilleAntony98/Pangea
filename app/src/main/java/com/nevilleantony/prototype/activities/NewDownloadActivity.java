package com.nevilleantony.prototype.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.fragments.CreateOrJoinSubpage;
import com.nevilleantony.prototype.fragments.CreateRoomSubpage;
import com.nevilleantony.prototype.fragments.JoinRoomSubpage;

public class NewDownloadActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_download);

		MaterialButtonToggleGroup buttonToggleGroup = findViewById(R.id.new_download_toggle_button_group);
		buttonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
			@Override
			public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
				Fragment fragment;
				switch (checkedId) {
					case R.id.create_room_button:
						fragment = new CreateRoomSubpage();
						break;
					case R.id.join_room_button:
						fragment = new JoinRoomSubpage();
						break;
					default:
						fragment = new CreateOrJoinSubpage();
				}

				getSupportFragmentManager().beginTransaction()
						.setReorderingAllowed(true)
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
						.replace(R.id.new_download_subpage_container, fragment)
						.commit();
			}
		});
	}
}