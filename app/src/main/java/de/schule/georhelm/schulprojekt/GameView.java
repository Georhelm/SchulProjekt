package de.schule.georhelm.schulprojekt;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.json.JSONObject;

public class GameView extends SurfaceView implements Runnable{

    volatile boolean playing;

    private Thread gameThread = null;
    private Player player;
    private Player enemy;
    private Paint paint;
    private Bitmap bitmapPlayer;
    private SurfaceHolder surfaceHolder;
    private Bitmap background;
    private Bitmap croppedEnemyBackground;
    private Matrix enemyBackGroundMatrix;
    private Matrix backGroundMatrix;
    private ConnectionSocket socket;
    private int countDownCount;
    private long timeOfLastUpdate;
    private int enemyOffset;
    private int enemyBackgroundOffset;
    private boolean isEndgame;
    int enemySpeed;
    int enemyHitpoint;
    int playerSpeed;
    int playerHitpoint;
    private int backgroundWidth;
    private int backgroundHeight;

    public GameView(Context context,JSONObject gameData) {
        super(context);
        JSONObject player1 = new JSONObject();
        JSONObject player2 = new JSONObject();

        this.isEndgame = false;
        this.enemyOffset = 0;
        this.enemyBackgroundOffset = 0;
        this.playerSpeed = 0;

        this.backgroundHeight = PixelConverter.convertHeight(1080, context);
        this.backgroundWidth = PixelConverter.convertWidth(1920, context);

        try{
            player1 = gameData.getJSONObject("player1");
            player2 = gameData.getJSONObject("player2");
        }catch (Exception e){
            e.printStackTrace();
        }

        player = new Player(context,player1,false);
        enemy = new Player(context,player2, true);

        this.bitmapPlayer = Bitmap.createBitmap(PixelConverter.convertWidth(1920, context),PixelConverter.convertHeight(1080, context), Bitmap.Config.ARGB_8888);
        Canvas playerCanvas = new Canvas(bitmapPlayer);

        //Player Mount
        playerCanvas.drawBitmap(
                player.mount.getBitmap(),
                player.mount.getMatrix(),
                paint);
        //Player
        playerCanvas.drawBitmap(
                player.getBitmap(),
                player.getMatrix(),
                paint);

        surfaceHolder = getHolder();
        paint = new Paint();

        // reduces size of loaded bitmap by ~5MB
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(context.getResources(), R.drawable.backgroundsymmetricfinal, options);

        options.inSampleSize = GameView.calculateInSampleSize(options, this.backgroundWidth, this.backgroundHeight);
        options.inJustDecodeBounds = false;

        background = BitmapFactory.decodeResource(context.getResources(), R.drawable.backgroundsymmetricfinal, options);
        background = Bitmap.createScaledBitmap(background,this.backgroundWidth,this.backgroundHeight,true);

        backGroundMatrix = new Matrix();

        enemyBackGroundMatrix = new Matrix();


        this.socket = ConnectionSocket.getSocket();
        this.socket.playerReady();

        this.socket.initGame(this);
    }

    @Override
    public void run(){
        this.timeOfLastUpdate = System.nanoTime();
        while(playing){
            update();

            draw();

            control();
        }
        this.finishGame();
    }

    // copied from https://developer.android.com/topic/performance/graphics/load-bitmap#java
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void update(){
    }

