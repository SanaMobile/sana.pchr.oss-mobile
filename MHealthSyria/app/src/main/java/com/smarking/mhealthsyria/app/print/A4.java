package com.smarking.mhealthsyria.app.print;

/**
 *
 * Constants which denote page height and width for A4 page
 * layouts. Assumes 300 dpi resolution for values of constant {@link #HEIGHT}
 * and {@link #WIDTH}
 */
public final class A4 {

    public static final int HEIGHT = 3508;

    public static final int WIDTH = 2480;


    public static int getHeight(int dpi){
        return Math.round(HEIGHT*(dpi/300));
    }

    public static int getWidth(int dpi){
        return Math.round(WIDTH*(dpi/300));
    }

    private A4(){}
}
