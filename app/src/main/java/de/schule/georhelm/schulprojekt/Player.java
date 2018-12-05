package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import org.json.JSONObject;

public class Player {

    //#region properties
    private String name;
    private Lance lance;
    private Mount mount;
    private Bitmap bitmap;
    private Matrix matrix;
    private int startPos;
    private int pos;
    private int x;
    private int y;
    private int hitpoints;
    private int nextHitpoints;
    private int height;
    private int width;
    private int handHeight;
    private int lastHit;
    private int mountHeight;
    private boolean isEnemy;
    //#endregion properties

    //#region constructor
    /**
     * Player class from which the player and the enemy is created. It contains all the important information and objects belonging to the player,
     * like mount, lance and positions.
     * @param context The context in which the object is created.
     * @param player The JSONObject player from the server, which contains all the information regarding that specific player.
     * @param isEnemy A boolean to determine whether the object created is the enemy or not.
     */
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
            this.startPos = this.pos;
            this.name = player.getString("username");
            this.lance = Lance.getLanceByID(player.getInt("weaponId"));
            this.mount = Mount.getMountByID(player.getInt("mountId"));
            this.hitpoints = player.getInt("hitpoints");
            this.mountHeight = PixelConverter.convertY(player.getInt("mountHeight"),this.height, context);
            this.y = mountHeight + this.handHeight;
            this.matrix = new Matrix();
            this.matrix.reset();
            this.matrix.postTranslate(this.x, this.y);
            this.lance.updateMatrix(this.x,this.y);
            this.mount.updateMatrix(this.x,this.y);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    //#endregion constructor

    //#region getters
    public int getMountHeight() {
        return mountHeight;
    }
    public int getWidth() {
        return width;
    }
    public int getHitpoints() {
        return hitpoints;
    }
    public int getLastHit() {
        return lastHit;
    }
    public int getX() {
        return this.x;
    }
    public int getPos() {
        return pos;
    }
    public Lance getLance() {
        return lance;
    }
    public Mount getMount() {
        return mount;
    }
    public Matrix getMatrix() {
        return matrix;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }
    //#endregion getters

    //#region setters
    public void setLastHit(int lastHit) {
        this.lastHit = lastHit;
    }
    public void setPos(int pos) {
        this.pos = pos;
    }
    public void setMount(Mount mount) {
        this.mount = mount;
    }
    public void setNextHitpoints(int nextHitpoints){
        this.nextHitpoints = nextHitpoints;
    }
    //#endregion setter

    //#region public methods
    /**
     * resets the pos of this object back to startPos
     */
    public void resetPos(){
        this.pos = this.startPos;
    }

    /**
     * Sends to the connectionSocket that the player is giving an input.
     */
    public void lanceUp() {
        ConnectionSocket.getSocket().playerInput(true);
    }

    /**
     * Sends to the connectionSocket that the player is giving no input.
     */
    public void lanceDown() {
        ConnectionSocket.getSocket().playerInput(false);
    }

    /**
     * Sets the angle of the Lance to given angle.
     * @param angle
     */
    public void setLanceAngle(int angle) {
        this.lance.setAngle(angle);
    }

    /**
     * Updates hitpoints to the amount of nextHitpoints.
     */
    public void updateHitpoints(){
        this.hitpoints = nextHitpoints;
    }
    //#endregion public methods
}
