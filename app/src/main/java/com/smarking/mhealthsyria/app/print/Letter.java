package com.smarking.mhealthsyria.app.print;

/**
 * Constants which denote page height and width for Letter size page
 * layouts. Assumes 300 dpi resolution for values of constant {@link #HEIGHT}
 * and {@link #WIDTH}
 */
public final class Letter {

    public static final int HEIGHT = 3300;

    public static final int WIDTH = 2550;

    public static int getHeight(int dpi){
        return Math.round(HEIGHT*(dpi/300));
    }

    public static int getWidth(int dpi){
        return Math.round(WIDTH*(dpi/300));
    }

    private Letter(){}
}
