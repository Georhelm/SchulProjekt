package de.schule.georhelm.schulprojekt.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.json.JSONObject;

import de.schule.georhelm.schulprojekt.managers.SoundManager;
import de.schule.georhelm.schulprojekt.utilities.ConnectionSocket;
import de.schule.georhelm.schulprojekt.managers.BitmapManager;
import de.schule.georhelm.schulprojekt.managers.PaintManager;
import de.schule.georhelm.schulprojekt.utilities.PixelConverter;
import de.schule.georhelm.schulprojekt.playerobjects.Player;
import de.schule.georhelm.schulprojekt.R;

@SuppressLint("ViewConstructor")
public class GameView extends SurfaceView implements Runnable{

    //#region properties
    private volatile boolean playing;
    private PaintManager paintManager;
    private BitmapManager bitmapManager;
    private Player player;
    private Player enemy;
    private Context context;
    private Matrix enemyBackGroundMatrix;
    private Matrix backGroundMatrix;
    private ConnectionSocket socket;
    private boolean isEndRound;
    private boolean continueButtonEnabled;
    private boolean isEndGame;
    private boolean gameWon;
    private long timeOfLastUpdate;
    private int frameCounter;
    private int lengthOfBattlefield;
    private int countDownCount;
    private int enemyOffset;
    private int enemyBackgroundOffset;
    //#endregion properties

    //#region constructor
    public GameView(Context context,JSONObject gameData) {
        super(context);
        this.context = context;
        JSONObject player1 = new JSONObject();
        JSONObject player2 = new JSONObject();
        try{
            player1 = gameData.getJSONObject("player1");
            player2 = gameData.getJSONObject("player2");
        }catch (Exception e){
            e.printStackTrace();
        }

        this.player = new Player(context,player1,false);
        this.enemy = new Player(context,player2, true);

        this.paintManager = new PaintManager(context);
        this.bitmapManager = new BitmapManager(this.context,this.player);
        this.isEndGame = false;
        this.lengthOfBattlefield = enemy.getPos();

        backGroundMatrix = new Matrix();
        enemyBackGroundMatrix = new Matrix();

        this.socket = ConnectionSocket.getSocket();

        initNewRound();
    }
    //#endregion constructor

    //#region public methods

