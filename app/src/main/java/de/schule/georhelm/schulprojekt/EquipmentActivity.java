package de.schule.georhelm.schulprojekt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EquipmentActivity extends AppCompatActivity {
    private ConnectionSocket socket;
    private List<MountStats> mountStats;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_equipment);
        this.socket = ConnectionSocket.getSocket();
        this.socket.getAvailableEquipment(this);
    }

    public void showMenu(View v) {
        this.finish();
    }

    public void fillEquipment(JSONObject equipment){
        this.mountStats = new ArrayList<MountStats>();
        try {
            JSONArray mounts = equipment.getJSONArray("mounts");
            for(int i = 0; i<mounts.length();i++){
                this.mountStats.add(new MountStats(mounts.getJSONObject(i),this));
            }
            this.drawEquipment();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void drawEquipment(){
        final ListView equipmentList = this.findViewById(R.id.equipmentList);
        final ArrayAdapter<MountStats> arrayAdapter = new ArrayAdapter<MountStats>(this, android.R.layout.simple_list_item_1,this.mountStats);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                equipmentList.setAdapter(arrayAdapter);
            }
        });
    }


}
