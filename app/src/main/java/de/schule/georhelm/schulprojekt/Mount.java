package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class Mount {
    private Bitmap bitmap;
    private int x;
    private int y;
    int mountHeight;
    int mountWidth;

    protected Matrix matrix;
    public Mount(Player player, Context context, int screenX, int screenY){
        x = (int)Math.round(screenX*0.12);
        y = (int)Math.round(screenY*0.7);
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pterodactyl);

        mountWidth = (int)Math.round(player.getPlayerWidth() * 2.5);//Invertiert, weil die Rakete noch um 90 grad gedreht wird!!!
        mountHeight= (int)Math.round(mountWidth * ((double)bitmap.getHeight() / (double)bitmap.getWidth())); //Maße des Bildes

        bitmap = Bitmap.createScaledBitmap(bitmap, mountWidth, mountHeight, false);

        matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 1.5f); // Centers image
        //matrix.postRotate(90);
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
