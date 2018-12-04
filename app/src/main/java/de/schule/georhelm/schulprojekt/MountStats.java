package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

public class MountStats {
    public int getId() {
        return id;
    }

    private int id;
    private String name;

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

    private int maxSpeed;
    private int acceleration;
    private int height;

    public Bitmap getBitmap() {
        return bitmap;
    }

    private Bitmap bitmap;

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

    public String toString(){
        return this.name;
    }

    public void recycle(){
        this.bitmap.recycle();
    }
}
