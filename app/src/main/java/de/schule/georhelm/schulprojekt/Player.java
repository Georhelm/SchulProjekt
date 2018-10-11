package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Player {
    private Bitmap bitmap;

    private int x;
    private int y;

    private int playerHeight;

    private int playerWidth;

    public int getPlayerHeight() {
        return playerHeight;
    }

    public int getPlayerWidth() {
        return playerWidth;
    }

    private int speed;

    private boolean liftingLance;

    private final int GRAVITY = -10;

    public Lance lance;
    public Mount mount;

    public Player(Context context, int screenX, int screenY){
        x = (int)Math.round(screenX*0.1);
        y = (int)Math.round(screenY*0.4);
        speed = 1;
        liftingLance = false;

        playerHeight = (int)Math.round(screenY*0.4);
        playerWidth = (int)Math.round(playerHeight* (360.0 / 540.0)); //Verhältnis des Bildes


        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.knight);
        bitmap = Bitmap.createScaledBitmap(bitmap, playerWidth, playerHeight, false);

        lance = new Lance(this, context, screenX, screenY);
        mount = new Mount(this,context, screenX, screenY);
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
