package com.example.manuel.thingseedemo;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by awetg on 7.3.2018.
 */

public class TrackService extends Service {

    final String KEY_INTERVAL = "key";
    final String KEY_USERNAME = "usr";
    final String KEY_PASSWORD = "pass";
    private String               username, password;
    int interval;

    TrackData trackData;
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

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.post(runnable);

        Log.d("service","started");

       return START_STICKY;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String result = "NOT OK";

            try {
                thingsee = new ThingSee(username, password);
                trackData.recordMore(thingsee);
                result = "OK";

            } catch (Exception e) {
                e.printStackTrace();
            }

            lastResultState = result;
            if(lastResultState.equals("OK"))
                saveData();

            handler.postDelayed(this, interval);
        }
    };

    private void saveData() {

        TrackData.AllDataStructure currentData = trackData.getAllLast();
        Double temperature, speed, impact, pressure;
        temperature = currentData.getTemperature();
        speed = currentData.getImpact();
        impact = currentData.getDistance();
        pressure = currentData.getBattery();
        Log.d("temprature",temperature.toString());

    }

}
