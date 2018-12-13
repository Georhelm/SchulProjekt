package de.schule.georhelm.schulprojekt.activities;

import android.content.Intent;
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

import de.schule.georhelm.schulprojekt.utilities.ConnectionSocket;
import de.schule.georhelm.schulprojekt.interfaces.ICommunicationResult;
import de.schule.georhelm.schulprojekt.managers.SoundManager;
import de.schule.georhelm.schulprojekt.R;
import de.schule.georhelm.schulprojekt.utilities.ServerCommunicator;

public class LoginActivity extends AppCompatActivity implements ICommunicationResult {

    //#region protected methods
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
    //#endregion protected methods

    //#region public methods
    /**
     * Tries to log in with the username and password inserted inteo the textfields.
     * @param view The view this method is called from.
     */
    public void tryLogin(View view){
        EditText userField = this.findViewById(R.id.inputUser);
        String user = userField.getText().toString();
        EditText passwordField = this.findViewById(R.id.inputPassword);
        String password = passwordField.getText().toString();
        passwordField.setText("");
        ServerCommunicator communicator = new ServerCommunicator(this, this);
        communicator.execute("login",user,password);
    }

    /**
     * Gets the result of trying to log in and saves a token on success and calls to create a socketconnection.
     * @param result
     */
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

    /**
     *Creates a connection to socket by using the token.
     * Goes to Menu activity on success.
     * @param token A String containing the token to log into the socket connection
     */
    private void createConnection(String token){
        ConnectionSocket socket = new ConnectionSocket(token);
        ConnectionSocket.setSocket(socket);
        if (socket.init(this)){
            Intent intent = new Intent(this, MenuActivity.class);
            this.startActivity(intent);
        }
    }

    /**
     *Opens the register activity.
     * @param view The view this method is called from.
     */
    public void openRegisterView(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        this.startActivity(intent);
    }

    /**
     * Saves the connection token on app-internal storage to remember log-in sessions.
     * @param token The token that needs to be saved.
     */
    private void saveToken(String token) {
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
    //#endregion public methods

}
