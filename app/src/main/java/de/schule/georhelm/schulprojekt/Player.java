package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import org.json.JSONObject;

public class Player {
    private Bitmap bitmap;
    private int pos;
    private String name;
    private int x;
    private int y;
    private boolean isEnemy;
    private Matrix matrix;
    private int height;
    private int width;
    private int handHeight;
    private int hitpoints;

    public int getLastHit() {
        return lastHit;
    }

    public void setLastHit(int lastHit) {
        this.lastHit = lastHit;
    }

    private int lastHit;
    public int mountHeight;

    public int getX() {
        return this.x;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Lance getLance() {
        return lance;
    }

    public void setHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    public void setLance(Lance lance) {
        this.lance = lance;
    }

    public Mount getMount() {
        return mount;
    }

    public void setMount(Mount mount) {
        this.mount = mount;
    }

    private Lance lance;
    private Mount mount;

    public Matrix getMatrix() {
        return matrix;
    }

    public Player(Context context, JSONObject player, boolean isEnemy){
        this.isEnemy = isEnemy;

        this.handHeight = PixelConverter.convertHeight(165, context); // When we add different characters this needs to come from an xml

        this.height = PixelConverter.convertHeight(450, context);
        this.width = PixelConverter.convertWidth(300, context);

        if(isEnemy){
            this.x = PixelConverter.convertWidth(1150, context);
        }else{
            this.x = PixelConverter.convertWidth(200, context);
        }


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(context.getResources(), R.drawable.knight, options);
        options.inSampleSize = GameView.calculateInSampleSize(options, this.width, this.height);
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.knight, options);
        bitmap = Bitmap.createScaledBitmap(bitmap, this.width, this.height, false);
        try{
            this.pos = player.getInt("position");
            this.name = player.getString("username");
            this.lance = Lance.getLanceByID(player.getInt("weaponId"));
            this.mount = Mount.getMountByID(player.getInt("mountId"));
            this.hitpoints = player.getInt("hitpoints");
            this.mountHeight = PixelConverter.convertY(player.getInt("mountHeight"),1,context);
            this.y = PixelConverter.convertY(player.getInt("mountHeight"),this.height, context) + this.handHeight;
            this.matrix = new Matrix();
            this.matrix.reset();
            this.matrix.postTranslate(this.x, this.y);
            this.lance.updateMatrix(this.x,this.y);
            this.mount.updateMatrix(this.x,this.y);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void lanceUp() {
        ConnectionSocket.getSocket().playerInput(true);
    }

    public void lanceDown() {
        ConnectionSocket.getSocket().playerInput(false);
    }

    public void setLanceAngle(int angle) {
        this.lance.setAngle(angle);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
