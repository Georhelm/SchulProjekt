package de.schule.georhelm.schulprojekt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EquipmentActivity extends AppCompatActivity {
    private ConnectionSocket socket;
    private List<MountStats> mountStats;
    private List<String> statList;
    private ArrayAdapter<String> statListAdapter;
    private int selectedMountId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_equipment);
        this.socket = ConnectionSocket.getSocket();
        this.socket.getAvailableEquipment(this);
        this.statList = new ArrayList<String>();
    }

    public void showMenu(View v) {
        for(MountStats mount:this.mountStats){
            mount.recycle();
        }
        this.finish();
    }

    public void fillEquipment(JSONObject equipment){
        this.mountStats = new ArrayList<MountStats>();
        try {
            JSONArray mounts = equipment.getJSONArray("mounts");
            int selectedMount = equipment.getInt("selectedMount");
            for(int i = 0; i<mounts.length();i++){
                this.mountStats.add(new MountStats(mounts.getJSONObject(i),this));
            }
            this.drawEquipment(selectedMount);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void drawEquipment(final int selectedMount){
        final ListView equipmentList = this.findViewById(R.id.equipmentList);
        final ArrayAdapter<MountStats> arrayAdapter = new ArrayAdapter<MountStats>(this, android.R.layout.simple_list_item_1,this.mountStats);

        final GridView gridView = findViewById(R.id.equipmentStatsGridview);

        this.statListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,this.statList);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initAdapters(equipmentList, arrayAdapter, gridView,selectedMount);
            }
        }
        );
        equipmentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectEquipment(parent, position);
            }
        });
    }

    private void initAdapters(ListView equipmentList, ArrayAdapter<MountStats> arrayAdapter, GridView gridView,int selectedMount) {
        equipmentList.setAdapter(arrayAdapter);
        gridView.setAdapter(this.statListAdapter);
        for(int i = 0; i<=mountStats.size();i++){
            if(mountStats.get(i).getId()==selectedMount){
                selectEquipment(equipmentList,i);
                break;
            }
        }
    }


    private void selectEquipment(AdapterView<?> parent, int position) {
        MountStats mountStats = (MountStats)parent.getAdapter().getItem(position);
        ImageView imageView = this.findViewById(R.id.equipmentPreview);
        imageView.setImageBitmap(mountStats.getBitmap());
        this.statList.clear();
        this.statList.add("Acceleration: ");
        this.statList.add(mountStats.getAcceleration());
        this.statList.add("Max speed: ");
        this.statList.add(mountStats.getMaxSpeed());
        this.statList.add("Height: ");
        this.statList.add(mountStats.getHeight());

        this.statListAdapter.notifyDataSetChanged();
        this.selectedMountId = mountStats.getId();

    }

    public void saveEquipmentSelection(View view){
        this.socket.saveEquipment(selectedMountId);
        Toast.makeText(this,R.string.textSaved,Toast.LENGTH_SHORT).show();

    }


}
