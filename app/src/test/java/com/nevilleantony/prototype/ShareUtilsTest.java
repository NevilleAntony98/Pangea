package com.nevilleantony.prototype;

import com.nevilleantony.prototype.share.ShareUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kotlin.Pair;

import static org.junit.Assert.assertEquals;

public class ShareUtilsTest {
	private List<Long> getLongs(int... Longs) {
		return Arrays.stream(Longs).asLongStream().boxed().collect(Collectors.toList());
	}

	private Pair<String, Map<String, List<Long>>> getTestCase1() {
		Map<String, List<Long>> hashPartsMap = new HashMap<>();
		hashPartsMap.put("deadbeef", getLongs(1, 2, 4));
		hashPartsMap.put("badf00d", getLongs(5));

		return new kotlin.Pair<>("deadbeef#;#1#;#2#;#4@;@badf00d#;#5", hashPartsMap);
	}

	private Pair<String, Map<String, List<Long>>> getTestCase2() {
		Map<String, List<Long>> hashPartsMap = new HashMap<>();
		hashPartsMap.put("deadbeef012x", getLongs(1));

		return new kotlin.Pair<>("deadbeef012x#;#1", hashPartsMap);
	}

	@Test
	public void encodePartsMessageTest1() {
		assertEquals(ShareUtils.encodePartsMessage(getTestCase1().getSecond()), getTestCase1().getFirst());
	}

	@Test
	public void decodePartsMessageTest1() {
		assertEquals(ShareUtils.decodePartsMessage(getTestCase1().getFirst()), getTestCase1().getSecond());
	}

	@Test
	public void encodePartsMessageTest2() {
		assertEquals(ShareUtils.encodePartsMessage(getTestCase2().getSecond()), getTestCase2().getFirst());
	}

	@Test
	public void decodePartsMessageTest2() {
		assertEquals(ShareUtils.decodePartsMessage(getTestCase2().getFirst()), getTestCase2().getSecond());
	}

	@Test
	public void getRequiredPartsTest1() {
		Map<String, List<Long>> hashPartsMap1 = new HashMap<>();
		hashPartsMap1.put("deadbeef", getLongs(1, 2, 4));
		hashPartsMap1.put("badf00d", getLongs(1, 5));

		Map<String, List<Long>> hashPartsMap2 = new HashMap<>();
		hashPartsMap2.put("deadbeef", getLongs(3, 4));
		hashPartsMap2.put("badf00d", getLongs(5, 6));

		Map<String, List<Long>> expected = new HashMap<>();
		expected.put("deadbeef", getLongs(3));
		expected.put("badf00d", getLongs(6));

		assertEquals(ShareUtils.getRequiredParts(hashPartsMap1, hashPartsMap2), expected);
	}

	@Test
	public void getRequiredPartsTest2() {
		Map<String, List<Long>> hashPartsMap1 = new HashMap<>();
		hashPartsMap1.put("deadbeef", getLongs(1, 2, 4));
		hashPartsMap1.put("badf00d", getLongs(1, 5));

		Map<String, List<Long>> hashPartsMap2 = new HashMap<>();
		hashPartsMap2.put("deadbeef", getLongs(3, 4));
		hashPartsMap2.put("badf00d", getLongs(5));

		Map<String, List<Long>> expected = new HashMap<>();
		expected.put("deadbeef", getLongs(3));

		assertEquals(ShareUtils.getRequiredParts(hashPartsMap1, hashPartsMap2), expected);
	}

	@Test
	public void getRequiredPartsTest3() {
		Map<String, List<Long>> hashPartsMap1 = new HashMap<>();
		hashPartsMap1.put("deadbeef", getLongs(3, 4));
		hashPartsMap1.put("badf00d", getLongs(1));

		Map<String, List<Long>> hashPartsMap2 = new HashMap<>();
		hashPartsMap2.put("deadbeef", getLongs(3));
		hashPartsMap2.put("badf00d", getLongs(5, 6, 7));
		hashPartsMap2.put("d3eadb33f", getLongs(5, 6, 7));

		Map<String, List<Long>> expected = new HashMap<>();
		expected.put("badf00d", getLongs(5, 6, 7));

		assertEquals(ShareUtils.getRequiredParts(hashPartsMap1, hashPartsMap2), expected);
	}

	@Test
	public void getRequiredPartsTest4() {
		Map<String, List<Long>> hashPartsMap1 = new HashMap<>();
		hashPartsMap1.put("deadbeef", getLongs(3, 4));
		hashPartsMap1.put("d3adb33f", getLongs(1));

		Map<String, List<Long>> hashPartsMap2 = new HashMap<>();
		hashPartsMap2.put("badf00d", getLongs(5, 6, 7));

		Map<String, List<Long>> expected = new HashMap<>();

		assertEquals(ShareUtils.getRequiredParts(hashPartsMap1, hashPartsMap2), expected);
	}
}
