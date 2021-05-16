package com.nevilleantony.prototype.utils;

import androidx.annotation.NonNull;

public class Range {
	public final long min;
	public final long max;

	public Range(long min, long max) throws IllegalArgumentException {
		if (min > max) {
			throw new IllegalArgumentException("Max range is lower than min");
		}

		this.min = min;
		this.max = max;
	}

	@NonNull
	@Override
	public String toString() {
		String minString = Long.toString(min);
		String maxString = Long.toString(max);

		return String.format("%s-%s", minString, maxString);
	}
}
