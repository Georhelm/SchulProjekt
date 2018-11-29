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
    private int lancetipYPos;


    public Matrix getMatrix() {
        return matrix;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Lance(Context context, int id, int x, int y, int width, int height, String name){
        this.id = id;
        this.x = PixelConverter.convertWidth(x, context);
        this.y = PixelConverter.convertHeight(y, context);
        this.width = PixelConverter.convertWidth(width, context);
        this.height = PixelConverter.convertHeight(height, context);
        this.name = name;
        rotation = 90;

        matrix = new Matrix();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        int bitmapId = context.getResources().getIdentifier(name,"drawable", context.getPackageName());

        BitmapFactory.decodeResource(context.getResources(), bitmapId, options);
        options.inSampleSize = GameView.calculateInSampleSize(options, this.width, this.height);
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeResource(context.getResources(), bitmapId, options);

        bitmap = Bitmap.createScaledBitmap(bitmap, this.width, this.height, false);

        matrix.reset();
        matrix.postRotate(rotation, this.width/2, this.height/1.5f);
        matrix.postTranslate(this.x, this.y);

        if(Lance.lances==null){
            Lance.lances = new ArrayList<Lance>();
        }

        if(Lance.getLanceByID(id) == null) {
            Lance.lances.add(this);
        }
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
        this.matrix.postRotate(angle, this.width/2, this.height/1.5f);
        this.matrix.postTranslate(this.x, this.y);
    }

    public int getLancetipYPos(){
        return this.lancetipYPos;
    }

    public void setLancetipYPos(int lancetipYPos){
        this. lancetipYPos = lancetipYPos;
    }


}
