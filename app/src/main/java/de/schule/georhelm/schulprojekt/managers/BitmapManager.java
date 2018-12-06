package de.schule.georhelm.schulprojekt.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import de.schule.georhelm.schulprojekt.views.GameView;
import de.schule.georhelm.schulprojekt.utilities.PixelConverter;
import de.schule.georhelm.schulprojekt.playerobjects.Player;
import de.schule.georhelm.schulprojekt.R;

public class BitmapManager {

    //#region properties
    private Bitmap player;
    private Bitmap minime;
    private Bitmap minimeEnemy;
    private Bitmap button;
    private Bitmap dustyCloud;
    private Bitmap background;
    private Context context;
    //#endregion properties

    //#region constructor
    public BitmapManager(Context context, Player playerObject){
        Paint paint = new Paint();
        int backgroundWidth = 1920;
        int backgroundHeight = 1080;

        this.context = context;
        this.background = bitMapPrepare(R.drawable.backgroundsymmetricfinal,backgroundWidth,backgroundHeight);
        this.button = bitMapPrepare(R.drawable.button_red_medium,450,150);
        this.dustyCloud = bitMapPrepare(R.drawable.dustycloud,2100,1400);
        this.minime = bitMapPrepare(R.drawable.knightsminime,50,50);
        this.minimeEnemy = bitMapPrepare(R.drawable.knightsminime,50,50,true);

        this.player = Bitmap.createBitmap(PixelConverter.convertWidth(backgroundWidth, context),PixelConverter.convertHeight(backgroundHeight, context), Bitmap.Config.ARGB_8888);
        Canvas playerCanvas = new Canvas(this.player);

        //Player Mount
        playerCanvas.drawBitmap(
                playerObject.getMount().getBitmap(),
                playerObject.getMount().getMatrix(),
                paint);
        //Player
        playerCanvas.drawBitmap(
                playerObject.getBitmap(),
                playerObject.getMatrix(),
                paint);
    }
    //#endregion constructor

    //#region getters
    public Bitmap getPlayer() {
        return player;
    }

    public Bitmap getMinime() {
        return minime;
    }

    public Bitmap getMinimeEnemy() {
        return minimeEnemy;
    }

    public Bitmap getButton() {
        return button;
    }

    public Bitmap getDustyCloud() {
        return dustyCloud;
    }

    public Bitmap getBackground() {
        return background;
    }
    //#endregion getters

    //#region private methods
    /**
     * Prepares a bitmap resource to be loaded performant, it reduces the size of loaded bitmap.
     * @param resource The recource to be loaded.
     * @param width The width the bitmap will have.
     * @param height The height the bitmap will have.
     * @return
     */
    private Bitmap bitMapPrepare(int resource, int width, int height){
        return bitMapPrepare(resource,width,height,false);
    }
    /**
     * Prepares a bitmap resource to be loaded performant, it reduces the size of loaded bitmap.
     * @param resource The recource to be loaded.
     * @param width The width the bitmap will have.
     * @param height The height the bitmap will have.
     * @param inverseBitmap Determines if the bitmap should be mirrored.
     * @return
     */
    private Bitmap bitMapPrepare(int resource, int width, int height, boolean inverseBitmap){
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(context.getResources(), resource, bitmapOptions);
        bitmapOptions.inSampleSize = GameView.calculateInSampleSize(bitmapOptions, PixelConverter.convertWidth(width,context), PixelConverter.convertHeight(height,context));
        bitmapOptions.inJustDecodeBounds = false;
        Bitmap prepareBitmapHolder = BitmapFactory.decodeResource(context.getResources(), resource, bitmapOptions);
        prepareBitmapHolder = Bitmap.createScaledBitmap(prepareBitmapHolder,PixelConverter.convertWidth(width,context), PixelConverter.convertHeight(height,context),true);

        if(inverseBitmap){
            Matrix inverseMatrix = new Matrix();
            inverseMatrix.preScale(-1f,1f);
            prepareBitmapHolder = Bitmap.createBitmap(prepareBitmapHolder,0,0,prepareBitmapHolder.getWidth(),prepareBitmapHolder.getHeight(),inverseMatrix,true);
        }
        return prepareBitmapHolder;
    }
    //#endregion private methods
}
