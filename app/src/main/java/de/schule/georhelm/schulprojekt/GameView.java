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
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Bitmap backGround;
    private Bitmap croppedEnemyBackground;
    private Matrix enemyBackGroundMatrix;
    private Bitmap enemyBackGround;
    private Matrix backGroundMatrix;
    private ConnectionSocket socket;
    private int countDownCount;

    public GameView(Context context,JSONObject gameData) {
        super(context);
        JSONObject player1 = new JSONObject();
        JSONObject player2 = new JSONObject();

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
        backGround = BitmapFactory.decodeResource(context.getResources(), R.drawable.backgroundwithclouds);
        backGround = Bitmap.createScaledBitmap(backGround,7680,1080,true);

        backGroundMatrix = new Matrix();

        enemyBackGround = BitmapFactory.decodeResource(context.getResources(), R.drawable.backgroundwithclouds);
        enemyBackGround = Bitmap.createScaledBitmap(enemyBackGround,7680,1080,true);

        enemyBackGroundMatrix = new Matrix();


        this.socket = ConnectionSocket.getSocket();
        this.socket.playerReady();

        this.socket.initGame(this);
    }

    @Override
    public void run(){
        while(playing){
            update();

            draw();

            control();
        }
    }

    private void update(){
    }

    private void draw(){
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            backGroundMatrix.reset();
            backGroundMatrix.postTranslate(-(player.getPos() % (backGround.getWidth() - 960)),-60);

            enemyBackGroundMatrix.reset();
            enemyBackGroundMatrix.postTranslate(960, -60);

            int bitmapPos = (enemy.getPos() % (enemyBackGround.getWidth() - 960));
            croppedEnemyBackground = Bitmap.createBitmap(enemyBackGround, bitmapPos,0, 960,enemyBackGround.getHeight());


            canvas.drawBitmap(backGround,backGroundMatrix, paint); //background here R.drawable.backgroundwithclouds
            canvas.drawBitmap(croppedEnemyBackground,enemyBackGroundMatrix, paint);
            drawPlayerObjects();
            drawText();
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawText(){
        if(this.countDownCount > 0){
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(this.getResources().getDimensionPixelSize(R.dimen.fontSizeMedium));
            canvas.drawText(Integer.toString(this.countDownCount),canvas.getWidth()/2, canvas.getHeight()/4, paint);
        }
    }

    private void drawPlayerObjects() {
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
        enemyCanvasMatrix.postTranslate(bitmapEnemy.getWidth()/2,0);
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
        /*switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                player.stopLiftingLance();
                break;
            case MotionEvent.ACTION_DOWN:
                player.liftLance();
                break;
        }*/
        return true;
    }

    public void endGame() {
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
}