package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

public class MountStats {
    //#region properties
    private int id;
    private String name;
    private int maxSpeed;
    private int acceleration;
    private int height;
    private Bitmap bitmap;
    //#endregion properties

    //#region constructor
    /**
     * Creates MountStats from given JSONObject to contain properties like id, name and acceleration.
     * A bitmap is loaded using the name of the mount.
     * @param json The JSONObject containing id, name, maxSpeed, acceleration and height of the mount.
     * @param context The context from which this method is called.
     */
    public MountStats(JSONObject json, Context context){
        try {
            this.id = json.getInt("id");
            this.name = json.getString("name");
            this.maxSpeed = json.getInt("maxSpeed");
            this.acceleration = json.getInt("acceleration");
            this.height = json.getInt("height");

            Integer bitmapId = context.getResources().getIdentifier(name,"drawable",context.getPackageName());

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeResource(context.getResources(), bitmapId, options);
            options.inSampleSize = GameView.calculateInSampleSize(options, context.getResources().getDimensionPixelSize(R.dimen.equipmentWidth), context.getResources().getDimensionPixelSize(R.dimen.equipmentHeight));
            options.inJustDecodeBounds = false;

            this.bitmap = BitmapFactory.decodeResource(context.getResources(), bitmapId, options);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //#endregion constructor

    //#region getters
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getMaxSpeed() {
        return Integer.toString(maxSpeed);
    }
    public String getAcceleration() {
        return Integer.toString(acceleration);
    }
    public String getHeight() {
        return Integer.toString(height);
    }
    public Bitmap getBitmap() {
        return bitmap;
    }
    //#endregion getters

    //#region public methods
    /**
     * Method to return the name of called mountStat.
     * @return Returns string containing the name property of this MountStat
     */
    @Override
    public String toString(){
        return this.name;
    }
    /**
     * Recycles the bitmap of this MountStat.
     */
    public void recycle(){
        this.bitmap.recycle();
    }
    //#endregion public methods
}



