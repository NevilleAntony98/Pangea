package com.nevilleantony.prototype.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.activities.RoomActivity;
import com.nevilleantony.prototype.utils.URLManager;
import com.nevilleantony.prototype.utils.Utils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class CreateRoomSubpage extends Fragment {

	private final CompositeDisposable compositeDisposable;
	private TextInputLayout textInputLayout;
	private Button launchButton;
	private URLManager.URLProperties urlProperties;
	private boolean hasLocationPermission = false;
	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				hasLocationPermission = isGranted;

				if (isGranted) {
					launchButton.performClick();
				} else {
					Toast.makeText(getContext(), "Requires location permission", Toast.LENGTH_SHORT).show();
				}
			});

	public CreateRoomSubpage() {
		compositeDisposable = new CompositeDisposable();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_create_room_subpage, container, false);
		textInputLayout = view.findViewById(R.id.url_text_input_layout);
		TextInputEditText urlEditText = view.findViewById(R.id.url_edit_text);
		TextInputEditText downloadNameEditText = view.findViewById(R.id.download_name_edit_text);

		hasLocationPermission = ContextCompat.checkSelfPermission(requireActivity(),
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

		launchButton = view.findViewById(R.id.launch_button);
		launchButton.setOnClickListener((button_view) -> {
			if (!urlProperties.isReachable)
				return;

			String url = Objects.requireNonNull(urlEditText.getText()).toString();
			String downloadName = Objects.requireNonNull(downloadNameEditText.getText()).toString();

			if (!hasLocationPermission) {
				requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
			} else {
				launchRoom(url, downloadName);
			}
		});

		Disposable disposable = RxTextView.textChanges(urlEditText)
				.skipInitialValue()
				.debounce(500, TimeUnit.MILLISECONDS)
				.subscribe(this::onTextChangeConsumer);

		compositeDisposable.add(disposable);

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		compositeDisposable.dispose();
	}

	private void launchRoom(String url, String downloadName) {
		String downloadSize = Utils.getHumanReadableSize(urlProperties.getContentLength());

		Intent intent = new Intent(getActivity(), RoomActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("room_name", downloadName);
		intent.putExtra("download_size", downloadSize);
		startActivity(intent);
	}

	private void onTextChangeConsumer(CharSequence charSequence) {
		Disposable nestedDisposable;

		if (charSequence.toString().isEmpty()) {
			urlProperties = new URLManager.URLProperties(false, false);

			nestedDisposable = Single.fromCallable(() -> {
				textInputLayout.setError(null);
				textInputLayout.setHelperText(null);
				launchButton.setEnabled(false);

				return true;
			})
					.subscribeOn(AndroidSchedulers.mainThread())
					.subscribe();

			compositeDisposable.add(nestedDisposable);

			return;
		}

		nestedDisposable = URLManager.getURLProperties(charSequence.toString())
				.subscribe((urlProperties) -> {
					if (!urlProperties.isReachable) {
						textInputLayout.setError(getString(R.string.invalid_unreachable_url));
					} else {
						textInputLayout.setError(null);
						if (!urlProperties.canAcceptRanges) {
							textInputLayout.setHelperTextTextAppearance(R.style.warningHelperText);
							textInputLayout.setHelperText(getString(R.string.partial_download_unsupported));
						} else {
							textInputLayout.setHelperTextTextAppearance(R.style.successHelperText);
							textInputLayout.setHelperText(getString(R.string.partial_download_supported));
						}
					}

					this.urlProperties = urlProperties;

					launchButton.setEnabled(urlProperties.isReachable);
				});

		compositeDisposable.add(nestedDisposable);
	}
}