    private void draw(){
        long newTime = System.nanoTime();
        double timeSinceLastUpdate = (newTime - this.timeOfLastUpdate) / 1000000000f;
        /*if(timeSinceLastUpdate > 50) {
            System.out.println("Time since last Update: " + timeSinceLastUpdate);
        }*/
        this.timeOfLastUpdate = newTime;
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.clipRect(0, 0, getWidth(), getHeight());
            if(this.isEndgame){
                player.setPos(player.getPos()+(int)(playerSpeed*timeSinceLastUpdate));
                if(canvas.getWidth()/2>this.enemyBackgroundOffset){
                    long offSetUpdate = Math.round(canvas.getWidth() / 7 * timeSinceLastUpdate);
                    this.enemyOffset+= offSetUpdate;
                    this.enemyBackgroundOffset+= offSetUpdate;
                    enemy.setPos((int)(enemy.getPos()-enemySpeed*timeSinceLastUpdate));
                }else{
                    this.enemyOffset-=enemySpeed*timeSinceLastUpdate+playerSpeed*timeSinceLastUpdate;
                    if(this.enemyOffset + this.enemy.getX()<=this.player.getX()){
                        this.playing = false;
                    }
                }
            }
            drawGameFlow(canvas,timeSinceLastUpdate);
            /*long timeForUpdate = (System.nanoTime() - this.timeOfLastUpdate) / 1000000;
            if (timeForUpdate > 50) {
                System.out.println("Time for update: " + timeForUpdate);
            }*/
        }
    }

    private void drawGameFlow(Canvas canvas, double timeSinceLastUpdate) {

        backGroundMatrix.reset();

        backGroundMatrix.postTranslate(-(player.getPos() % canvas.getWidth()),-canvas.getHeight() / 18);

        enemyBackGroundMatrix.reset();
        enemyBackGroundMatrix.postTranslate(canvas.getWidth() / 2 +this.enemyBackgroundOffset, -canvas.getHeight() / 18);
        int bitmapPos = (enemy.getPos() % (background.getWidth() - canvas.getWidth()/2));
        if (bitmapPos > 0){
            croppedEnemyBackground = Bitmap.createBitmap(background, bitmapPos,0, canvas.getWidth()/2,background.getHeight());
        }

        canvas.drawBitmap(background,backGroundMatrix, paint);
        backGroundMatrix.postTranslate(background.getWidth(), 0);
        canvas.drawBitmap(background, backGroundMatrix, paint);
        canvas.drawBitmap(croppedEnemyBackground,enemyBackGroundMatrix, paint);
        drawPlayerObjects(canvas);
        drawText(canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void drawText(Canvas canvas){
        if(this.countDownCount > 0){
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(this.getResources().getDimensionPixelSize(R.dimen.fontSizeMedium));
            canvas.drawText(Integer.toString(this.countDownCount),canvas.getWidth()/2, canvas.getHeight()/4, paint);
        }
    }

    private void drawPlayerObjects(Canvas canvas) {



        Bitmap bitmapEnemy = Bitmap.createBitmap(canvas.getWidth(),canvas.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas enemyCanvas = new Canvas(bitmapEnemy);

        //Enemy Mount
        enemyCanvas.drawBitmap(
                enemy.mount.getBitmap(),
                enemy.mount.getMatrix(),
                paint);
        //Enemy
        enemyCanvas.drawBitmap(
                enemy.getBitmap(),
                enemy.getMatrix(),
                paint);
        //Enemy Lance
        enemyCanvas.drawBitmap(
                enemy.lance.getBitmap(),
                enemy.lance.getMatrix(),
                paint);
        Matrix enemyCanvasMatrix = new Matrix();
        enemyCanvasMatrix.preScale(-1,1,bitmapEnemy.getWidth()/2,bitmapEnemy.getHeight()/2);
        enemyCanvasMatrix.postTranslate(bitmapEnemy.getWidth()/2+this.enemyOffset,0);
        //enemyCanvasMatrix.postScale(0.9f,0.9f);

        canvas.drawBitmap(bitmapEnemy,enemyCanvasMatrix,paint);
        canvas.drawBitmap(bitmapPlayer, 0, 0,paint);
        //Player Lance
        canvas.drawBitmap(
                player.lance.getBitmap(),
                player.lance.getMatrix(),
                paint);
        //Testing draw Lance ange
        //canvas.drawCircle(canvas.getWidth()/2,7+canvas.getHeight()-player.lance.getLancetipYPos(), 14,new Paint(Color.GREEN));
        canvas.drawLine(0,canvas.getHeight()-player.lance.getLancetipYPos(),1920,canvas.getHeight()-player.lance.getLancetipYPos(),new Paint(Color.GREEN));
        //canvas.drawCircle(canvas.getWidth()/2,enemy.lance.getLancetipYPos(), 30,new Paint(Color.RED));
        //Testing draw Lance angle
        bitmapEnemy.recycle();
    }

    private void control(){
        //try{
        //    gameThread.sleep(17);
        //}catch(InterruptedException e){
        //   e.printStackTrace();
        //}
    }

    public void pause(){
        playing = false;
    }

    public void resume(){
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.player.lanceUp();
                break;
            case MotionEvent.ACTION_UP:
                this.player.lanceDown();
                break;
        }
        return true;
    }

    public void endGame(int enemySpeed, int enemyHitpoint, int playerSpeed, int playerHitpoint) {
        this.isEndgame = true;
        this.enemySpeed = enemySpeed;
        this.enemyHitpoint = enemyHitpoint;
        this.playerSpeed = playerSpeed;
        this.playerHitpoint = playerHitpoint;
    }

    private void finishGame() {
        this.background.recycle();
        this.croppedEnemyBackground.recycle();
        this.bitmapPlayer.recycle();
        Activity activity = (Activity)this.getContext();
        activity.finish();
    }

    public void countDown(int count){
        this.countDownCount = count;
    }

    public void setPlayerPositions(int playerPos, int enemyPos, int playerLancetipYPos, int enemyLancetipYPos){
        this.player.setPos(playerPos);
        this.enemy.setPos(enemyPos);
        this.player.lance.setLancetipYPos(playerLancetipYPos);
        this.enemy.lance.setLancetipYPos(enemyLancetipYPos);
    }

    public void setLanceAngles(int playerLanceAngle, int enemyLanceAngle) {
        this.player.setLanceAngle(playerLanceAngle);
        this.enemy.setLanceAngle(enemyLanceAngle);
    }
}
