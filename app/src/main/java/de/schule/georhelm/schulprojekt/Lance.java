package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class Lance {
    private Bitmap bitmap;

    private int x;
    private int y;


    private final int lowestPositionIsHighDegree = 125;
    private final int highestPositionIsLowDegree = 45;
    private int rotation;

    protected Matrix matrix;

    private int speed = 0;

    public Lance(Context context, int screenX, int screenY){
        x = 120;
        y = 300;

        rotation = 90;

        speed = 1;
        matrix = new Matrix();

        matrix.postTranslate(100,300);

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.weaponlance);

        bitmap = Bitmap.createScaledBitmap(bitmap, 700, 700, false);

        matrix.setRotate(rotation,bitmap.getWidth()/2,bitmap.getHeight()/2);

    }

    public void update(boolean isLifting){
        if(isLifting){
            rotation -= 2;
            if(rotation< highestPositionIsLowDegree){
                rotation = highestPositionIsLowDegree;
            }
            matrix.setRotate(rotation,bitmap.getWidth()/2,bitmap.getHeight()/2+150);

        }else{
            rotation += 2;
            if(rotation> lowestPositionIsHighDegree){
                rotation = lowestPositionIsHighDegree;
            }
            matrix.setRotate(rotation,bitmap.getWidth()/2,bitmap.getHeight()/2+150);
        }

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

    public int getSpeed() {
        return speed;
    }
}
