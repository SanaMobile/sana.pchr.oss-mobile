package com.smarking.mhealthsyria.app.print;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.text.BidiFormatter;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.smarking.mhealthsyria.app.model.category.Category;

/**
 *
 */
public class Form {

    public static final int WIDTH_DEFAULT = 128;
    public static final int HEIGHT_DEFAULT = 128;

    protected Category.Language lang = Category.Language.ENGLISH;
    protected boolean textRTL = false;
    BidiFormatter bidiFormatter = BidiFormatter.getInstance();
    static final char RTL = '\u200F';
    static final char LTR = '\u200E';

    public void setLanguage(String language){
        if(language.equals("ar"))
            setLanguage(Category.Language.ARABIC);
        else if(language.equals("en"))
            setLanguage(Category.Language.ENGLISH);
        else
            setLanguage(Category.Language.ENGLISH);
    }

    public void setLanguage(Category.Language language){
        this.lang = language;
        setTextDirectionality(language);
    }

    public void setTextDirectionality(Category.Language language){
        if(language == Category.Language.ENGLISH){
            bidiFormatter = BidiFormatter.getInstance(false);
        } else {
            bidiFormatter = BidiFormatter.getInstance(true);
        }
        textRTL = bidiFormatter.isRtlContext();
    }

    public boolean isRTL(){
        return textRTL;
    }

    /**
     * Sets the text of a TextView with a given id in a parent View using the
     * default text direction heuristic of the
     * {@link android.support.v4.text.BidiFormatter BidiFormatter}
     * class.
     *
     * @param textViewId Id of the child TextView
     * @param root The parent view for locating the child
     * @param string
     */
    public TextView setText(int textViewId, View root, String string){
        TextView child = (TextView) root.findViewById(textViewId);
        if(child == null) {
            throw new IllegalArgumentException("No child view with id");
        }
        return setText(child, string);
    }


    /**
     * Sets the text of a TextView using the default text direction heuristic
     * of the {@link android.support.v4.text.BidiFormatter BidiFormatter}
     * class.
     *
     * @param view Id of the child TextView
     * @param string
     */
    public TextView setText(TextView view, String string){
        view.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        view.setText(unicodeWrap(string));
        return view;
    }

    /**
     * Formats and sets the text of a TextView with a given id in a parent
     * View using the default text direction heuristic of the
     * {@link android.support.v4.text.BidiFormatter BidiFormatter}
     * class.
     *
     * @param textViewId Id of the child TextView
     * @param root The parent view for locating the child
     * @param format
     * @param strings
     */
    public TextView setText(int textViewId, View root, String format, String ... strings){
        TextView child = (TextView) root.findViewById(textViewId);
        if(child == null) {
            throw new IllegalArgumentException("No child view with id");
        }
        return setText(child, format, strings);
    }


    /**
     * Formats and sets the text of a TextView using the default text
     * direction heuristic of the {@link android.support.v4.text.BidiFormatter BidiFormatter}
     * class.
     *
     * @param view Id of the child TextView
     * @param format
     * @param strings
     */
    public TextView setText(TextView view, String format, String ... strings){
        if(strings == null)
            return view;
        if(TextUtils.isEmpty(format)) {
            throw new NullPointerException("Null format string");
        }
        String[] wrapped = wrap(strings);
        view.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        view.setText(String.format(format, wrapped));
        return view;
    }

    protected String unicodeWrap(String arg){
        return bidiFormatter.unicodeWrap(arg);
    }

    /**
     * Unicode wraps individual String arguments returns them as an array of
     * wrapped strings.
     * @param strings
     * @return
     */
    protected String[] wrap(String ... strings){
        String[] wrapped = new String[strings.length];
        for(int index = 0; index < strings.length;index++){
            wrapped[index] = bidiFormatter.unicodeWrap(strings[index]);
        }
        return wrapped;
    }

    /**
     * Unicode wraps individual String arguments and builds them into a single
     * String.
     * @param strings
     * @return
     */
    protected String build(String ... strings){
        StringBuilder builder = new StringBuilder();
        for(String string:strings){
            builder.append(bidiFormatter.unicodeWrap(string));
        }
        return builder.toString();
    }

    protected ImageView setQRImage(int id, View root, byte[] value){
        ImageView view = (ImageView)root.findViewById(id);
        try {
            view.setImageBitmap(QRCodeEncoder.encodeAsBitmap(
                    Base64.encodeToString(value, Base64.DEFAULT),
                    BarcodeFormat.QR_CODE, 512, 512));
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), " ZXING ERRROR: " + e);
            e.printStackTrace();
        }
        return view;
    }

    protected StringBuilder getStringBuilder(){

        StringBuilder builder;
        if(isRTL()){
            builder = new StringBuilder(RTL);
        } else {
            builder = new StringBuilder();
        }
        return builder;
    }

    public void loadImageToView(Context context, ImageView view,
                                int resId, int reqWidth, int reqHeight){

        Bitmap bitmap = decodeSampledBitmapFromResource(context.getResources(),
                resId, reqWidth, reqHeight);
        view.setImageBitmap(bitmap);
    }


    public void loadImageToView(Context context, ImageView view,
                                int resId) {
        loadImageToView(context, view, resId, WIDTH_DEFAULT, HEIGHT_DEFAULT);
    }
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId,
                                                         int reqWidth,
                                                         int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
