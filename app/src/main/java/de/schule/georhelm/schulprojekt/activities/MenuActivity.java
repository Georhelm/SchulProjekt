package de.schule.georhelm.schulprojekt.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;

import de.schule.georhelm.schulprojekt.utilities.ConnectionSocket;
import de.schule.georhelm.schulprojekt.managers.SoundManager;
import de.schule.georhelm.schulprojekt.R;
import de.schule.georhelm.schulprojekt.utilities.SearchHandler;

public class MenuActivity extends AppCompatActivity {

    //#region properties
    private ConnectionSocket socket;
    private SearchHandler searchHandler;
    private boolean isSearching;
    //#endregion properties

    //#region protected methods
    /**
     * Initializes the socket additionally to the standard onCreate.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_menu);
        this.socket = ConnectionSocket.getSocket();
        this.isSearching = false;
        this.socket.getWinCount(this);
    }
    /**
     * Resumes and retrieves the wincount.
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.socket.getWinCount(this);
    }
    //#endregion protected methods

    //#region public methods
    /**
     * Resets the View back to inital state.
     */
    public void resetView(){
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
                activity.isSearching = false;
            }
        });
    }
    /**
     * Starts the GameActivity and resets this view.
     * @param startGameSetting A JSONObject with information about the game to be started. This is retrieved by the server.
     */
    public void startGame(JSONObject startGameSetting) {
        this.isSearching = false;
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("gamedata", startGameSetting.toString());
        this.startActivity(intent);
        this.resetView();
    }
    /**
     *Starts the equipment activity to choose equipment
     * @param view The view from which this method is called
     */
    public void showEquipment(View view) {
        Intent intent = new Intent(this, EquipmentActivity.class);
        this.startActivity(intent);
    }
    /**
     * Logs out and deletes the AccessToken. Returns to Login activity.
     * @param view The view this method is called from.
     */
    public void logOut(View view){
        socket.logOut();
        File appInternalDirectory = getFilesDir();
        File tokenSave = new File(appInternalDirectory + "/accessToken.txt");
        tokenSave.delete();
        this.finish();
    }
    /**
     * Sets the wins of player on the Textview.
     * @param wins An integer representing the amount of wins of the player.
     */
    public void setWins(final int wins) {
        final TextView winView = this.findViewById(R.id.menuWins);
        final String winString = String.format(this.getResources().getString(R.string.textWins),wins);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                winView.setText(winString);
            }
        });
    }
    /**
     * Disables all buttons but mute button and shows a searchtime count.
     */
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

                activity.isSearching= true;
            }
        });

    }
    /**
     * Toggles the sound between not muted and muted.
     * @param view The view this method is called from.
     */
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
    /**
     *Calls the Socket to start a singlePlayergame.
     * @param view The view this method is called from.
     */
    public void startSinglePlayerGame(View view){
        socket.startSingleplayerGame(this);
    }
    /**
     * Calls the Socket to start a multiPlayergame
     * @param view The view this method is called from.
     */
    public void startMultiPlayerGame(View view){
        if(this.isSearching){
            socket.cancelSearch(this);
        }else{
            socket.startMultiplayerGame(this);
        }

    }
    //#endregion public methods
}
