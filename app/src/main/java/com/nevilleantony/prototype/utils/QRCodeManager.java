package com.nevilleantony.prototype.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeManager {
	private QRCodeManager() {
	}

	public static Bitmap getEncodedBitmapFromString(String str, int width, int height) throws WriterException {
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		BitMatrix bitMatrix = multiFormatWriter.encode(str, BarcodeFormat.QR_CODE, width, height);
		BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

		return barcodeEncoder.createBitmap(bitMatrix);
	}
}
