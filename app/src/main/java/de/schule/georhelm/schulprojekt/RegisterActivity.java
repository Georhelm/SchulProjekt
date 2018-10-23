package de.schule.georhelm.schulprojekt;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity implements ICommunicationResult {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

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

    public void onResult(JSONObject result) {
        try {
            String response = result.getString("result");

            if(response.equals("success")){
                Toast.makeText(this,"Register Successful!", Toast.LENGTH_LONG).show();
                this.finish();
            }else if(response.equals("error")&&result.getString("msg").equals("user_exists")){
                Toast.makeText(this,"User already exists!", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this,"Wtf", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelRegister(View view){
        this.finish();
    }
}
