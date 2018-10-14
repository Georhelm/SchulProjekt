package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable{

    volatile boolean playing;

    private Thread gameThread = null;
    private int location;
    private Player player;
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Bitmap backGround;
    private int screenWidth;
    private int screenHeight;

    public GameView(Context context, int screenX, int screenY) {
        super(context);
        screenWidth = screenX;
        screenHeight = screenY;
        player = new Player(context, screenWidth, screenHeight);

        surfaceHolder = getHolder();
        paint = new Paint();
        location = 0;
        backGround = BitmapFactory.decodeResource(context.getResources(), R.drawable.backgroundwithclouds);
        backGround = Bitmap.createScaledBitmap(backGround,screenWidth*12,(int)Math.round(screenHeight*1.2),true);
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
        player.update();
        System.out.println("player speed: " +player.getSpeed());
        location = location - player.getSpeed();
        System.out.println("location: " +location);
    }

    private void draw(){
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawBitmap(backGround,location,(int)Math.round(-screenHeight*0.25), paint); //background here R.drawable.backgroundwithclouds
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
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                player.stopLiftingLance();
                break;
            case MotionEvent.ACTION_DOWN:
                player.liftLance();
                break;
        }
        return true;
    }
}
