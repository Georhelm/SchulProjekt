package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.JsonReader;

import org.json.JSONObject;

public class Player {
    private Bitmap bitmap;

    public int getPos() {
        return pos;
    }

    private int pos;
    private String name;

    public int getX() {
        return x;
    }

    private int x;

    public int getY() {
        return y;
    }

    private int y;
    private boolean isEnemy;
    private int playerHeight;

    private int playerWidth;

    public Lance lance;
    public Mount mount;

    public Player(Context context, JSONObject player, boolean isEnemy){
        this.isEnemy = isEnemy;
        this.x = 50;
        this.y = 50;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.knight);
        try{
            this.pos = player.getInt("position");
            this.name = player.getString("username");
            this.lance = Lance.getLanceByID(player.getInt("weaponId"));
            this.mount = Mount.getMountByID(player.getInt("mountId"));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //Old
    /*public Player(Context context, int screenX, int screenY){

        x = (int)Math.round(screenX*0.1);
        y = (int)Math.round(screenY*0.4);
        speed = 1;
        exactSpeed = 1;
        liftingLance = false;

        playerHeight = (int)Math.round(screenY*0.4);
        playerWidth = (int)Math.round(playerHeight* (360.0 / 540.0)); //Verhältnis des Bildes


        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.knight);
        bitmap = Bitmap.createScaledBitmap(bitmap, playerWidth, playerHeight, false);

        lance = new Lance(this, context, screenX, screenY);
        mount = new Mount(this,context, screenX, screenY);
    }*/

    public void update(){
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
