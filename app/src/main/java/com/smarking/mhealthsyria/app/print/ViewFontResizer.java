package com.smarking.mhealthsyria.app.print;

import android.view.View;
import android.widget.TextView;

import java.util.List;

/**
 * Utility to adjust the font size of a list of TextViews to fit onto a
 * specified height
 *
 */
public final class ViewFontResizer {

    public static final int MIN_HEIGHT = 36;

    /**
     * Reduces the font size of a list of text views down to a minimum size
     *
     * @param root the root View to measure the total height of
     * @param desiredHeight the desired height of the root view
     * @param minHeight the minimum height of the text font measured in sp
     * @param adjustViews the list of text views to adjust
     */
    public static void shrinkFont(View root, int desiredHeight, int minHeight, List<TextView> adjustViews){
        int size = root.getMeasuredHeight();
        float textSize = adjustViews.get(0).getTextSize();
        while(size > desiredHeight && textSize > minHeight){
            textSize = textSize*0.95f;
            for(TextView view: adjustViews){
                view.setTextSize(textSize);
            }
            size = root.getMeasuredHeight();
        }
    }

    /**
     * Reduces the font size of a list of text views down to the value
     * of {@link #MIN_HEIGHT}
     *
     * @param root the root View to measure the total height of
     * @param desiredHeight the desired height of the root view
     * @param adjustViews the list of text views to adjust
     */
    public static void shrinkFont(View root, int desiredHeight, List<TextView> adjustViews){
        shrinkFont(root,desiredHeight, MIN_HEIGHT, adjustViews);
    }
}
