package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.List;

public class Lance {
    private Bitmap bitmap;
    private static List<Lance> lances;
    private int x;
    private int y;
    private int id;

    //private final int lowestPositionIsHighDegree = 125;
    //private final int highestPositionIsLowDegree = 45;
    private int rotation;

    protected Matrix matrix;

   // private int speed = 0;

    public Lance(Context context, int id){
        x = 50;
        y = 50;
        rotation = 90;
        this.id = id;
        //speed = 1;
        matrix = new Matrix();

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.weaponlance);

        bitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, false);

        matrix.reset();
        matrix.postTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 1.5f); // Centers image
        matrix.postRotate(rotation);
        matrix.postTranslate(x, y);

        if(Lance.lances==null){
            Lance.lances = new ArrayList<Lance>();
        }
        Lance.lances.add(this);
    }


    public static Lance getLanceByID(int id){
        List<Lance> lances = Lance.lances;

        for(Lance lance : lances){
            if(lance.id == id){
                return lance;
            }
        }
        return null;
    }

    public void update(boolean isLifting){
        /*if(isLifting){
            rotation -= 2;
            if(rotation< highestPositionIsLowDegree){
                rotation = highestPositionIsLowDegree;
            }
            matrix.reset();
            matrix.postTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 1.5f); // Centers image
            matrix.postRotate(rotation);
            matrix.postTranslate(x, y);

        }else{
            rotation += 2;
            if(rotation> lowestPositionIsHighDegree){
                rotation = lowestPositionIsHighDegree;
            }
            matrix.reset();
            matrix.postTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 1.5f); // Centers image
            matrix.postRotate(rotation);
            matrix.postTranslate(x, y);
        }
*/
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

    //public int getSpeed() {
   //     return speed;
   //}
}
