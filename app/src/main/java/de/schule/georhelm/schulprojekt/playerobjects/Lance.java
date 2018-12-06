package de.schule.georhelm.schulprojekt.playerobjects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.List;

import de.schule.georhelm.schulprojekt.views.GameView;
import de.schule.georhelm.schulprojekt.utilities.PixelConverter;

public class Lance {

    //#region properties
    private Bitmap bitmap;
    private static List<Lance> lances;
    private int x;
    private int y;
    private int id;
    private int width;
    private int height;
    private String name;
    private Matrix matrix;
    private int tipYPos;
    //#endregion properties
    //#region getters
    public int getTipYPos() {
        return tipYPos;
    }
    public Matrix getMatrix() {
        return matrix;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }
    //#endregion getters
    //#region setters
    public void setTipYPos(int tipYPos) {
        this.tipYPos = tipYPos;
    }
    //#endregion setters
    //#region constructors
    public Lance(Context context, int id, int x, int y, int width, int height, String name){
        this.id = id;
        this.x = PixelConverter.convertWidth(x, context);
        this.y = PixelConverter.convertHeight(y, context);
        this.width = PixelConverter.convertWidth(width, context);
        this.height = PixelConverter.convertHeight(height, context);
        this.name = name;

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
        matrix.postRotate(90, this.width/2, this.height/1.5f);
        matrix.postTranslate(this.x, this.y);

        if(Lance.lances==null){
            Lance.lances = new ArrayList<Lance>();
        }

        if(Lance.getLanceByID(id) == null) {
            Lance.lances.add(this);
        }
    }
    private Lance(Lance lance){
        this.name = lance.name;
        this.id = lance.id;
        this.x = lance.x;
        this.y = lance.y;
        this.width = lance.width;
        this.height = lance.height;
        this.matrix = new Matrix(lance.matrix);
        this.bitmap = lance.bitmap.copy(lance.bitmap.getConfig(),true);
    }
    //#endregion constructors
    //#region public static methods
    public static Lance getLanceByID(int id){
        List<Lance> lances = Lance.lances;

        for(Lance lance : lances){
            if(lance.id == id){
                return new Lance(lance);
            }
        }
        return null;
    }
    //#endregion public static methods
    //#region public methods
    /**
     * Translates the x and y coordinates of the lance according to given values.
     * @param valueX An integer representing the x value for transformation.
     * @param valueY An integer representing the y value for transformation.
     */
    public void updateMatrix(int valueX, int valueY){
        this.x += valueX;
        this.y += valueY;
        this.matrix.postTranslate(valueX, valueY);
    }

    /**
     * Sets the angle of the Lance.
     * @param angle An integer representing the degrees of the angle.
     */
    public void setAngle(int angle) {
        this.matrix.reset();
        this.matrix.postRotate(angle, this.width/2, this.height/1.5f);
        this.matrix.postTranslate(this.x, this.y);
    }
    //#endregion public methods
}
