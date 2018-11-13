package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.List;

public class Mount {
    private String name;
    private Bitmap bitmap;
    private int x;
    private int y;
    private int id;
    private static List<Mount> mounts;
    private Matrix matrix;

    public Matrix getMatrix() {
        return matrix;
    }

    public Mount(Context context, int id, int x, int y, int width, int height, String name){
        this.name = name;
        this.x = x;
        this.y = y;
        this.id = id;
        Integer bitmapId = context.getResources().getIdentifier(name,"drawable",context.getPackageName());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(context.getResources(), bitmapId, options);
        options.inSampleSize = GameView.calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeResource(context.getResources(), bitmapId, options);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate(x, y);

        if(Mount.mounts==null){
            Mount.mounts = new ArrayList<Mount>();
        }
        if (Mount.getMountByID(id) == null) {
            Mount.mounts.add(this);
        }
    }
    public Mount (Mount mount){
        this.id = mount.id;
        this.x = mount.x;
        this.y = mount.y;
        this.name = mount.name;
        this.bitmap = mount.bitmap.copy(mount.bitmap.getConfig(),true);
        this.matrix = new Matrix(mount.matrix);
    }

    public static Mount getMountByID(int id){
        List<Mount> mounts = Mount.mounts;

        for(Mount mount: mounts){
            if(mount.id == id){
                return new Mount(mount);
            }
        }
        return null;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void updateMatrix(int playerX, int playerY){
        this.matrix.postTranslate(playerX, playerY);
    }
}
