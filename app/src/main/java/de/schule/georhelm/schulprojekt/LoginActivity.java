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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LoginActivity extends AppCompatActivity implements ICommunicationResult {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SoundManager.startThemeMusic(this);

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
        passwordField.setText("");
        ServerCommunicator communicator = new ServerCommunicator(this);
        communicator.execute("login",user,password);
    }



    public void onResult(JSONObject result){

        boolean success;
        try{
            success = result.getString("result").equals("success");
            if(success){
                String token = result.getString("token");
                saveToken(token);
                createConnection(token);
            }else{
                Toast.makeText(this, "Wrong username or password", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){

        }
    }

    public void createConnection(String token){
        ConnectionSocket socket = new ConnectionSocket(token);
        ConnectionSocket.setSocket(socket);
        if (socket.init(this)){
            Intent intent = new Intent(this, MenuActivity.class);
            this.startActivity(intent);
        }
    }

    public void openRegisterView(View v){
        Intent intent = new Intent(this, RegisterActivity.class);
        this.startActivity(intent);
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
