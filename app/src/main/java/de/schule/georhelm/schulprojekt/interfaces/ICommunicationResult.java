package de.schule.georhelm.schulprojekt.interfaces;

import org.json.JSONObject;

/**
 * Used to import into different classes, to enable them to recieve responses from server.
 */
public interface ICommunicationResult {
    void onResult(JSONObject result);
}
