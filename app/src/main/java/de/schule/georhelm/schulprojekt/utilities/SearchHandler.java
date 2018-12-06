package de.schule.georhelm.schulprojekt.utilities;

import android.content.Context;
import android.widget.TextView;
import android.os.Handler;

import de.schule.georhelm.schulprojekt.R;


public class SearchHandler implements Runnable {

    //#region properties
    private int seconds;
    private boolean running;
    private TextView field;
    private Context context;
    private Handler handler;
    private final int delay = 1000;
    //#endregion properties

    //#region constructor
    /**
     * Class for handling the time while waiting for multiplayer game to start.
     * @param context Context which this class is called from.
     * @param field Field in which the number of counting seconds is displayed.
     */
    public SearchHandler(Context context, TextView field) {
        this.seconds = 0;
        this.running = true;
        this.field = field;
        this.context = context;
        this.handler = new Handler();
        this.handler.postDelayed(this, 0);
    }
    //#endregion constructor

    //#region public methods
    /**
     * Counts seconds from 0 upwards until the method end() is called.
     * Has a delay to prevent executing too many times.
     */
    @Override
    public void run() {
        seconds++;
        String searching = String.format(this.context.getString(R.string.textSearching),seconds);
        this.field.setText(searching);
        if(this.running) {
            handler.postDelayed(this, delay);
        }else {
            this.field.setText("");
        }
    }
    /**
     * Sets running property of this Handler to false, so the run() loop stops.
     */
    public void end() {
        this.running = false;
    }
    //#endregion public methods
}
