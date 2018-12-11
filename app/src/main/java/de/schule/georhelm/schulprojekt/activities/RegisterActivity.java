package de.schule.georhelm.schulprojekt.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import de.schule.georhelm.schulprojekt.interfaces.ICommunicationResult;
import de.schule.georhelm.schulprojekt.R;
import de.schule.georhelm.schulprojekt.utilities.ServerCommunicator;

public class RegisterActivity extends AppCompatActivity implements ICommunicationResult {

    //#region public methods
    /**
     * Tries to register with the given username, password and password check.
     * Sends Toast messages to user to inform about success or error.
     * @param view The register view which should call this method.
     */
    public void tryRegister(View view){
        ServerCommunicator communicator = new ServerCommunicator(this);

        EditText userField = this.findViewById(R.id.inputRegisterUser);
        String user = userField.getText().toString();

        EditText passwordField = this.findViewById(R.id.inputRegisterPassword);
        String password = passwordField.getText().toString();
        EditText passwordCheckField = this.findViewById(R.id.inputRegisterPasswordCheck);
        String passwordCheck = passwordCheckField.getText().toString();

        if(!password.equals(passwordCheck)){
            Toast.makeText(this, "Passwords didn´t match!", Toast.LENGTH_SHORT).show();
            return;
        }
        communicator.execute("register",user,password);
    }
    /**
     * Handles the response of the server.
     * @param result Result from server to identify success or error.
     */
    public void onResult(JSONObject result) {
        try {
            String response = result.getString("result");

            if(response.equals("success")){
                Toast.makeText(this,"Register Successful!", Toast.LENGTH_LONG).show();
                this.finish();
            }else if(response.equals("error")&&result.getString("msg").equals("user_exists")) {
                Toast.makeText(this, "User already exists!", Toast.LENGTH_LONG).show();
            }else if(response.equals("error" ) && result.getString("msg").equals("password_short")){
                Toast.makeText(this, "Password to short!", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this,"Message send by server was invalid.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Closes the register activity and goes back to the login activity
     * @param view
     */
    public void cancelRegister(View view){
        this.finish();
    }
    //#endregion public methods

    //#region protected methods
    /**
     * default create of RegisterActivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }
    //#endregion protected methods
}
