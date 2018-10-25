package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.List;

public class Mount {
    private Bitmap bitmap;
    private int x;
    private int y;
   // private int mountHeight;
    //private int mountWidth;
    private int id;
    private static List<Mount> mounts;
    //public double getAcceleration() {
       // return acceleration;
    //}

    //private double acceleration;

    protected Matrix matrix;
    public Mount(Context context, int id){
        x = 50;
        y = 50;
        this.id = id;
        //acceleration = 0.02;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.aptosaurus);

        //mountWidth = (int)Math.round(player.getPlayerWidth() * 2);//Invertiert, weil die Rakete noch um 90 grad gedreht wird!!!
        //mountHeight= (int)Math.round(mountWidth * ((double)bitmap.getHeight() / (double)bitmap.getWidth())); //Maße des Bildes

        //bitmap = Bitmap.createScaledBitmap(bitmap, mountWidth, mountHeight, false);

        matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 1.5f); // Centers image
        //matrix.postRotate(90);
        matrix.postTranslate(x, y);

        if(Mount.mounts==null){
            Mount.mounts = new ArrayList<Mount>();
        }
        Mount.mounts.add(this);
    }

    public static Mount getMountByID(int id){
        List<Mount> mounts = Mount.mounts;

        for(Mount mount: mounts){
            if(mount.id == id){
                return mount;
            }
        }
        return null;
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
