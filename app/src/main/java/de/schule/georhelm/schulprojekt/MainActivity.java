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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

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

        File appInternalDirectory = getFilesDir();
        File tokenSave = new File(appInternalDirectory + "/accessToken.txt");
        if(tokenSave.exists()){
            String token = "";
            try {
                FileReader fileReader = new FileReader(tokenSave);

                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while((line = bufferedReader.readLine()) != null) {
                    token=line;
                }
                bufferedReader.close();
                fileReader.close();
                createConnection(token);
            }
            catch(Exception e) {
            }
        }

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

    public void registerSuccessfull(JSONObject result){
        try{
            System.out.println(result.getString("result"));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void loginResult(JSONObject result){

        boolean success = false;
        try{
            success = result.getString("result").equals("success");
            if(success){
                String token = result.getString("token");
                saveToken(token);
                createConnection(token);
            }else{

            }
        }catch(Exception e){

        }
    }

    public void createConnection(String token){
        ConnectionSocket socket = new ConnectionSocket(token);
        SocketManager.setSocket(socket);
        if (socket.init()){
            Intent intent = new Intent(this, MenuActivity.class);
            this.startActivity(intent);
        }
    }

    public void saveToken(String token) {
        File appInternalDirectory = getFilesDir();
        File tokenSave = new File(appInternalDirectory + "/accessToken.txt");
        try{
            tokenSave.createNewFile();
            FileWriter fileWriter = new FileWriter( tokenSave);
            PrintWriter printWriter = new PrintWriter( fileWriter );
            printWriter.printf(token);
            printWriter.close();
            fileWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
