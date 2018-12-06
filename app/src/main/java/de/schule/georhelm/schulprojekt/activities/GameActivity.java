package de.schule.georhelm.schulprojekt.activities;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import de.schule.georhelm.schulprojekt.utilities.ConnectionSocket;
import de.schule.georhelm.schulprojekt.views.GameView;
import de.schule.georhelm.schulprojekt.playerobjects.Lance;
import de.schule.georhelm.schulprojekt.playerobjects.Mount;
import de.schule.georhelm.schulprojekt.R;

public class GameActivity  extends AppCompatActivity {

    //#region properties
    private GameView gameView;
    //#endregion properties

    //#region protected methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);
        JSONObject gameData = new JSONObject();
        //Display display = getWindowManager().getDefaultDisplay();
        Bundle bundle = this.getIntent().getExtras();
        try {
            gameData = new JSONObject(bundle.getString("gamedata"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try{
            loadMounts();
            loadLances();
        }catch(Exception e){
            e.printStackTrace();
        }
        gameView = new GameView(this, gameData);

        setContentView(gameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
    //#endregion protected methods

    //#region private methods
    /**
     * Loads all mounts from xml file.
     * @throws XmlPullParserException Throws because loaded from xml.
     * @throws IOException Throws because loaded from xml.
     */
    private void loadMounts() throws XmlPullParserException, IOException {
        loadEquipmentFromXML("Mount", R.xml.mounts);
    }
    /**
     * Loads all weapons from xml file.
     * @throws XmlPullParserException Throws because loaded from xml.
     * @throws IOException Throws because loaded from xml.
     */
    private void loadLances() throws XmlPullParserException, IOException {
        loadEquipmentFromXML("Lance", R.xml.lances);
    }
    /**
     * Loads equipment from xml depending on given type and resource.
     * @param type String representing the type of equipment to be loaded. Either "mount" or "lance".
     * @param xmlResourceId Resource tyoe what should be loaded. E.g. "R.xml.lances".
     * @throws XmlPullParserException Throws because loaded from xml.
     * @throws IOException Throws because loaded from xml.
     */
    private void loadEquipmentFromXML(String type, int xmlResourceId) throws XmlPullParserException, IOException {
        Integer id = -1;
        String name = "";
        Integer x = -1;
        Integer y = -1;
        Integer width = -1;
        Integer height= -1;
        Resources resources = this.getResources();
        XmlResourceParser xmlResourceParser = resources.getXml(xmlResourceId);
        int eventType = xmlResourceParser.getEventType();
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            if(eventType == XmlResourceParser.START_TAG){
                switch (xmlResourceParser.getName()){
                    case "Id":
                        id = xmlResourceParser.getAttributeIntValue(null,"value", -1);
                        break;
                    case "X":
                        x = xmlResourceParser.getAttributeIntValue(null,"value", -1);
                        break;
                    case "Y":
                        y = xmlResourceParser.getAttributeIntValue(null,"value", -1);
                        break;
                    case "Width":
                        width = xmlResourceParser.getAttributeIntValue(null,"value", -1);
                        break;
                    case "Height":
                        height = xmlResourceParser.getAttributeIntValue(null,"value", -1);
                        break;
                    case "Name":
                        name = xmlResourceParser.getAttributeValue(null,"value");
                        break;
                }
            }else if(eventType == XmlResourceParser.END_TAG && xmlResourceParser.getName().equals(type)){
                if(type.equals("Mount")){
                    new Mount(this,id,x,y,width,height,name);
                }else if(type.equals("Lance")){
                    new Lance(this,id,x,y,width,height,name);
                }

            }
            eventType = xmlResourceParser.next();
        }
    }
    //#endregion private methods

    //#region public methods
    /**
     * Overrides the press of back button to trigger socket.leaveGame() before finishing the activity.
     */
    @Override
    public void onBackPressed() {
        ConnectionSocket.getSocket().leaveGame();
        this.finish();
    }
    //#endregion public methods



}
