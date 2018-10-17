package de.schule.georhelm.schulprojekt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.prefs = this.getSharedPreferences("users", MODE_PRIVATE);

        SharedPreferences.Editor editor = this.prefs.edit();

        editor.putString("username", "");
        editor.putString("password", "");
        editor.apply();


        final MediaPlayer themeMusicIntroMP = MediaPlayer.create(this, R.raw.medievalsongintro);
        final MediaPlayer themeMusicLoopMP = MediaPlayer.create(this, R.raw.medievalsongloop);
        themeMusicLoopMP.setLooping(true);
        themeMusicIntroMP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                themeMusicLoopMP.start();
                themeMusicIntroMP.stop();
            }
        });

        themeMusicIntroMP.start();

    }

    public void tryLogin(View v){
        EditText userField = this.findViewById(R.id.inputUser);
        String user = userField.getText().toString();
        EditText passwordField = this.findViewById(R.id.inputPassword);
        String password = passwordField.getText().toString();
        ServerCommunicator communicator = new ServerCommunicator(this);
        user = "testboiIII";
        password = "yeahMannen";
        communicator.execute("login",user,password);
    }

    public void tryRegister(){
        ServerCommunicator communicator = new ServerCommunicator(this);

        EditText userField = this.findViewById(R.id.inputUser);
        String user = userField.getText().toString();

        EditText passwordField = this.findViewById(R.id.inputPassword);
        String password = passwordField.getText().toString();
        user = "testboiIII";
        password = "yeahMannen";
        communicator.execute("register",user,password);
    }

    public void loginResult(JSONObject result){

    }
}
