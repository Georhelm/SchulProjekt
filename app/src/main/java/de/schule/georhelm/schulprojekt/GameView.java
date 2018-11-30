package de.schule.georhelm.schulprojekt;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
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
    private Matrix enemyBackGroundMatrix;
    private Matrix backGroundMatrix;
    private ConnectionSocket socket;
    private int countDownCount;
    private long timeOfLastUpdate;
    private int enemyOffset;
    private int enemyBackgroundOffset;
    private boolean isEndRound;
    int enemySpeed;
    int playerSpeed;
    private int backgroundWidth;
    private int backgroundHeight;
    private Context context;
    private Bitmap button;
    private Bitmap dustyCloud;
    private int frameCounter;
    private boolean continueButtonEnabled;
    private boolean isEndGame;

    public GameView(Context context,JSONObject gameData) {
        super(context);

        this.context = context;
        JSONObject player1 = new JSONObject();
        JSONObject player2 = new JSONObject();


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
        this.isEndGame = false;

        this.bitmapPlayer = Bitmap.createBitmap(PixelConverter.convertWidth(1920, context),PixelConverter.convertHeight(1080, context), Bitmap.Config.ARGB_8888);
        Canvas playerCanvas = new Canvas(bitmapPlayer);

        //Player Mount
        playerCanvas.drawBitmap(
                player.getMount().getBitmap(),
                player.getMount().getMatrix(),
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

        BitmapFactory.Options buttonOptions = new BitmapFactory.Options();
        buttonOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(context.getResources(), R.drawable.button_red_medium, buttonOptions);
        buttonOptions.inSampleSize = GameView.calculateInSampleSize(buttonOptions, PixelConverter.convertWidth(600,context), PixelConverter.convertHeight(100,context));
        buttonOptions.inJustDecodeBounds = false;
        button = BitmapFactory.decodeResource(context.getResources(), R.drawable.button_red_medium, buttonOptions);

        BitmapFactory.Options dustyCloudOptions = new BitmapFactory.Options();
        dustyCloudOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(context.getResources(), R.drawable.dustycloud, dustyCloudOptions);
        dustyCloudOptions.inSampleSize = GameView.calculateInSampleSize(dustyCloudOptions, PixelConverter.convertWidth(1920,context), PixelConverter.convertHeight(800,context));
        dustyCloudOptions.inJustDecodeBounds = false;
        dustyCloud = BitmapFactory.decodeResource(context.getResources(), R.drawable.dustycloud, dustyCloudOptions);

        backGroundMatrix = new Matrix();

        enemyBackGroundMatrix = new Matrix();

        this.socket = ConnectionSocket.getSocket();

        initNewRound();
    }

    public void initNewRound(){
        this.frameCounter = -50;
        this.player.resetPos();
        this.player.setLanceAngle(90);
        this.enemy.resetPos();
        this.enemy.setLanceAngle(90);
        this.continueButtonEnabled = false;
        this.isEndRound = false;
        this.enemyOffset = 0;
        this.enemyBackgroundOffset = 0;
        this.playerSpeed = 0;
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
        //this.finishGame();
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
        this.timeOfLastUpdate = newTime;
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.clipRect(0, 0, getWidth(), getHeight());
            continueButtonEnabled = false;
            Boolean drawCloud = false;
            if(this.isEndRound){
                player.setPos(player.getPos()+(int)(playerSpeed*timeSinceLastUpdate));
                if(canvas.getWidth()/2>this.enemyBackgroundOffset){
                    long offSetUpdate = Math.round(canvas.getWidth() / 7 * timeSinceLastUpdate);
                    this.enemyOffset+= offSetUpdate;
                    this.enemyBackgroundOffset+= offSetUpdate;
                    int tempPos = (int)(enemy.getPos()-enemySpeed*timeSinceLastUpdate);
                    if(tempPos < 0) {
                        tempPos = background.getWidth() - canvas.getWidth()/2;
                    }
                    enemy.setPos(tempPos);

                }else{
                    this.enemyOffset-=enemySpeed*timeSinceLastUpdate+playerSpeed*timeSinceLastUpdate;
                    if(this.enemyOffset + this.enemy.getX()<=this.player.getX()){
                        //this.playing = false;
                        continueButtonEnabled = true;
                    }
                    if(this.enemyOffset + this.enemy.getX()<=this.player.getX()+this.player.getWidth() && frameCounter<255){
                        drawCloud= true;
                    }
                }
            }
            drawGameFlow(canvas,timeSinceLastUpdate);
            if(isEndRound){
                //drawLanceHitHeight(canvas);
                //drawHitpointMessage(canvas);
                if(drawCloud){
                    drawCloud(canvas);
                    this.enemy.updateHitpoints();
                    this.player.updateHitpoints();
                }
                if(continueButtonEnabled){
                    drawContinueButton(canvas);
                }
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawCloud(Canvas canvas) {
        Paint cloudPaint = new Paint();
        cloudPaint.setAlpha(Math.min(255-frameCounter,255));
        canvas.drawBitmap(dustyCloud,PixelConverter.convertWidth(-100,context),PixelConverter.convertY(-50, dustyCloud.getHeight(),context), cloudPaint);
        frameCounter+=5;
    }

    private void drawContinueButton(Canvas canvas) {
        canvas.drawBitmap(button,canvas.getWidth()/2-button.getWidth()/2,canvas.getHeight()/2-button.getHeight()/2, paint);
        Paint buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.BLACK);
        buttonTextPaint.setStyle(Paint.Style.FILL);
        buttonTextPaint.setTextSize(this.getResources().getDimensionPixelSize(R.dimen.fontSizeSmall));
        String text = "Next round";
        if(isEndGame){
            text = "Return to menu";
        }

        Rect bounds = new Rect();
        buttonTextPaint.getTextBounds(text,0,text.length(),bounds);
        canvas.drawText(text,canvas.getWidth()/2-bounds.exactCenterX(), canvas.getHeight()/2-bounds.exactCenterY(), buttonTextPaint);
    }

    /*
    Method for debugging purposes
     */
    private void drawLanceHitHeight(Canvas canvas) {
        canvas.drawLine(0, PixelConverter.convertY(player.getLance().getTipYPos(),1,this.context), PixelConverter.convertWidth(1920, this.context),PixelConverter.convertY(player.getLance().getTipYPos(),1,this.context), paint);
    }

    private void drawGameFlow(Canvas canvas, double timeSinceLastUpdate) {

        backGroundMatrix.reset();

        backGroundMatrix.postTranslate(-(player.getPos() % canvas.getWidth()),-canvas.getHeight() / 18);

        enemyBackGroundMatrix.reset();
        enemyBackGroundMatrix.postTranslate(canvas.getWidth() / 2 +this.enemyBackgroundOffset, -canvas.getHeight() / 18);
        int bitmapPos = (enemy.getPos() % (background.getWidth() - canvas.getWidth()/2));
        Bitmap croppedEnemyBackground = Bitmap.createBitmap(background, bitmapPos,0, canvas.getWidth()/2,background.getHeight());

        canvas.drawBitmap(background,backGroundMatrix, paint);
        backGroundMatrix.postTranslate(background.getWidth(), 0);
        canvas.drawBitmap(background, backGroundMatrix, paint);
        canvas.drawBitmap(croppedEnemyBackground,enemyBackGroundMatrix, paint);
        drawPlayerObjects(canvas);
        drawText(canvas);
        drawHealthBars(canvas);
        //showEnemyHitBoxes(canvas);

        if(!isEndRound){
            drawDividerLine(canvas);
        }
    }

    private void drawDividerLine(Canvas canvas) {
        Paint dividerLinePaint = new Paint();
        dividerLinePaint.setStrokeWidth(6);
        dividerLinePaint.setColor(Color.WHITE);
        canvas.drawLine(canvas.getWidth()/2,0,canvas.getWidth()/2,canvas.getHeight(),dividerLinePaint);
        dividerLinePaint.setColor(Color.GRAY);
        dividerLinePaint.setStrokeWidth(2);
        canvas.drawLine(canvas.getWidth()/2-4,0,canvas.getWidth()/2-4,canvas.getHeight(),dividerLinePaint);
        canvas.drawLine(canvas.getWidth()/2+4,0,canvas.getWidth()/2+4,canvas.getHeight(),dividerLinePaint);

    }

    private void drawHealthBars(Canvas canvas) {
        Paint healthBarPaint = new Paint();
        healthBarPaint.setColor(Color.RED);
        healthBarPaint.setStrokeWidth(50);
        canvas.drawLine(PixelConverter.convertWidth(100,context),PixelConverter.convertHeight(100,context),PixelConverter.convertWidth(100+player.getHitpoints()*5,context),PixelConverter.convertHeight(100,context),healthBarPaint);
        canvas.drawLine(PixelConverter.convertWidth(canvas.getWidth(),context),PixelConverter.convertHeight(100,context),PixelConverter.convertWidth(canvas.getWidth()-enemy.getHitpoints()*5,context),PixelConverter.convertHeight(100,context),healthBarPaint);
    }

    /*
    Method for debugging purposes
     */
    private void showEnemyHitBoxes(Canvas canvas) {
        Paint paintDos = new Paint();
        paintDos.setStrokeWidth(20);
        paintDos.setColor(Color.RED);
        canvas.drawLine(this.enemy.getX()+400,this.enemy.mountHeight-75,this.enemy.getX()+400,this.enemy.mountHeight-175, paintDos);
        paintDos.setColor(Color.YELLOW);
        canvas.drawLine(this.enemy.getX()+400,this.enemy.mountHeight-75,this.enemy.getX()+400,this.enemy.mountHeight+25, paintDos);
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
    /*
    Method for debugging purposes
     */
    private void drawHitpointMessage(Canvas canvas){
        String hit;
        switch (player.getLastHit()){
            case 0:
                hit="Head";
                break;
            case 1:
                hit="Body";
                break;
            case 2:
                hit="Miss";
                break;
            default:
                hit="Miss";
        }
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(this.getResources().getDimensionPixelSize(R.dimen.fontSizeMedium));
        canvas.drawText(hit,canvas.getWidth()/2, canvas.getHeight()/4, paint);
    }

    private void drawPlayerObjects(Canvas canvas) {



        Bitmap bitmapEnemy = Bitmap.createBitmap(canvas.getWidth(),canvas.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas enemyCanvas = new Canvas(bitmapEnemy);

        //Enemy Mount
        enemyCanvas.drawBitmap(
                enemy.getMount().getBitmap(),
                enemy.getMount().getMatrix(),
                paint);
        //Enemy
        enemyCanvas.drawBitmap(
                enemy.getBitmap(),
                enemy.getMatrix(),
                paint);
        //Enemy Lance
        enemyCanvas.drawBitmap(
                enemy.getLance().getBitmap(),
                enemy.getLance().getMatrix(),
                paint);
        Matrix enemyCanvasMatrix = new Matrix();
        enemyCanvasMatrix.preScale(-1,1,bitmapEnemy.getWidth()/2,bitmapEnemy.getHeight()/2);
        enemyCanvasMatrix.postTranslate(bitmapEnemy.getWidth()/2+this.enemyOffset,0);
        //enemyCanvasMatrix.postScale(0.9f,0.9f);

        canvas.drawBitmap(bitmapEnemy,enemyCanvasMatrix,paint);
        canvas.drawBitmap(bitmapPlayer, 0, 0,paint);
        //Player Lance
        canvas.drawBitmap(
                player.getLance().getBitmap(),
                player.getLance().getMatrix(),
                paint);

        bitmapEnemy.recycle();

        // --------- DEBUG --------------


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
        if(continueButtonEnabled){
            int x = (int)motionEvent.getX();
            int y = (int)motionEvent.getY();
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                int buttonX = PixelConverter.convertWidth(1920,context)/2-button.getWidth()/2;
                int buttonY = PixelConverter.convertHeight(1080,context)/2-button.getHeight()/2;
                if(x<=buttonX+button.getWidth() && x>=buttonX && y<=buttonY+button.getHeight() && y>= buttonY){
                    if(isEndGame){
                        finishGame();
                    }else{
                        this.startRound();
                    }
                }
            }
        }
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

    private void startRound() {
        this.initNewRound();
    }

    public void endRound(int enemySpeed,int enemyHitpoints,int enemyWeaponHeight,int enemyPointHit,int playerSpeed,int playerHitpoints,int playerWeaponHeight,int playerPointHit, boolean endOfGame) {
        this.isEndRound = true;
        this.enemySpeed = enemySpeed;
        this.enemy.setNextHitpoints(enemyHitpoints);
        this.playerSpeed = playerSpeed;
        this.player.setNextHitpoints(playerHitpoints);
        this.player.getLance().setTipYPos(playerWeaponHeight);
        this.player.setLastHit(playerPointHit);
        if(endOfGame){
            isEndGame = true;
        }
    }

    private void finishGame() {
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
