package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Player {
    private Bitmap bitmap;

    private int x;
    private int y;

    private int speed;

    private boolean liftingLance;

    private final int GRAVITY = -10;

    public Lance lance;

    public Player(Context context, int screenX, int screenY){
        x = 75;
        y = 400;
        speed = 1;
        liftingLance = false;


        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.knight);
        bitmap = Bitmap.createScaledBitmap(bitmap, 360, 540, false);

        lance = new Lance(context, screenX, screenY);
    }

    public void update(){
        //x++;

        lance.update(liftingLance);
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

    public void liftLance(){
        liftingLance = true;
    }

    public void stopLiftingLance() {
        liftingLance = false;
    }
}
