package com.nevilleantony.prototype.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.adapters.ViewPagerAdapter;
import com.nevilleantony.prototype.fragments.SampleFragment;

public class MainActivity extends AppCompatActivity {

	ViewPager2 viewPager;
	ViewPagerAdapter viewPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		viewPager = findViewById(R.id.view_pager);
		viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());

		viewPagerAdapter.addFragment(SampleFragment.newInstance("Downloads Page"));
		viewPagerAdapter.addFragment(SampleFragment.newInstance("Completed Page"));

		final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
		bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				switch (item.getItemId()) {
					case R.id.downloads_action:
						viewPager.setCurrentItem(0);
						break;
					case R.id.finished_downloads_action:
						viewPager.setCurrentItem(1);
						break;
				}
				return true;
			}
		});

		viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);

				int item_id = R.id.downloads_action;
				FloatingActionButton fab = findViewById(R.id.new_download_fab);

				switch (position) {
					case 0:
						fab.show();
						break;
					case 1:
						item_id = R.id.finished_downloads_action;
						fab.hide();
						break;
				}

				bottomNavigationView.getMenu().findItem(item_id).setChecked(true);
			}
		});
		viewPager.setAdapter(viewPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings_menu:
				Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
				break;
			case R.id.help_menu:
				Toast.makeText(MainActivity.this, "Help", Toast.LENGTH_SHORT).show();
				break;
		}
		return true;
	}

	public void onFABClicked(View view) {
		Intent intent = new Intent(this, NewDownloadActivity.class);
		startActivity(intent);
	}
}