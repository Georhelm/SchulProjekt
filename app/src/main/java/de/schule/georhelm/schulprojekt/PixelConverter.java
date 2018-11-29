package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class PixelConverter {

    public static int convertHeight(int x, Context context) {
        Point size = PixelConverter.getScreenSize(context);
        float percent = x / 1080f;
        return Math.round(percent * size.y);
    }

    public static int convertWidth(int x, Context context) {
        Point size = PixelConverter.getScreenSize(context);
        float percent = x / 1920f;
        return Math.round(percent * size.x);
    }

    private static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int convertY(int y, int height, Context context) {
        Point size = PixelConverter.getScreenSize(context);
        int screenHeight = size.y;
        return screenHeight - PixelConverter.convertHeight(y, context) - height;
    }

/*    public static int convertX(int x, int width, Context context) {
        Point size = PixelConverter.getScreenSize(context);
        int screenWidth = size.x;
        return screenWidth - PixelConverter.convertWidth(x, context) - width;
    }*/
}
