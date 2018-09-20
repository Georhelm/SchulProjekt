package de.schule.georhelm.schulprojekt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class OptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_options);
    }

    public void showMenu(View v) {
        this.finish();
    }
}
