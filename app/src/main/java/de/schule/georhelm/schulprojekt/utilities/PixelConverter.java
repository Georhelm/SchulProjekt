package de.schule.georhelm.schulprojekt.utilities;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class PixelConverter {

    //#region public static methods
    /**
     * Converts a given value which fits a landscape-height of 1080p into a value fitting the actual screensize.
     * @param value An integer representing the value of landscape-height in 1080p that has to be converted to the actual screensize.
     * @param context The context from which the screensize will be determined.
     * @return Integer which contains the converted value.
     */
    public static int convertHeight(int value, Context context) {
        Point size = PixelConverter.getScreenSize(context);
        float percent = value / 1080f;
        return Math.round(percent * size.y);
    }
    /**
     * Converts a given value which fits a landscape-width of 1980p into a value fitting the actual screensize.
     * @param value An integer representing the value of landscape-width in 1980p that has to be converted to the actual screensize.
     * @param context The context from which the screensize will be determined.
     * @return Integer which contains the converted value.
     */
    public static int convertWidth(int value, Context context) {
        Point size = PixelConverter.getScreenSize(context);
        float percent = value / 1920f;
        return Math.round(percent * size.x);
    }
    /**
     * Converts given Integer representing an y value so that it gets subtracted from the screenheight.
     * This is used to counter the fact that y position of screen is measured from top to bottom (0,0 is top left corner)
     * @param y An Integer representing the value to be converted.
     * @param height The height of the object of interest. Eg.: For a bitmap, the height of it must be aditionally substracted.
     * @param context The context from which this method is called.
     * @return An integer representing the y value counting from the bottom.
     */
    public static int convertY(int y, int height, Context context) {
        Point size = PixelConverter.getScreenSize(context);
        int screenHeight = size.y;
        return screenHeight - PixelConverter.convertHeight(y, context) - height;
    }
    //#endregion public static methods

    //#region private static methods
    /**
     * Gets the screensize of the given context and returns it as a point.
     * @param context The context from which the screensize needs to be determined.
     * @return Returns a point which contains the screensize as x and y values.
     */
    private static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }
    //#endregion private static methods
}
