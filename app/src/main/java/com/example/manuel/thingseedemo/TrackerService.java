package com.example.manuel.thingseedemo;


import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by awetg on 20.2.2018.
 */

public class TrackerService extends Service {

    private static final int    MAXPOSITIONS = 20;
//    private static final String PREFERENCEID = "Credentials";

//    private String               username, password;
    private String[]             positions = new String[MAXPOSITIONS];


    final String keyIntent = "start";
    long startTimeStamp;

    long defaultValue = -1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startTimeStamp = intent.getLongExtra(keyIntent,defaultValue);

        return START_STICKY;
    }


    private class TalkToThingsee extends AsyncTask<String, Integer, String> {
        ThingSee       thingsee;
        List<Location> coordinates = new ArrayList<Location>();

        @Override
        protected String doInBackground(String... params) {
            String result = "NOT OK";

            // here we make the request to the cloud server for MAXPOSITION number of coordinates
            try {
                thingsee = new ThingSee("bbbmetropolia@gmail.com", "badbadboys0");

                JSONArray events = thingsee.Events(thingsee.Devices(), MAXPOSITIONS);
                //System.out.println(events);
                coordinates = thingsee.getPath(events);

//                for (Location coordinate: coordinates)
//                    System.out.println(coordinate);
                result = "OK";
            } catch(Exception e) {
                Log.d("NET", "Communication error: " + e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // check that the background communication with the client was succesfull
            if (result.equals("OK")) {
                // now the coordinates variable has those coordinates
                // elements of these coordinates is the Location object who has
                // fields for longitude, latitude and time when the position was fixed
                for (int i = 0; i < coordinates.size(); i++) {
                    Location loc = coordinates.get(i);

                    positions[i] = (new Date(loc.getTime())) +
                            " (" + loc.getLatitude() + "," +
                            loc.getLongitude() + ")"; //coordinates.get(i).toString();
                    Log.d("cord ",positions[i]);
                }
            } else {
                // no, tell that to the user and ask a new username/password pair
                positions[0] = getResources().getString(R.string.no_connection);
//                queryDialog(TrackerService.this, getResources().getString(R.string.info_prompt));
            }
//            myAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            // first clear the previous entries (if they exist)
            for (int i = 0; i < positions.length; i++)
                positions[i] = "";
//            myAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {}
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
