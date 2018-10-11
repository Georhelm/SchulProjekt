package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class Mount {
    private Bitmap bitmap;
    private int x;
    private int y;
    protected Matrix matrix;
    public Mount(int playerLocationX, int playerLocationY, Context context, int screenX, int screenY){
        x = playerLocationX + 165;
        y = playerLocationY + 350;

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rocketwithfire);

        bitmap = Bitmap.createScaledBitmap(bitmap, 110, 246, false);

        matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 1.5f); // Centers image
        matrix.postRotate(90);
        matrix.postTranslate(x, y);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
