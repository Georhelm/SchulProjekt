package de.schule.georhelm.schulprojekt.playerobjects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.List;

import de.schule.georhelm.schulprojekt.views.GameView;
import de.schule.georhelm.schulprojekt.utilities.PixelConverter;

public class Mount {

    //#region properties
    private String name;
    private Bitmap bitmap;
    private int x;
    private int y;
    private int width;
    private int height;
    private int id;
    private static List<Mount> mounts;
    private Matrix matrix;
    //#endregion properties

    //#region constructors
    /**
     * Creates a mount with given parameters. Ads it to static Mountlist if not existing yet, so every mount exists only once.
     * @param context The Context from which this method is called.
     * @param id An integer representing the id of the mount.
     * @param x An integer representing the x position of the mount in relation to player.
     * @param y An integer representing the y position of the mount in relation to player.
     * @param width An integer representing the width of the mount.
     * @param height An integer representing the height of the mount.
     * @param name A string representing the name of the mount.
     */
    public Mount(Context context, int id, int x, int y, int width, int height, String name){
        this.name = name;
        this.x = PixelConverter.convertWidth(x, context);
        this.y = PixelConverter.convertHeight(y, context);
        this.width = PixelConverter.convertWidth(width, context);
        this.height = PixelConverter.convertHeight(height, context);
        this.id = id;
        Integer bitmapId = context.getResources().getIdentifier(name,"drawable",context.getPackageName());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(context.getResources(), bitmapId, options);
        options.inSampleSize = GameView.calculateInSampleSize(options, this.width, this.height);
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeResource(context.getResources(), bitmapId, options);
        bitmap = Bitmap.createScaledBitmap(bitmap, this.width, this.height, false);
        matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate(this.x, this.y);

        if(Mount.mounts==null){
            Mount.mounts = new ArrayList<Mount>();
        }
        if (Mount.getMountByID(id) == null) {
            Mount.mounts.add(this);
        }
    }

    /**
     * Creates a copy of given Mount.
     * @param mount The mount that is to be copied.
     */
    private Mount (Mount mount){
        this.id = mount.id;
        this.x = mount.x;
        this.y = mount.y;
        this.name = mount.name;
        this.bitmap = mount.bitmap.copy(mount.bitmap.getConfig(),true);
        this.matrix = new Matrix(mount.matrix);
    }
    //#endregion constructors

    //#region getters
    public Matrix getMatrix() {
        return matrix;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }
    //#endregion getters

    //#region public methods
    /**
     * Translates the matrix of this mount to the same position of given Values.
     * @param valueX  An integer representing the x value the matrix has to be translated to.
     * @param valueY An integer representing the y value the matrix has to be translated to.
     */
    public void updateMatrix(int valueX, int valueY){
        this.matrix.postTranslate(valueX, valueY);
    }
    //#endregion public methods

    //#region public static methods

    /**
     * Returns a mount from the static Mounts list by given id.
     * @param id An integer which represents the id of the mount to be retrieved.
     * @return
     */
    public static Mount getMountByID(int id){
        List<Mount> mounts = Mount.mounts;
        for(Mount mount: mounts){
            if(mount.id == id){
                return new Mount(mount);
            }
        }
        return null;
    }
    //#endregion public static methods
}
