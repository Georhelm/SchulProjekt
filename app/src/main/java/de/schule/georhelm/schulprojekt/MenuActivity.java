package de.schule.georhelm.schulprojekt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.json.JSONObject;

import java.io.File;

public class MenuActivity extends AppCompatActivity {

    ConnectionSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_menu);
        this.socket = ConnectionSocket.getSocket();
    }

    public void startSingleplayer(JSONObject startGameSetting) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("gamedata", startGameSetting.toString());
        this.startActivity(intent);
    }

    public void startMultiplayer(View v){
        Intent intent = new Intent(this, GameActivity.class);
        this.startActivity(intent);
    }

    public void showOptions(View v) {
        Intent intent = new Intent(this, OptionsActivity.class);
        this.startActivity(intent);
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

    public void startSinglePlayerGame(View v){
        socket.startSingleplayerGame(this);
    }
}
