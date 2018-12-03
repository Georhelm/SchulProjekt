package de.schule.georhelm.schulprojekt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;

public class MenuActivity extends AppCompatActivity {

    private ConnectionSocket socket;
    private SearchHandler searchHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_menu);
        this.socket = ConnectionSocket.getSocket();
    }

    public void startGame(JSONObject startGameSetting) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("gamedata", startGameSetting.toString());
        this.startActivity(intent);
        if(this.searchHandler != null) {
            this.searchHandler.end();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button btnEquipment = findViewById(R.id.buttonEquipment);
                btnEquipment.setEnabled(true);

                Button btnLogout = findViewById(R.id.buttonLogout);
                btnLogout.setEnabled(true);

                Button btnMultiplayer = findViewById(R.id.buttonMultiplayer);
                btnMultiplayer.setEnabled(true);

                Button btnSinglePlayer = findViewById(R.id.buttonSingleplayer);
                btnSinglePlayer.setEnabled(true);
            }
        });
    }

    public void showEquipment(View v) {
        Intent intent = new Intent(this, EquipmentActivity.class);
        this.startActivity(intent);
    }

    public void logOut(View v){
        socket.logOut();
        File appInternalDirectory = getFilesDir();
        File tokenSave = new File(appInternalDirectory + "/accessToken.txt");
        tokenSave.delete();
        this.finish();
    }

    public void showSearchingGame() {
        final MenuActivity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button btnEquipment = findViewById(R.id.buttonEquipment);
                btnEquipment.setEnabled(false);

                Button btnLogout = findViewById(R.id.buttonLogout);
                btnLogout.setEnabled(false);

                Button btnMultiplayer = findViewById(R.id.buttonMultiplayer);
                btnMultiplayer.setEnabled(false);

                Button btnSinglePlayer = findViewById(R.id.buttonSingleplayer);
                btnSinglePlayer.setEnabled(false);

                final TextView text = findViewById(R.id.menuText);
                searchHandler = new SearchHandler(activity, text);

            }
        });

    }

    public void startSinglePlayerGame(View v){
        socket.startSingleplayerGame(this);
    }

    public void startMultiPlayerGame(View v){
        socket.startMultiplayerGame(this);
    }
}
