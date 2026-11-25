package com.clubsync.util;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public final class QrUtils {
    // Fond blanc et modules #0B1020 comme spécifié
    private static final int COLOR_MODULE = 0xFF0B1020;
    private static final int COLOR_BACKGROUND = 0xFFFFFFFF;

    private QrUtils() {}

    public static Bitmap generateQrBitmap(String content, int size) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? COLOR_MODULE : COLOR_BACKGROUND);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
