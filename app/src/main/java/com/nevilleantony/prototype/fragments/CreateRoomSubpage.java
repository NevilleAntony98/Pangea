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
	private TextInputLayout urlTextInput;
	private TextInputLayout roomNameInput;
	private TextInputEditText roomNameEditText;
	private TextInputEditText urlEditText;
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
		urlTextInput = view.findViewById(R.id.url_text_input_layout);
		urlEditText = view.findViewById(R.id.url_edit_text);
		roomNameInput = view.findViewById(R.id.room_name_input_layout);
		roomNameEditText = view.findViewById(R.id.room_name_edit_text);

		hasLocationPermission = ContextCompat.checkSelfPermission(requireActivity(),
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

		launchButton = view.findViewById(R.id.launch_button);
		launchButton.setOnClickListener((button_view) -> {
			if (!urlProperties.isReachable)
				return;

			String url = Objects.requireNonNull(urlEditText.getText()).toString();
			String roomName = Objects.requireNonNull(roomNameEditText.getText()).toString();

			if (!hasLocationPermission) {
				requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
			} else {
				launchRoom(url, roomName);
			}
		});

		Disposable disposable = RxTextView.textChanges(urlEditText)
				.skipInitialValue()
				.debounce(500, TimeUnit.MILLISECONDS)
				.subscribe(this::onTextChangeConsumer);

		compositeDisposable.add(disposable);

		disposable = RxTextView.textChanges(roomNameEditText)
				.skipInitialValue()
				.debounce(200, TimeUnit.MILLISECONDS)
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
		intent.putExtra("is_owner", true);
		startActivity(intent);
	}

	private void onTextChangeConsumer(CharSequence charSequence) {
		Disposable nestedDisposable;

		if (urlEditText.toString().isEmpty()) {
			urlProperties = new URLManager.URLProperties(false, false);

			nestedDisposable = Single.fromCallable(() -> {
				urlTextInput.setError(null);
				urlTextInput.setHelperText(null);
				launchButton.setEnabled(false);

				return true;
			})
					.subscribeOn(AndroidSchedulers.mainThread())
					.subscribe();

			compositeDisposable.add(nestedDisposable);

			return;
		}

		nestedDisposable = URLManager.getURLProperties(Objects.requireNonNull(urlEditText.getText()).toString().trim())
				.subscribe((urlProperties) -> {
					if (!urlProperties.isReachable) {
						urlTextInput.setError(getString(R.string.invalid_unreachable_url));
					} else {
						urlTextInput.setError(null);
						if (!urlProperties.canAcceptRanges) {
							urlTextInput.setHelperTextTextAppearance(R.style.warningHelperText);
							urlTextInput.setHelperText(getString(R.string.partial_download_unsupported));
						} else {
							urlTextInput.setHelperTextTextAppearance(R.style.successHelperText);
							urlTextInput.setHelperText(getString(R.string.partial_download_supported));
						}
					}

					this.urlProperties = urlProperties;
					String downloadName = Objects.requireNonNull(roomNameEditText.getText()).toString().trim();
					roomNameInput.setError(downloadName.isEmpty() ? getString(R.string.non_empty_room_name_required) :
							null);

					launchButton.setEnabled(urlProperties.isReachable && !downloadName.isEmpty());
				});

		compositeDisposable.add(nestedDisposable);
	}
}