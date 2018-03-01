package com.example.manuel.thingseedemo;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by manuel on 2/23/18.
 */

public class DataRecorder {
    //private static final int    MAXEVENTS = 10;
    //private static final int    REQUEST_DELAY = 2000;
    public static final int     DATA_UPDATED = 2;
    String username, password;
    ThingSee       thingsee; //ThingSee object from which to retrieve the data
    TrackData data; //DataTrack container (group of TimeStream) in which to store data
    boolean recording = false; //Is recording?
    int interval = 3000; //Fetch the data every (ms)
    private String lastResultState = "OK";

    /*private long startTimestamp = 0;
    private long realStartTimestamp = 0;
    private long curTimestamp = 0;*/

    private HandlerThread handlerThread = new HandlerThread("RecorderHandlerThread");
    private Handler handler, targetMsgHandler;
    private Runnable externalAction; //Execute this action every time the recorder tries to get data

    public DataRecorder(ThingSee thingSeeObject, TrackData dataContainer, long startTime, int recordingInterval) {
        username = null;
        password = null;
        thingsee = thingSeeObject;
        data = dataContainer;
        interval = recordingInterval;
        //startTimestamp = startTime;
        //realStartTimestamp = System.currentTimeMillis();
    }

    public DataRecorder(String user, String pass, TrackData dataContainer, long startTime, int recordingInterval) {
        username = user;
        password = pass;
        thingsee = null;
        data = dataContainer;
        interval = recordingInterval;
        Log.d("INFO", "Created recorder");
        //startTimestamp = startTime;
        //realStartTimestamp = System.currentTimeMillis();
    }

    public void start(Handler callbackHandler) {
        recording = true;
        targetMsgHandler = callbackHandler;
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.post(runnable);
    }

    public void stop() {
        handlerThread.quitSafely();
        recording = false;
    }

    public void setAction(Runnable action) {
        externalAction = action;
    }

    public TrackData getData(){
        return data;
    }

    public String getLastResultState(){
        return lastResultState;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            doInBackground();

            //if (externalAction != null)
                //externalAction.run();

            Message msg = targetMsgHandler.obtainMessage(DATA_UPDATED);
            msg.sendToTarget();

            Log.d("INFO", "Sent DATA_UPDATED to main thread");

            handler.postDelayed(this, interval);
        }
    };

    protected void doInBackground() {
        String result = "NOT OK";

        Log.d("INFO", "DataRecorder handle called");
        try {
            if (thingsee == null && (username == null || password == null)) {
                lastResultState = result;
                Log.d("INFO", "Login info missing");
                return; //I don't have a proper ThingSee object or any credential to build it
            }
            else if (thingsee == null) {
                Log.d("INFO", "ThingSee connection attempt");
                thingsee = new ThingSee(username, password);
                Log.d("INFO", "Connected");
            }

            //JSONArray events = thingsee.Events(thingsee.Devices(), MAXPOSITIONS);
            //System.out.println(events);
            //data.initFromThingSee(thingsee, curTimestamp - INSTANT_MARGIN, curTimestamp + INSTANT_MARGIN);
            data.recordMore(thingsee);
            //coordinates = thingsee.getPath(events);

//                for (Location coordinate: coordinates)
//                    System.out.println(coordinate);
            result = "OK";
        } catch(Exception e) {
            Log.d("NET", "Communication error: " + e.getMessage());
        }

        lastResultState = result;
    }

    /*
    protected void onPostExecute(String result) {
        // check that the background communication with the client was succesfull
        if (result.equals("OK")) {
            // now the coordinates variable has those coordinates
            // elements of these coordinates is the Location object who has
            // fields for longitude, latitude and time when the position was fixed

            curTimestamp = startTimestamp + (System.currentTimeMillis() - realStartTimestamp);


        } else {
            Log.d("Oops", result);
        }
    }
*/
    public TrackData.AllDataStructure getAllCurrentData(){
        return data.getAllAtTime(data.getCurrentTimestamp());
    }

}