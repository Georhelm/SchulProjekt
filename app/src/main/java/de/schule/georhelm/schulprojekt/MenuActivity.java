package de.schule.georhelm.schulprojekt;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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
    private boolean isSearching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_menu);
        this.socket = ConnectionSocket.getSocket();
        this.isSearching = false;
    }

    public void startGame(JSONObject startGameSetting) {
        this.isSearching = false;
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("gamedata", startGameSetting.toString());
        this.startActivity(intent);

        this.resetView();
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
                //btnMultiplayer.setEnabled(false);

                btnMultiplayer.setText(R.string.textCancel);

                Button btnSinglePlayer = findViewById(R.id.buttonSingleplayer);
                btnSinglePlayer.setEnabled(false);

                final TextView text = findViewById(R.id.menuText);
                searchHandler = new SearchHandler(activity, text);

            }
        });

    }

    public void toggleSound(View view){
        final MenuActivity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                boolean muted = SoundManager.toggleMusic(activity);
                Button soundButton = findViewById(R.id.buttonToggleSound);
                Drawable buttonBackground;
                if(muted){
                    buttonBackground = activity.getDrawable(R.drawable.button_unmute_red);
                }else{
                    buttonBackground = activity.getDrawable(R.drawable.button_mute_red);
                }
                soundButton.setBackground(buttonBackground);
            }
        });

    }

    public void startSinglePlayerGame(View v){
        socket.startSingleplayerGame(this);
    }

    public void startMultiPlayerGame(View v){
        if(this.isSearching){
            socket.cancelSearch();
            this.isSearching = false;
            this.resetView();
        }else{
            this.isSearching=true;
            socket.startMultiplayerGame(this);
        }

    }

    private void resetView(){
        final MenuActivity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button btnEquipment = findViewById(R.id.buttonEquipment);
                btnEquipment.setEnabled(true);

                Button btnLogout = findViewById(R.id.buttonLogout);
                btnLogout.setEnabled(true);

                Button btnMultiplayer = findViewById(R.id.buttonMultiplayer);
                btnMultiplayer.setText(R.string.textMultiplayer);

                Button btnSinglePlayer = findViewById(R.id.buttonSingleplayer);
                btnSinglePlayer.setEnabled(true);

                if(activity.searchHandler != null) {
                    activity.searchHandler.end();
                }
            }
        });
    }
}
