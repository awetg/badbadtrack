package com.example.manuel.thingseedemo;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.manuel.thingseedemo.util.DataStorage;

/**
 * Created by awetg on 7.3.2018.
 */

public class TrackService extends Service {

    final String KEY_INTERVAL = "KEY";
    final String KEY_USERNAME = "USR";
    final String KEY_PASSWORD = "PASS";
    static final String LAST_TRACK = "LAST";

    private String               username, password, trackName;
    int interval;

    TrackData trackData = new TrackData();
    ThingSee thingsee;
    private String lastResultState = "OK";



    private HandlerThread handlerThread = new HandlerThread("RecorderHandlerThread");
    private Handler handler;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        interval = intent.getIntExtra(KEY_INTERVAL,10000);
        username = intent.getStringExtra(KEY_USERNAME);
        password = intent.getStringExtra(KEY_PASSWORD);
        trackName = intent.getStringExtra(LAST_TRACK);

        trackData.start(10000);

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.post(runnable);

        Log.d("service","started");

       return START_REDELIVER_INTENT;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String result = "NOT OK";

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

                if(thingsee!=null)
                trackData.recordMore(thingsee);

                result = "OK";

            } catch (Exception e) {
                e.printStackTrace();
            }

            lastResultState = result;

            handler.postDelayed(this, interval);
        }
    };



    @Override
    public void onDestroy() {
        Log.d("SERVICE CLASS: " ,"TRACK DATA IS NULL: " + (trackName==null));
        DataStorage.storeData(trackData,trackName);
        handlerThread.quitSafely();
        super.onDestroy();
    }
}
