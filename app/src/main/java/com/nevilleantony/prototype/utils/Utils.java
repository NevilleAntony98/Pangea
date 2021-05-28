package com.nevilleantony.prototype.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.UUID;

public class Utils {
	@SuppressLint("DefaultLocale")
	public static String getHumanReadableSize(long bytes) {
		long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
		if (absB < 1024) {
			return bytes + " B";
		}
		long value = absB;
		CharacterIterator characterIterator = new StringCharacterIterator("KMGTPE");
		for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
			value >>= 10;
			characterIterator.next();
		}
		value *= Long.signum(bytes);
		return String.format("%.1f %ciB", value / 1024.0, characterIterator.current());
	}

	public static String getDigest(String data) {
		return UUID.nameUUIDFromBytes(data.getBytes()).toString();
	}

	public static boolean isLocationEnabled(Context context) {
		final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public static void tryRequestLocation(Context context) {
		if (!isLocationEnabled(context)) {
			new AlertDialog.Builder(context)
					.setMessage("Please enable location to continue.")
					.setCancelable(false)
					.setPositiveButton("Yes",
							(dialog, which) -> {
								context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
								dialog.cancel();
							})
					.setNegativeButton("No", (dialog, which) -> dialog.cancel())
					.create()
					.show();
		}
	}
}
