package com.nevilleantony.prototype;

import com.nevilleantony.prototype.utils.Utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import kotlin.Pair;

public class UtilsTest {

	@Test
	public void testGetWifiInfo1() {
		String ssid = "Promised LAN";
		String passphrase = "12345678";
		String encoded = "WIFI:S:Promised LAN;T:WPA;P:12345678;;";

		Pair<String, String> decoded = Utils.getWifiInfo(encoded);
		assertEquals(decoded.getFirst(), ssid);
		assertEquals(decoded.getSecond(), passphrase);
	}

	@Test
	public void testGetWifiInfo2() {
		String ssid = "#Sil3nc3 0f th3 LAN5";
		String passphrase = "abcde1234";
		String encoded = "WIFI:S:#Sil3nc3 0f th3 LAN5;T:WPA;P:abcde1234;;";

		Pair<String, String> decoded = Utils.getWifiInfo(encoded);
		assertEquals(decoded.getFirst(), ssid);
		assertEquals(decoded.getSecond(), passphrase);
	}
}
