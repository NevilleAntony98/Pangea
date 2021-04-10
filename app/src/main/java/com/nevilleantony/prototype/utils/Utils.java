package com.nevilleantony.prototype.utils;

import android.annotation.SuppressLint;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

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
}
