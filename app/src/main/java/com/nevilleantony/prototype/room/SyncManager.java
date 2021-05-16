package com.nevilleantony.prototype.room;

import com.nevilleantony.prototype.utils.Range;

import java.util.ArrayList;
import java.util.List;

public class SyncManager {
	public static List<Range> getRanges(long size, long number) throws IllegalArgumentException {
		if (number == 0) {
			throw new IllegalArgumentException("0 is not a valid number of parts");
		}

		if (size < 0) {
			throw new IllegalArgumentException("Size cannot be negative");
		}

		List<Range> ranges = new ArrayList<>();

		long remainder = size % number;
		long base = (size - remainder) / number;

		long current = 0;
		for (int i = 0; i < number; i++) {
			long currentMax = current + (base - 1) + Math.max(Math.min(1, remainder--), 0);
			Range range = new Range(current, currentMax);
			ranges.add(range);

			current = currentMax + 1;
		}

		return ranges;
	}
}
