package jadkowski.dawid.pollutionapp.helpers;

import android.graphics.Color;

/**
 * Created by dawid on 06/01/2017.
 */

public class Constants {
    public static final String HOST = "YOUR WEBHOST LINK HERE";
    public static final String[] MONTHS_NAME = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    public static final String[] BOROUGHS_NAME = new String[]{
            "Camden", "Hackney", "Haringey", "Islington", "Westminster"};
    public static final int[] DARK_COLOURS = {
            Color.rgb(147, 179, 177), Color.rgb(70, 126, 119), Color.rgb(21, 82, 81)
    };
    public static final int[] GREEN_COLOURS = {
            Color.rgb(162, 179, 147), Color.rgb(96, 126, 70), Color.rgb(50, 82, 21)
    };
    public static final int[] YELLOW_COLOURS = {
            Color.rgb(179, 178, 147), Color.rgb(125, 126, 70), Color.rgb(82, 75, 21)
    };
    public static final int[] RED_COLOURS = {
            Color.rgb(179, 152, 147), Color.rgb(126, 81, 70), Color.rgb(82, 34, 21)
    };
    public static final int GREEN_CLOUR = Color.rgb(124, 175, 78);
    public static final int YELLOW_COLOUR = Color.rgb(211, 197, 41);
    public static final int RED_COLOUR = Color.rgb(153, 68, 46);
    public static final int GRAY_COLOUR = Color.rgb(199, 199, 199);
    public static final int[] GRADIENT_COLOURS = {
            Color.rgb(102, 225, 0), // green
            Color.rgb(255, 0, 0)    // red
    };
    public static final float[] GRADIENT_STARTPOINTS = {
            0.2f, 1f
    };
}
