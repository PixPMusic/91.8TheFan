package com.thefan.android;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class metaFetch {

    public static final String jsonURL = "http://iponyradio.com/thefan/json.php";
    private static String title;
    private static String artist;

    // contacts JSONArray
    JSONArray stations = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> stationList;

    public metaFetch() {

    }

    public void update() {
        // Calling async task to get json
        new GetContacts().execute();
    }

    private static void updateLocal (String tempTitle, String tempArtist) {
        if (!tempArtist.equals(artist)) {
            artist = tempArtist;
        }
        if (!tempTitle.equals(title)) {
            title = tempTitle;
        }
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetContacts extends AsyncTask<Void, Void, Void> {
        String tempArtist;
        String tempTitle;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(jsonURL, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    tempArtist = jsonObj.getString("artist");
                    tempTitle = jsonObj.getString("title");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            updateLocal(tempTitle, tempArtist);
        }

    }

    public static String getArtist() {
        return artist;
    }

    public static String getTitle() {
        return title;
    }
}
