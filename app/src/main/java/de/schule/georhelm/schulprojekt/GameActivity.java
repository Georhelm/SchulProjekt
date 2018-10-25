package de.schule.georhelm.schulprojekt;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;

import org.json.JSONException;
import org.json.JSONObject;

public class GameActivity  extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);
        JSONObject gameData = new JSONObject();
        //Display display = getWindowManager().getDefaultDisplay();
        Bundle bundle = this.getIntent().getExtras();
        try {
            gameData = new JSONObject(bundle.getString("gamedata"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        loadMounts();
        loadLances();
        gameView = new GameView(this, gameData);

        setContentView(gameView);
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameView.resume();
    }

    private void loadMounts(){
        new Mount(this,1);
    }

    private void loadLances(){
        new Lance(this,1);
    }


}
