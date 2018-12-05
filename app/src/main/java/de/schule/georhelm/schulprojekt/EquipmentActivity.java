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

    //#region properties
    private ConnectionSocket socket;
    private List<MountStats> mountStats;
    private List<String> statList;
    private ArrayAdapter<String> statListAdapter;
    private int selectedMountId;
    //#endregion properties

    //#region protected methodds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_equipment);
        this.socket = ConnectionSocket.getSocket();
        this.socket.getAvailableEquipment(this);
        this.statList = new ArrayList<String>();
    }
    //#endregion protected methods

    //#region private methods
    /**
     * Draws the chosen equipment into the Imageview to have a preview of it.
     * @param selectedMount An integer representing the id of the chosen mount.
     */
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

    /**
     * Initializes adapters for the equipmentlist and the gridview.
     * @param equipmentList The ListView that contains the Equipment.
     * @param arrayAdapter The adapter that contains the Mountstats.
     * @param gridView The gridView that the adapter will connect to.
     * @param selectedMount The mount that is currently selected.
     */
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

    /**
     * Changes the imageview and statlist to contain the data of chosen equipment.
     * @param parent MountStats object containing the adapter.
     * @param position The position at which the adapter contains the chosen item.
     */
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
    //#endregion

    //#region public methods
    /**
     *Recycles every mount and goes back to the menu activity.
     * @param view The view this method is called from.
     */
    public void showMenu(View view) {
        for(MountStats mount:this.mountStats){
            mount.recycle();
        }
        this.finish();
    }
    /**
     * Fills the mountStats arraylist with all available mounts.
     * @param equipment A JSONObject containing all information about all equipment.
     */
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
    /**
     * Tells the socket to save the currently chosen equipment.
     * @param view The view this method is called from.
     */
    public void saveEquipmentSelection(View view){
        this.socket.saveEquipment(selectedMountId);
        Toast.makeText(this,R.string.textSaved,Toast.LENGTH_SHORT).show();
    }
    //#endregion public methods





}
