package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.widget.TextView;
import android.os.Handler;


public class SearchHandler implements Runnable {

    private int seconds;
    private boolean running;
    private TextView field;
    private Context context;
    private Handler handler;
    private final int delay = 1000;

    public SearchHandler(Context context, TextView field) {
        this.seconds = 0;
        this.running = true;
        this.field = field;
        this.context = context;
        this.handler = new Handler();
        this.handler.postDelayed(this, 0);
    }
    @Override
    public void run() {
        seconds++;
        String searching = this.context.getString(R.string.textSearching);
        this.field.setText( searching + " " + seconds + "s");
        if(this.running) {
            handler.postDelayed(this, delay);
        }else {
            this.field.setText("");
        }
    }

    public void end() {
        this.running = false;
    }
}
