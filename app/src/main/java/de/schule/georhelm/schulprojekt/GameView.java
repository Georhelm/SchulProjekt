package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
            canvas.drawBitmap(backGround,-player.getPos(),0, paint); //background here R.drawable.backgroundwithclouds
            drawPlayerObjects();
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawPlayerObjects() {
        canvas.drawBitmap(
            player.mount.getBitmap(),
            player.mount.matrix,
                paint);
        canvas.drawBitmap(
                player.getBitmap(),
                player.getX(),
                player.getY(),
                paint);
        canvas.drawBitmap(
                player.lance.getBitmap(),
                player.lance.matrix,
                paint);
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
}
