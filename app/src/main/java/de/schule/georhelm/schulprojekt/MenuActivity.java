package de.schule.georhelm.schulprojekt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_menu);
    }

    public void startSingleplayer(View v) {
        Intent intent = new Intent(this, GameActivity.class);
        this.startActivity(intent);
    }

    public void startMultiplayer(View v){

    }

    public void showOptions(View v) {
        Intent intent = new Intent(this, OptionsActivity.class);
        this.startActivity(intent);
    }

    public void showEquipment(View v) {
        Intent intent = new Intent(this, EquipmentActivity.class);
        this.startActivity(intent);
    }
}