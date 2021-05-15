package com.nevilleantony.prototype.room;

import java.util.List;

public enum MessageType {
	/*
	ROOM_MEMBER_UPDATE message will contain a list of members except the group owner separated by
	the delimiter
	*/
	ROOM_MEMBER_UPDATE('u'),

	/*
	ROOM_SYNC message will contain:
		[0] The URL itself
		[1] Unique hash of the URL
		[2] Total download size in human readable format
		[3] Range (same format as in http) assigned to current device
	separated by the delimiter
	*/
	ROOM_SYNC('s'),
	INVALID_MESSAGE('0');

	public static final String DELIMITER = "@;@";
	private final char charRepr;

	MessageType(char charRepr) {
		this.charRepr = charRepr;
	}

	public static MessageType fromValue(char charRepr) {
		switch (charRepr) {
			case 'u':
				return ROOM_MEMBER_UPDATE;
			case 's':
				return ROOM_SYNC;
			default:
				return INVALID_MESSAGE;
		}
	}

	public static String encodeList(List<? extends CharSequence> list) {
		return String.join(DELIMITER, list);
	}

	public static String[] decodeList(String message) {
		return message.split(DELIMITER);
	}

	public char getCharRepr() {
		return this.charRepr;
	}

}
