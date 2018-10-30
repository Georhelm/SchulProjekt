package de.schule.georhelm.schulprojekt;

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
            canvas.drawBitmap(backGround,-player.getPos()%(backGround.getWidth()-canvas.getWidth()),-60, paint); //background here R.drawable.backgroundwithclouds
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
        //Player Mount
        canvas.drawBitmap(
                player.mount.getBitmap(),
                player.mount.getMatrix(),
                paint);
        //Player
        canvas.drawBitmap(
                player.getBitmap(),
                player.getMatrix(),
                paint);
        //Player Lance
        canvas.drawBitmap(
                player.lance.getBitmap(),
                player.lance.getMatrix(),
                paint);
        Bitmap bitmapTest = Bitmap.createBitmap(7680,1080,Bitmap.Config.ARGB_8888);
        Canvas enemyCanvas = new Canvas(bitmapTest);

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
        Matrix matrix = new Matrix();
        matrix.preScale(-1,1,bitmapTest.getWidth()/2,bitmapTest.getHeight()/2);
        matrix.postTranslate(bitmapTest.getWidth()/2,bitmapTest.getHeight()/2);
        matrix.postScale(0.1f,0.1f);
        canvas.drawBitmap(bitmapTest,matrix,paint);
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

    public void countDown(int count){
        this.countDownCount = count;
    }

    public void setPlayerPositions(int playerPos, int enemyPos){
        this.player.setPos(playerPos);
        this.enemy.setPos(enemyPos);
    }
}
