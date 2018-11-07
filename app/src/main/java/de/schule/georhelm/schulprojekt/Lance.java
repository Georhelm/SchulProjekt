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
    private int width;
    private int height;
    private String name;
    private int rotation;
    private Matrix matrix;


    public Matrix getMatrix() {
        return matrix;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Lance(Context context, int id, int x, int y, int width, int height, String name){
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.name = name;
        rotation = 90;

        matrix = new Matrix();
        bitmap = BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(name,"drawable", context.getPackageName()));

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        matrix.reset();
        matrix.postRotate(rotation, width/2, height/2);
        matrix.postTranslate(x, y);

        if(Lance.lances==null){
            Lance.lances = new ArrayList<Lance>();
        }
        Lance.lances.add(this);
    }

    public Lance(Lance lance){
        this.name = lance.name;
        this.id = lance.id;
        this.x = lance.x;
        this.y = lance.y;
        this.width = lance.width;
        this.height = lance.height;
        this.matrix = new Matrix(lance.matrix);
        this.bitmap = lance.bitmap.copy(lance.bitmap.getConfig(),true);
    }


    public static Lance getLanceByID(int id){
        List<Lance> lances = Lance.lances;

        for(Lance lance : lances){
            if(lance.id == id){
                return new Lance(lance);
            }
        }
        return null;
    }

    public void updateMatrix(int playerX, int playerY){
        this.x += playerX;
        this.y += playerY;
        this.matrix.postTranslate(playerX, playerY);
    }

    public void setAngle(int angle) {
        this.matrix.reset();
        this.matrix.postRotate(angle, this.width/2, this.height/2);
        this.matrix.postTranslate(this.x, this.y);
    }


}