    /**
     * Fires the three main phases: update, draw and control.
     */
    @Override
    public void run(){
        this.timeOfLastUpdate = System.nanoTime();
        while(playing){
            draw();
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
    /**
     * Lifts the lance on touch when the game is running. If it´s the end of the round, it controls if the button to continue is pressed.
     * Supressing Lintwarning - Operating this app through voice commands is not applicable.
     * @param motionEvent
     * @return returns true if its not the end of the round.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if(continueButtonEnabled){
            int x = (int)motionEvent.getX();
            int y = (int)motionEvent.getY();
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                Bitmap button = this.bitmapManager.getButton();
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

    /**
     * Sets property "playing" to false
     */
    public void pause(){
        playing = false;
    }
    /**
     * Sets property "playing" to true, starts the gameThread
     */
    public void resume(){
        playing = true;
        Thread gameThread = new Thread(this);
        gameThread.start();
    }
    /**
     * Sets the "countDownCount" property to the given integer.
     * @param count An integer to which value property "countDownCount" will be set.
     */
    public void countDown(int count){
        this.countDownCount = count;
    }
    /**
     * Sets the player and enemy positions to given values.
     * @param playerPos An integer representing the position the player will be set to.
     * @param enemyPos An integer representing the position the enemy will be set to.
     */
    public void setPlayerPositions(int playerPos, int enemyPos){
        this.player.setPos(playerPos);
        this.enemy.setPos(enemyPos);
    }
    /**
     * Sets the player and enemy lance angles to given values.
     * @param playerLanceAngle An integer representing the degree the players lance angle will have.
     * @param enemyLanceAngle An integer representing the degree the enemies lance angle will have.
     */
    public void setLanceAngles(int playerLanceAngle, int enemyLanceAngle) {
        this.player.setLanceAngle(playerLanceAngle);
        this.enemy.setLanceAngle(enemyLanceAngle);
    }
    //#endregion public methods

    //#region private methods
    private void draw(){
        SurfaceHolder surfaceHolder = getHolder();
        long newTime = System.nanoTime();
        double timeSinceLastUpdate = (newTime - this.timeOfLastUpdate) / 1000000000f;
        this.timeOfLastUpdate = newTime;
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.clipRect(0, 0, getWidth(), getHeight());
            continueButtonEnabled = false;
            Boolean drawCloud = false;
            if(this.isEndRound){
                player.setPos(player.getPos()+(int)(player.getEndSpeed()*timeSinceLastUpdate));
                if(canvas.getWidth()/2>this.enemyBackgroundOffset){
                    long offSetUpdate = Math.round(canvas.getWidth() / 7 * timeSinceLastUpdate);
                    this.enemyOffset+= offSetUpdate;
                    this.enemyBackgroundOffset+= offSetUpdate;
                    int tempPos = (int)(this.enemy.getPos()-this.enemy.getEndSpeed()*timeSinceLastUpdate);
                    if(tempPos < 0) {
                        tempPos = this.bitmapManager.getBackground().getWidth() - canvas.getWidth()/2;
                    }
                    enemy.setPos(tempPos);

                }else{
                    this.enemyOffset-=this.enemy.getEndSpeed()*timeSinceLastUpdate+this.player.getEndSpeed()*timeSinceLastUpdate;
                    if(this.enemyOffset + this.enemy.getX()<=this.player.getX()){
                        //this.playing = false;
                        continueButtonEnabled = true;
                    }
                    if(this.enemyOffset + this.enemy.getX()<=this.player.getX()+this.player.getWidth() && frameCounter<255){
                        drawCloud= true;
                    }
                }
            }
            drawGameFlow(canvas);
            if(isEndRound){
                //drawLanceHitHeight(canvas);
                //drawHitpointMessage(canvas);
                if(drawCloud){
                    SoundManager.playCrashSound(context);
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
    /**
     * Resets properties of this class to start a new Round
     */
    private void initNewRound(){
        this.frameCounter = -50;
        this.player.resetPos();
        this.player.setLanceAngle(90);
        this.enemy.resetPos();
        this.enemy.setLanceAngle(90);
        this.continueButtonEnabled = false;
        this.isEndRound = false;
        this.enemyOffset = 0;
        this.enemyBackgroundOffset = 0;
        this.player.setEndSpeed(0);
        this.enemy.setEndSpeed(0);
        this.socket.playerReady();
        this.socket.initGame(this);
        SoundManager.resetCrashSound();
    }
    /**
     * Draws the dusty cloud at the point where both players hit each other.
     * @param canvas The canvas on which the cloud will be drawn.
     */
    private void drawCloud(Canvas canvas) {
        Bitmap dustyCloud = bitmapManager.getDustyCloud();
        paintManager.updateCloudPaintAlpha(frameCounter);
        canvas.drawBitmap(dustyCloud,PixelConverter.convertWidth(-80,context),PixelConverter.convertY(-100, dustyCloud.getHeight(),context), paintManager.getCloudPaint());
        frameCounter+=5;
    }
    /**
     * Draws the button to continue (or return to menu) at the end of the round.
     * @param canvas The canvas on which the button will be drawn.
     */
    private void drawContinueButton(Canvas canvas) {
        Bitmap button = bitmapManager.getButton();
        canvas.drawBitmap(button,canvas.getWidth()/2-button.getWidth()/2,canvas.getHeight()/2-button.getHeight()/2, paintManager.getMainPaint());

        String text = "Next round";
        if(isEndGame){
            text = "Return to menu";
            drawEndgameResult(canvas);
        }

        Rect bounds = new Rect();
        Paint buttonTextPaint = paintManager.getButtonTextPaint();
        buttonTextPaint.getTextBounds(text,0,text.length(),bounds);
        canvas.drawText(text,canvas.getWidth()/2-bounds.exactCenterX(), canvas.getHeight()/2-bounds.exactCenterY(), buttonTextPaint);
    }
    /**
     * Calls the displayResult method with either "Victory!" or "Loss!" message.
     * @param canvas The canvas the message will be drawn on.
     */
    private void drawEndgameResult(Canvas canvas) {
        if(gameWon){
            this.displayResult("Victory!",canvas);
        }else{
            this.displayResult("Loss!", canvas);
        }
    }
    /**
     * Draws the Victory / Loss message on the canvas at the end of the game.
     * @param result A string containing the result message. Either "Victory!" or "Loss!".
     * @param canvas The canvas the result will be drawn onto.
     */
    private void displayResult(String result, Canvas canvas) {
        Rect bounds = new Rect();
        Paint resultMessagePaint = paintManager.getMessagePaint();
        resultMessagePaint.getTextBounds(result,0,result.length(),bounds);
        canvas.drawText(result,canvas.getWidth()/2-bounds.exactCenterX(), canvas.getHeight()/4-bounds.exactCenterY(), resultMessagePaint);
    }
    /**
     * Calls all the draw methods while the round is running.
     * @param canvas The canvas all the objects will be drawn onto.
     */
    private void drawGameFlow(Canvas canvas) {
        Bitmap background = bitmapManager.getBackground();
        backGroundMatrix.reset();

        backGroundMatrix.postTranslate(-(player.getPos() % canvas.getWidth()),-canvas.getHeight() / 18);

        enemyBackGroundMatrix.reset();
        enemyBackGroundMatrix.postTranslate(canvas.getWidth() / 2 +this.enemyBackgroundOffset, -canvas.getHeight() / 18);
        int bitmapPos = (enemy.getPos() % (background.getWidth() - canvas.getWidth()/2));
        Bitmap croppedEnemyBackground = Bitmap.createBitmap(background, bitmapPos,0, canvas.getWidth()/2,background.getHeight());
        Paint paint = paintManager.getMainPaint();
        canvas.drawBitmap(background,backGroundMatrix, paint);
        backGroundMatrix.postTranslate(background.getWidth(), 0);
        canvas.drawBitmap(background, backGroundMatrix, paint);
        canvas.drawBitmap(croppedEnemyBackground,enemyBackGroundMatrix, paint);
        drawPlayerObjects(canvas);
        drawHealthBars(canvas);
        //showEnemyHitBoxes(canvas);
        if(!isEndRound){
            drawDividerLine(canvas);
            drawMinimap(canvas);
        }
        drawCountdownText(canvas);
    }
    /**
     * Draws the mini-map showing the distance of both players.
     * @param canvas The canvas the mini-map will be drawn onto.
     */
    private void drawMinimap(Canvas canvas) {
        Bitmap bitmapMinime = bitmapManager.getMinime();
        Bitmap bitmapMinimeEnemy = bitmapManager.getMinimeEnemy();

        int mapStart = PixelConverter.convertWidth(500,context);
        int mapEnd = canvas.getWidth()- PixelConverter.convertWidth(500,context);
        int mapHeight = PixelConverter.convertHeight(200,context);
        canvas.drawLine(mapStart,mapHeight,mapEnd,mapHeight,paintManager.getMinimapLinePaint());
        int lengthOfMinimap = (mapEnd-mapStart);
        int minimeOffset = Math.round((player.getPos()/(float)lengthOfBattlefield)*lengthOfMinimap);
        int minimeEnemyOffset = lengthOfMinimap - Math.round((enemy.getPos()/(float)lengthOfBattlefield)*lengthOfMinimap);

        Paint paint = paintManager.getMainPaint();
        int minimeMapPosition = mapStart + minimeOffset-bitmapMinimeEnemy.getWidth();
        canvas.drawBitmap(bitmapMinime,minimeMapPosition, mapHeight-bitmapMinime.getHeight()/2,paint);

        int minimeEnemyMapPosition = mapEnd - minimeEnemyOffset;
        canvas.drawBitmap(bitmapMinimeEnemy,minimeEnemyMapPosition,mapHeight-bitmapMinimeEnemy.getHeight()/2,paint);
    }
    /**
     * Draws the line between the player and the enemy screen.
     * @param canvas The canvas the line will be drawn onto.
     */
    private void drawDividerLine(Canvas canvas) {
        Paint dividerLinePaintOne = paintManager.getDividerLinePaintOne();
        Paint dividerLinePaintTwo = paintManager.getDividerLinePaintTwo();
        canvas.drawLine(canvas.getWidth()/2,0,canvas.getWidth()/2,canvas.getHeight(),dividerLinePaintOne);
        canvas.drawLine(canvas.getWidth()/2-4,0,canvas.getWidth()/2-4,canvas.getHeight(),dividerLinePaintTwo);
        canvas.drawLine(canvas.getWidth()/2+4,0,canvas.getWidth()/2+4,canvas.getHeight(),dividerLinePaintTwo);

    }
    /**
     * Draws the healthbars of the player and enemy.
     * @param canvas The canvas the health bars will be drawn onto.
     */
    private void drawHealthBars(Canvas canvas) {
        int healthbarOffset = PixelConverter.convertWidth(100,context);
        int healthbarHeight = PixelConverter.convertHeight(100,context);
        int healthbarLength = PixelConverter.convertWidth(player.getHitpoints()*7,context);
        int enemyHealthbarLength = PixelConverter.convertWidth(enemy.getHitpoints() * 7, context);

        Paint healthBarPaint = paintManager.getHealthBarPaint();
        canvas.drawLine(healthbarOffset,healthbarHeight,healthbarLength+healthbarOffset,healthbarHeight,healthBarPaint);
        canvas.drawLine(canvas.getWidth()-healthbarOffset,healthbarHeight,canvas.getWidth()-healthbarOffset-enemyHealthbarLength,healthbarHeight,healthBarPaint);
    }
    /**
     * Draws the countdown at the beginning of the round.
     * @param canvas The canvas the countdown will be drawn onto.
     */
    private void drawCountdownText(Canvas canvas){
        if(this.countDownCount > 0){
            String countdownText = Integer.toString(this.countDownCount);
            Paint countDownPaint = paintManager.getMessagePaint();
            Rect bounds = new Rect();
            countDownPaint.getTextBounds(countdownText,0,countdownText.length(),bounds);
            canvas.drawText(countdownText,canvas.getWidth()/2-bounds.exactCenterX(), canvas.getHeight()/4-bounds.exactCenterY(), countDownPaint);
            //canvas.getWidth()/2-bounds.exactCenterX(), canvas.getHeight()/2-bounds.exactCenterY()
        }
    }
    /**
     * Draws all the objects
     * @param canvas
     */
    private void drawPlayerObjects(Canvas canvas) {

        Bitmap bitmapEnemy = Bitmap.createBitmap(canvas.getWidth(),canvas.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas enemyCanvas = new Canvas(bitmapEnemy);
        Paint paint = paintManager.getMainPaint();
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
        canvas.drawBitmap(this.bitmapManager.getPlayer(), 0, 0,paint);
        //Player Lance
        canvas.drawBitmap(
                player.getLance().getBitmap(),
                player.getLance().getMatrix(),
                paint);

        bitmapEnemy.recycle();

        // --------- DEBUG --------------


    }
    /**
     * Initializes new Round.
     */
    private void startRound() {
        this.initNewRound();
    }
    /**
     * Saves the properties of the game to the values at the time of the clash, so that the animation can start. Determines if the game is over and if the player has won.
     * @param enemySpeed An integer representing the speed of the enemy.
     * @param enemyHitpoints An integer representing the hitpoints of the enemy.
     * @param enemyWeaponHeight An integer representing the height of the lancetip of the enemy.
     * @param enemyPointHit An integer representing the point of the player that the enemy hit.
     * @param playerSpeed An integer representing the speed of the player.
     * @param playerHitpoints An integer representing the hitpoints of the player.
     * @param playerWeaponHeight An integer representing the height of the lancetip of the player.
     * @param playerPointHit An integer representing the point of the enemy that the player hit.
     * @param endOfGame A boolean value representing whether the game is over or not.
     * @param gameWon A boolean value representing whether the player has won or not.
     */
    public void endRound(int enemySpeed,int enemyHitpoints,int enemyWeaponHeight,int enemyPointHit,int playerSpeed,int playerHitpoints,int playerWeaponHeight,int playerPointHit, boolean endOfGame, boolean gameWon) {
        this.isEndRound = true;
        this.enemy.setEndSpeed(enemySpeed);
        this.enemy.setNextHitpoints(enemyHitpoints);
        this.player.setEndSpeed(playerSpeed);
        this.player.setNextHitpoints(playerHitpoints);
        this.player.getLance().setTipYPos(playerWeaponHeight);
        this.player.setLastHit(playerPointHit);
        if(endOfGame){
            this.isEndGame = true;
            this.gameWon=gameWon;
        }
    }
    /**
     * Ends this activity and therefor returns to the menu.
     */
    private void finishGame() {
        Activity activity = (Activity)this.getContext();
        activity.finish();
    }
    //#endregion private methods

    //#region debugging methods
    /**
     * Draws a horizontal line at the height of the tip of the player lance.
     * @param canvas The canvas the line will be drawn onto.
     */
    private void drawLanceHitHeight(Canvas canvas) {
        canvas.drawLine(0, PixelConverter.convertY(player.getLance().getTipYPos(),1,this.context),
                PixelConverter.convertWidth(1920, this.context),
                PixelConverter.convertY(player.getLance().getTipYPos(),1,this.context), paintManager.getMainPaint());
    }
    /**
     * Draws the Hit-Boxes of the enemy.
     * @param canvas The canvas the hit-boxes will be drawn onto.
     */
    private void showEnemyHitBoxes(Canvas canvas) {
        Paint paintDos = new Paint();
        paintDos.setStrokeWidth(20);
        paintDos.setColor(Color.RED);
        canvas.drawLine(this.enemy.getX()+400,this.enemy.getMountHeight()-75,this.enemy.getX()+400,this.enemy.getMountHeight()-175, paintDos);
        paintDos.setColor(Color.YELLOW);
        canvas.drawLine(this.enemy.getX()+400,this.enemy.getMountHeight()-75,this.enemy.getX()+400,this.enemy.getMountHeight()+25, paintDos);
    }
    /**
     * Draws a message on the screen where the player lance has hit the enemy.
     * @param canvas The canvas the message will be drawn onto.
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
    //#endregion debugging methods










}
