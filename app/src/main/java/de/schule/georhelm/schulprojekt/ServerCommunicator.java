package de.schule.georhelm.schulprojekt;

import android.arch.core.util.Function;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerCommunicator extends AsyncTask<String,Void,String> {

    private String baseURL;

    private ICommunicationResult communicationResult;

    public ServerCommunicator(ICommunicationResult communicationResult){
        this.baseURL = "http://siffers.de:1234"; //Get out of config later
        this.communicationResult = communicationResult;
    }

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

    @Override
    protected String doInBackground(String... data) {
        if(data[0].equals("register")){
            return this.postToURL("/register",data[1],data[2]);
        }else if(data[0].equals("login")){
            return this.postToURL("/login",data[1],data[2]);
        }
        return null;

    }

    @Override
    protected void onPostExecute(String result) {
        try{
            JSONObject responseObject = new JSONObject(result);
            this.communicationResult.onResult(responseObject);
        }catch(Exception e){
            e.printStackTrace();
        }
        }
    }

