package de.schule.georhelm.schulprojekt.utilities;

import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.schule.georhelm.schulprojekt.R;
import de.schule.georhelm.schulprojekt.interfaces.ICommunicationResult;

/**
 * Used for user login and register process.
 */
public class ServerCommunicator extends AsyncTask<String,Void,String> {

    //#region private properties
    private String baseURL;
    private ICommunicationResult communicationResult;
    //#endregion private properties

    //#region constructor
    /**
     *Servercommunicator to manage Login and Register activity.
     * @param communicationResult where the communicationResult is sent to.
     */
    public ServerCommunicator(ICommunicationResult communicationResult, Context context){
        this.baseURL = context.getString(R.string.baseURL) + ":" + context.getString(R.string.port);
        this.communicationResult = communicationResult;
    }
    //#endregion constructor

    //#region private methods
    /**
     * Send JSON of username and password to url (Might be login or register)
     * @param URL Url to send to.
     * @param username Username given.
     * @param password  Password given.
     * @return Returns the response from the server.
     */
    private String postToURL(String URL, String username, String password){
        String requestBody = "{\"user\": \""+username+"\", \"password\":\""+password+"\"}";
        HttpURLConnection con;
        try{
            URL url = new URL(this.baseURL+URL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type",
                    "application/json");

            con.setRequestProperty("Content-Length",
                    Integer.toString(requestBody.length()));
            con.setRequestProperty("Content-Language", "en-US");
            con.getOutputStream().write(requestBody.getBytes("UTF8"));

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();

        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }
    //#endregion private methods

    //#region protected methods
    /**
     * Used to send data to server. Decides whether it´s a login or register activity.
     * @param data data[0] is either "register" or "login" , data[1] is Username, data[2] is password
     * @return Returns the URL from the server, which is returned by method postToURL
     */
    @Override
    protected String doInBackground(String... data) {
        if(data[0].equals("register")){
            return this.postToURL("/register",data[1],data[2]);
        }else if(data[0].equals("login")){
            return this.postToURL("/login",data[1],data[2]);
        }
        return null;

    }
    /**
     *Callback after recieving response from server.
     * Sends results to calling object.
     * @param result
     */
    @Override
    protected void onPostExecute(String result) {
        try{
            JSONObject responseObject = new JSONObject(result);
            this.communicationResult.onResult(responseObject);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    //#endregion protected methods
}








