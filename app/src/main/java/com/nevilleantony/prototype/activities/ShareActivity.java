package com.nevilleantony.prototype.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.nevilleantony.prototype.R;
import com.nevilleantony.prototype.share.ShareClient;
import com.nevilleantony.prototype.share.ShareNetworkManager;
import com.nevilleantony.prototype.share.ShareServer;
import com.nevilleantony.prototype.share.ShareUtils;
import com.nevilleantony.prototype.utils.QRCodeManager;
import com.nevilleantony.prototype.utils.Utils;

import java.io.IOException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Pair;

public class ShareActivity extends AppCompatActivity {

	private static final String TAG = "ShareActivity";
	private MaterialButtonToggleGroup toggleButton;
	private LinearProgressIndicator progressBar;
	private ImageView qrImageView;
	private Button startShareButton;
	private TextView shareQRText;
	private HandlerThread handlerThread;
	private Handler handler;
	private ShareServer shareServer;
	private ShareClient shareClient;
	private ConnectionType connectionType;
	private WifiManager.LocalOnlyHotspotReservation reservation;
	private Disposable disposable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		toggleButton = findViewById(R.id.share_toggle_button);
		qrImageView = findViewById(R.id.share_qr_image);
		startShareButton = findViewById(R.id.start_share_button);
		shareQRText = findViewById(R.id.share_qr_text);
		progressBar = findViewById(R.id.share_progress_indicator);
		progressBar.setIndeterminate(true);
		Button newRoomButton = findViewById(R.id.share_new_room_button);
		Button joinRoomButton = findViewById(R.id.share_join_room_button);

		newRoomButton.setOnClickListener(this::onNewRoomClicked);
		joinRoomButton.setOnClickListener(this::onJoinRoomClicked);

		startShareButton.setOnClickListener(v -> {
			progressBar.setVisibility(View.VISIBLE);

			ShareUtils.FileShareCallback fileShareCallback = new ShareUtils.FileShareCallback() {
				@Override
				public void onHandshakeSuccessful() {
					new Handler(Looper.getMainLooper()).post(() -> {
						startShareButton.setText(R.string.sharing_sync_info);
						progressBar.setIndeterminate(false);
						progressBar.setProgress(0, true);
					});
				}

				@Override
				public void onShareProgressChanged(int progress) {
					new Handler(Looper.getMainLooper()).post(() -> progressBar.setProgress(progress, true));
				}

				@Override
				public void onShareCompleted() {
					new Handler(Looper.getMainLooper()).post(() -> {
						startShareButton.setText(R.string.done);
						startShareButton.setEnabled(true);
						startShareButton.setOnClickListener(null);
						progressBar.setProgress(100, true);
					});
				}
			};

			if (connectionType == ConnectionType.HOTSPOT_OWNER) {
				shareClient.setFileShareCallback(fileShareCallback);
				shareClient.start(this);
			} else {
				shareServer.setFileShareCallback(fileShareCallback);
				shareServer.start(this);
			}

			startShareButton.setText(R.string.syncing);
			startShareButton.setEnabled(false);
		});
	}

	private void onNewRoomClicked(View view) {
		toggleButton.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setIndeterminate(true);
		connectionType = ConnectionType.HOTSPOT_OWNER;

		ShareNetworkManager.enableHotspot(this, new WifiManager.LocalOnlyHotspotCallback() {
			@Override
			public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
				super.onStarted(reservation);

				((ShareActivity) getContext()).reservation = reservation;
				if (reservation == null) {
					Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
					return;
				}

				String encoded = Utils.encodeWifi(reservation);
				int length = qrImageView.getHeight();
				disposable = Single.fromCallable(() -> QRCodeManager.getEncodedBitmapFromString(encoded, length,
						length))
						.subscribeOn(Schedulers.computation())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe((bitmap, throwable) -> {
							if (bitmap != null) {
								qrImageView.setImageBitmap(bitmap);
								shareQRText.setVisibility(View.VISIBLE);
								startShareButton.setVisibility(View.VISIBLE);
							}

							if (throwable != null) {
								throwable.printStackTrace();
							}
						});

				sampleClientList();
			}

			@Override
			public void onStopped() {
				super.onStopped();
			}

			@Override
			public void onFailed(int reason) {
				super.onFailed(reason);
			}
		});
	}

	private void onJoinRoomClicked(View view) {
		connectionType = ConnectionType.WIFI_CLIENT;

		IntentIntegrator scanIntegrator = new IntentIntegrator(this);
		scanIntegrator.setPrompt("Scan");
		scanIntegrator.setBeepEnabled(true);
		scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
		scanIntegrator.setCaptureActivity(CaptureActivity.class);
		scanIntegrator.setOrientationLocked(false);
		scanIntegrator.setBarcodeImageEnabled(true);
		scanIntegrator.initiateScan();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (scanningResult != null) {
			if (scanningResult.getContents() != null) {
				Pair<String, String> ssidPassword = Utils.getWifiInfo(scanningResult.getContents());
				ShareNetworkManager.connectToWifi(getContext(), ssidPassword.getFirst(),
						ssidPassword.getSecond(), new ConnectivityManager.NetworkCallback() {
							@Override
							public void onAvailable(@NonNull Network network) {
								super.onAvailable(network);

								ConnectivityManager connectivityManager =
										(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

								connectivityManager.bindProcessToNetwork(network);

								Log.e(TAG, "Wifi available");
							}
						});

				try {
					shareServer = new ShareServer();
					shareServer.waitForClient(this::onConnected);
				} catch (IOException e) {
					e.printStackTrace();
				}

				toggleButton.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				progressBar.setIndeterminate(true);
				startShareButton.setVisibility(View.VISIBLE);
			}
		} else {
			Log.d(TAG, "Failed to parse QR Code");
		}
	}

	private void sampleClientList() {
		handlerThread = new HandlerThread("Client list sampler Thread");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
		sampleClientsUntilConnected();
	}

	private void sampleClientsUntilConnected() {
		handler.postDelayed(() -> ShareNetworkManager.getDeviceList(device -> {
			if (device != null) {
				shareClient = new ShareClient(device);
				shareClient.pingServer(this::onConnected);
			} else {
				sampleClientsUntilConnected();
			}
		}), 2000);
	}

	private void onConnected() {
		new Handler(Looper.getMainLooper()).post(() -> {
			Toast.makeText(getContext(), "Other device has connected", Toast.LENGTH_SHORT).show();
			startShareButton.setEnabled(true);
			startShareButton.setText(R.string.start_sync);
			progressBar.setVisibility(View.INVISIBLE);
		});
	}

	private Context getContext() {
		return this;
	}

	private void cleanup() {
		if (reservation != null) {
			reservation.close();
		}

		if (shareServer != null) {
			shareServer.cleanup();
		}

		if (shareClient != null) {
			shareClient.cleanup();
		}

		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}

		if (handlerThread != null) {
			handlerThread.quitSafely();
			handlerThread.interrupt();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (disposable != null && !disposable.isDisposed()) {
			disposable.dispose();
		}

		cleanup();
	}

	private enum ConnectionType {
		HOTSPOT_OWNER,
		WIFI_CLIENT
	}
}