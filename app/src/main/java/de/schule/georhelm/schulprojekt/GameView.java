package de.schule.georhelm.schulprojekt;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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
    private SurfaceHolder surfaceHolder;
    private Bitmap backGround;
    private Bitmap croppedEnemyBackground;
    private Matrix enemyBackGroundMatrix;
    private Bitmap enemyBackGround;
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

    public GameView(Context context,JSONObject gameData) {
        super(context);
        JSONObject player1 = new JSONObject();
        JSONObject player2 = new JSONObject();

        this.isEndgame = false;
        this.enemyOffset = 0;
        this.enemyBackgroundOffset = 0;
        this.playerSpeed = 0;

        try{
            player1 = gameData.getJSONObject("player1");
            player2 = gameData.getJSONObject("player2");
        }catch (Exception e){
            e.printStackTrace();
        }

        player = new Player(context,player1,false);
        enemy = new Player(context,player2, true);

        surfaceHolder = getHolder();
        paint = new Paint();

        // reduces size of loaded bitmap by ~5MB
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(context.getResources(), R.drawable.backgroundwithclouds, options);

        options.inSampleSize = GameView.calculateInSampleSize(options, 7680, 1080);
        options.inJustDecodeBounds = false;

        backGround = BitmapFactory.decodeResource(context.getResources(), R.drawable.backgroundwithclouds, options);
        backGround = Bitmap.createScaledBitmap(backGround,7680,1080,true);

        backGroundMatrix = new Matrix();

        enemyBackGround = BitmapFactory.decodeResource(context.getResources(), R.drawable.backgroundwithclouds, options);
        enemyBackGround = Bitmap.createScaledBitmap(enemyBackGround,7680,1080,true);

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
            if(this.isEndgame){
                player.setPos(player.getPos()+(int)(playerSpeed*timeSinceLastUpdate));
                if(canvas.getWidth()/2>this.enemyBackgroundOffset){
                    this.enemyOffset+=10;
                    this.enemyBackgroundOffset+=10;
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

        backGroundMatrix.postTranslate(-(player.getPos() % (backGround.getWidth() -canvas.getWidth())),-60);

        enemyBackGroundMatrix.reset();
        enemyBackGroundMatrix.postTranslate(960+this.enemyBackgroundOffset, -60);
        int bitmapPos = (enemy.getPos() % (enemyBackGround.getWidth() - canvas.getWidth()/2));
        if (bitmapPos < 0){
            enemy.setPos(enemyBackGround.getWidth() - canvas.getWidth()/2);
            bitmapPos = enemy.getPos();
        }
        croppedEnemyBackground = Bitmap.createBitmap(enemyBackGround, bitmapPos,0, canvas.getWidth()/2,enemyBackGround.getHeight());

        canvas.drawBitmap(backGround,backGroundMatrix, paint); //background here R.drawable.backgroundwithclouds
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

        Bitmap bitmapPlayer = Bitmap.createBitmap(1920,1080,Bitmap.Config.ARGB_8888);
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
        //Player Lance
        playerCanvas.drawBitmap(
                player.lance.getBitmap(),
                player.lance.getMatrix(),
                paint);
        Matrix playerCanvasMatrix = new Matrix();
        //playerCanvasMatrix.preScale(-1,1,bitmapPlayer.getWidth()/2,bitmapPlayer.getHeight()/2);
        playerCanvasMatrix.postTranslate(0,0);
        playerCanvasMatrix.postScale(0.9f,0.9f);


        Bitmap bitmapEnemy = Bitmap.createBitmap(1920,1080,Bitmap.Config.ARGB_8888);
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
        enemyCanvasMatrix.postScale(0.9f,0.9f);

        canvas.drawBitmap(bitmapEnemy,enemyCanvasMatrix,paint);
        canvas.drawBitmap(bitmapPlayer,playerCanvasMatrix,paint);
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
        try{
            gameThread.join();
        }catch(InterruptedException e){

        }
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
        try {
            this.gameThread.join();
        }catch(Exception e){
            e.printStackTrace();
        }
        this.backGround.recycle();
        this.enemyBackGround.recycle();
        this.croppedEnemyBackground.recycle();
        Activity activity = (Activity)this.getContext();
        activity.finish();
    }

    public void countDown(int count){
        this.countDownCount = count;
    }

    public void setPlayerPositions(int playerPos, int enemyPos){
        this.player.setPos(playerPos);
        this.enemy.setPos(enemyPos);
    }

    public void setLanceAngles(int playerLanceAngle, int enemyLanceAngle) {
        this.player.setLanceAngle(playerLanceAngle);
        this.enemy.setLanceAngle(enemyLanceAngle);
    }
}
