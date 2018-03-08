package com.example.manuel.thingseedemo;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

/**
 * Created by manuel on 2/23/18.
 */


public class RealTimeRecorder {
    public static final int     DATA_UPDATED = 2;
    String username, password;
    ThingSee       thingsee; //ThingSee object from which to retrieve the data
    TrackData data; //DataTrack container (group of TimeStream) in which to store data
    boolean recording = false; //Is recording?
    int interval = 3000; //Fetch the data every (ms)
    private String lastResultState = "OK";

    private HandlerThread handlerThread = new HandlerThread("RecorderHandlerThread");
    private Handler handler, targetMsgHandler;

    public RealTimeRecorder(ThingSee thingSeeObject, int recordingInterval) {
        username = null;
        password = null;
        thingsee = thingSeeObject;
        data = new TrackData();
        interval = recordingInterval;
    }

    public RealTimeRecorder(String user, String pass, int recordingInterval) {
        username = user;
        password = pass;
        thingsee = null;
        data = new TrackData();
        interval = recordingInterval;
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

            Message msg = targetMsgHandler.obtainMessage(DATA_UPDATED);
            msg.sendToTarget();

            handler.postDelayed(this, interval);
        }
    };

    protected void doInBackground() {
        String result = "NOT OK";

        try {
            if (thingsee == null && (username == null || password == null)) {
                lastResultState = result;
                return; //I don't have a proper ThingSee object or any credential to build it
            }
            else if (thingsee == null) {
                thingsee = new ThingSee(username, password);
            }

            data.recordMore(thingsee);

            result = "OK";
        } catch(Exception e) {
            Log.d("NET", "Communication error: " + e.getMessage());
        }

        lastResultState = result;
    }

    public TrackData.AllDataStructure getAllLastData(){
        return data.getAllLast();
    }

}