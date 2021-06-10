package com.nevilleantony.prototype.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.rxbinding4.material.RxBottomNavigationView;
import com.jakewharton.rxbinding4.viewpager2.RxViewPager2;
import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.adapters.ViewPagerAdapter;
import com.nevilleantony.prototype.fragments.DownloadsListFragment;
import com.nevilleantony.prototype.fragments.SampleFragment;
import com.nevilleantony.prototype.utils.Utils;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private final CompositeDisposable disposables = new CompositeDisposable();;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());

        viewPagerAdapter.addFragment(new DownloadsListFragment());
        viewPagerAdapter.addFragment(SampleFragment.newInstance("Completed Page"));

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        Disposable disposable = RxBottomNavigationView.itemSelections(bottomNavigationView)
                .subscribe(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.downloads_action:
                            viewPager.setCurrentItem(0);
                            break;
                        case R.id.finished_downloads_action:
                            viewPager.setCurrentItem(1);
                            break;
                    }
                });
        disposables.add(disposable);

        disposable = RxViewPager2.pageSelections(viewPager)
                .skipInitialValue()
                .subscribe(position -> {
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
                });
        disposables.add(disposable);

        viewPager.setAdapter(viewPagerAdapter);

        permission_request();
    }


    private void permission_request() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getApplicationContext(), "permission granted", Toast.LENGTH_SHORT);
            toast.show();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast toast = Toast.makeText(getApplicationContext(), "permission required to write", Toast.LENGTH_SHORT);
            toast.show();
        } else {

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast toast = Toast.makeText(getApplicationContext(), "Permission has been granted",
                        Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Will not function", Toast.LENGTH_SHORT);
                toast.show();

            }

        }
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
        if (view.getId() == R.id.new_download_fab) {
            Intent intent = new Intent(this, NewDownloadActivity.class);
            startActivity(intent);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                return;
            }

            if (!Utils.isLocationEnabled(this)) {
                Utils.tryRequestLocation(this);
                return;
            }

            Intent intent = new Intent(this, ShareActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }


}