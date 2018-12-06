package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

public class PaintManager {

    //#region properties
    private Paint mainPaint;
    private Paint minimapLinePaint;
    private Paint cloudPaint;
    private Paint buttonTextPaint;
    private Paint messagePaint;
    private Paint dividerLinePaintOne;
    private Paint dividerLinePaintTwo;
    private Paint healthBarPaint;

    //#endregion properties

    //#region constructor
    public PaintManager(Context context){

        this.mainPaint = new Paint();

        this.cloudPaint = new Paint();

        this.buttonTextPaint = new Paint();
        setTextOptions(this.buttonTextPaint, Color.BLACK, context.getResources().getDimensionPixelSize(R.dimen.fontSizeSmall));

        this.messagePaint = new Paint();
        setTextOptions(this.messagePaint, Color.RED, context.getResources().getDimensionPixelSize(R.dimen.fontSizeMedium));

        this.dividerLinePaintOne = new Paint();
        setLineOptions(this.dividerLinePaintOne, 6, Color.WHITE);

        this.minimapLinePaint = new Paint();
        setLineOptions(this.minimapLinePaint, 10,Color.LTGRAY);

        this.dividerLinePaintTwo = new Paint();
        setLineOptions(this.dividerLinePaintTwo,2,Color.GRAY);

        this.healthBarPaint = new Paint();
        setLineOptions(this.healthBarPaint,50,Color.RED);
        
    }

    //#endregion constructor

    //#region getters
    public Paint getMainPaint() {
        return mainPaint;
    }

    public Paint getMinimapLinePaint() {
        return minimapLinePaint;
    }

    public Paint getCloudPaint() {
        return cloudPaint;
    }

    public Paint getButtonTextPaint() {
        return buttonTextPaint;
    }

    public Paint getMessagePaint() {
        return messagePaint;
    }

    public Paint getDividerLinePaintOne() {
        return dividerLinePaintOne;
    }

    public Paint getDividerLinePaintTwo() {
        return dividerLinePaintTwo;
    }

    public Paint getHealthBarPaint() {
        return healthBarPaint;
    }
    //#endregion getters

    //#region public methods
    public void updateCloudPaintAlpha(int frameCounter){
        this.cloudPaint.setAlpha(Math.min(255-frameCounter,255));
    }
    //#endregion public methods

    //#region private methods
    /**
     * Sets Strokewith and color for paint object.
     * @param linePaint The paint to apply changes to.
     * @param strokeWidth The Strokewidth.
     * @param color Color
     */
    private void setLineOptions(Paint linePaint, int strokeWidth, int color) {
        linePaint.setStrokeWidth(strokeWidth);
        linePaint.setColor(color);
    }

    /**
     * Set options for color, style and font size
     * @param textPaint
     * @param color
     * @param fontSize
     */
    private void setTextOptions(Paint textPaint, int color, int fontSize) {
        textPaint.setColor(color);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(fontSize);
    }
    //#endregion private methods
}
