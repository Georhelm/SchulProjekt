package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.JsonReader;

import org.json.JSONObject;

public class Player {
    private Bitmap bitmap;
    private int pos;
    private String name;
    private int x;
    private int y;
    private boolean isEnemy;
    private Matrix matrix;

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Lance lance;
    public Mount mount;

    public Matrix getMatrix() {
        return matrix;
    }

    public Player(Context context, JSONObject player, boolean isEnemy){
        this.isEnemy = isEnemy;
        if(isEnemy){
            this.x = 1230;
        }else{
            this.x = 330;

        }
        this.y = 490;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.knight);
        bitmap = Bitmap.createScaledBitmap(bitmap, 300, 450, false);
        matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate(x, y);
        try{
            this.pos = player.getInt("position");
            this.name = player.getString("username");
            this.lance = Lance.getLanceByID(player.getInt("weaponId"));
            this.mount = Mount.getMountByID(player.getInt("mountId"));
            this.lance.updateMatrix(this.x,this.y);
            this.mount.updateMatrix(this.x,this.y);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void update(){
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
