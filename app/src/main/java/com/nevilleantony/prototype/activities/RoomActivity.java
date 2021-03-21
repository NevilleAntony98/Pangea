package com.nevilleantony.prototype.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jakewharton.rxbinding4.material.RxBottomNavigationView;
import com.jakewharton.rxbinding4.viewpager2.RxViewPager2;
import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.adapters.ViewPagerAdapter;
import com.nevilleantony.prototype.fragments.SampleFragment;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class RoomActivity extends AppCompatActivity {

	private final CompositeDisposable disposables;
	private ViewPager2 viewPager;

	public RoomActivity() {
		disposables = new CompositeDisposable();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room);

		viewPager = findViewById(R.id.room_view_pager);
		ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());

		viewPagerAdapter.addFragment(SampleFragment.newInstance("View room"));
		viewPagerAdapter.addFragment(SampleFragment.newInstance("Share room"));

		BottomNavigationView bottomNavigationView = findViewById(R.id.room_bottom_navigation_bar);
		Disposable disposable = RxBottomNavigationView.itemSelections(bottomNavigationView)
				.subscribe(menuItem -> {
					switch (menuItem.getItemId()) {
						case R.id.room_view_action:
							viewPager.setCurrentItem(0);
							break;
						case R.id.room_share_action:
							viewPager.setCurrentItem(1);
							break;
					}
				});
		disposables.add(disposable);

		disposable = RxViewPager2.pageSelections(viewPager)
				.skipInitialValue()
				.subscribe(position -> {
					int item_id = R.id.room_view_action;
					if (position == 1) {
						item_id = R.id.room_share_action;
					}

					bottomNavigationView.getMenu().findItem(item_id).setChecked(true);
				});
		disposables.add(disposable);

		viewPager.setAdapter(viewPagerAdapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		disposables.dispose();
	}
}