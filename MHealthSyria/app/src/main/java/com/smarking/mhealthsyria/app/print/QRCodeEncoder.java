package com.smarking.mhealthsyria.app.print;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * This is an adaptation of the zxing client QRCodeEncoder class
 *
 * Created by winkler.em@gmail.com, on 12/22/2015.
 */
public class QRCodeEncoder {


    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    public static Bitmap encodeAsBitmap(String contents,
                                        BarcodeFormat format,
                                        int desiredWidth,
                                        int desiredHeight,
                                        ErrorCorrectionLevel errorCorrectionLevel) throws WriterException {
        Hashtable<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new Hashtable<EncodeHintType, Object>(2);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
            hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        }
        BitMatrix result = new MultiFormatWriter().encode(contents, format, desiredWidth, desiredHeight, hints);
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    public static Bitmap encodeAsBitmap(String contents,
                                        BarcodeFormat format,
                                        int desiredWidth,
                                        int desiredHeight) throws WriterException {
        return encodeAsBitmap(contents,format,desiredWidth,desiredHeight,ErrorCorrectionLevel.H);
    }
}
