package com.example.manuel.thingseedemo.fragments;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.manuel.thingseedemo.R;
import com.example.manuel.thingseedemo.RealTimeRecorder;
import com.example.manuel.thingseedemo.TrackData;
import com.example.manuel.thingseedemo.util.DataStorage;
import com.example.manuel.thingseedemo.util.TimestampDateHandler;

/**
 * Created by awetg on 15.2.2018.
 * A simple {@link Fragment} subclass.
 */

public class Logs extends Fragment {

    private static final int    REQUEST_DELAY = 2000;
    private static final String PREFERENCEID = "Credentials";

    //TrackData trackData = new TrackData();
    TrackData trackData;
    TrackData.AllDataStructure currentData = null;

    private String               username, password;
    private boolean isRealtime = false;

    private RealTimeRecorder realTimeRecorder;

    private View myView;
    private TextView tdate, tstatus;
    private SeekBar timeSeekBar;
    //private long startTimestamp = 0;
//    private long realStartTimestamp = 0;

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            if (msg.what == RealTimeRecorder.DATA_UPDATED)
                getCurrentData();
                showDataAtTime(System.currentTimeMillis());
                checkAndRefreshState();
        }
    };

    private void checkAndRefreshState(){
        if (DataStorage.isRealtime() && !isRealtime){
            realTimeRecorder = new RealTimeRecorder(username, password, 5000);
            realTimeRecorder.start(handler);
            isRealtime = true;
            timeSeekBar.setEnabled(false);
            tstatus.setText("Real time data");
        } else if (DataStorage.isRealtime()) {

        }
        else {
            isRealtime = false;
            if (realTimeRecorder != null){
                realTimeRecorder.stop();
                realTimeRecorder = null;
            }
            trackData = DataStorage.getTrackData();
            String trackName = DataStorage.getCachedFileName();
            if (DataStorage.currentlyRecording())
                tstatus.setText("Recording track " + trackName + "\n End the recording to go back in real time mode");
            else
                tstatus.setText("Showing data recorded in track " + trackName + "\n Stop the track to go back in real time mode");

            timeSeekBar.setEnabled(true);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.logs,container,false);

        SharedPreferences prefGet = getActivity().getSharedPreferences(PREFERENCEID, Activity.MODE_PRIVATE);
        username = prefGet.getString("username", "bbbmetropolia@gmail.com");
        password = prefGet.getString("password", "badbadboys0");


        /*((Button)myView.findViewById(R.id.getDataButton)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resetTrack();
                    }
                }
        );*/

        tdate = myView.findViewById(R.id.date);
        tstatus = myView.findViewById(R.id.status);
        timeSeekBar = myView.findViewById(R.id.mainSeekBar);
        timeSeekBar.setProgress(0);
        long startTimestamp = System.currentTimeMillis();
//        realStartTimestamp = startTimestamp;
        String beginningMessage  = "Waiting for connection...";
        tdate.setText(beginningMessage);

        checkAndRefreshState();

        showDataAtTime(progressToTimestamp());

        timeSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        showDataAtTime(progressToTimestamp());
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        return myView;
    }

    private long progressToTimestamp(){
        if (trackData == null || trackData.isEmpty() || isRealtime)
            return 0;

        long timeStart = trackData.getFirstTimestamp();
        long timeEnd = trackData.getCurrentTimestamp();
        double progStart = 0;
        double progEnd = timeSeekBar.getMax();
        double progress = timeSeekBar.getProgress();

        if (progEnd == progStart)
            return 0;

        long timeProgress = (long) ((timeEnd - timeStart) * progress / (progEnd - progStart));
        long timestamp = timeStart + timeProgress;

        /*Log.d("TIMESEEK", "First: " + timeStart);
        Log.d("TIMESEEK", "Last: " + timeEnd);

        Log.d("TIMESEEK", "Result: " + timestamp);*/

        return timestamp == 0 ? 1 : timestamp;
    }

    public void showDataAtTime(long timestamp){
        if (!isRealtime && (trackData == null || !trackData.isInitialized()))
            return;

        TrackData.AllDataStructure dataAtTime;

        if (isRealtime)
            dataAtTime = currentData;
        else
            dataAtTime = trackData.getAllAtTime(timestamp);

        if (dataAtTime == null)
            return;

        tdate.setText(TimestampDateHandler.timestampToDate(timestamp));

        //Log.d("INFO", "Temperature Data contains n. elements: " + trackData.getTemperatureStream().sampleCount());

        Double temperature, speed, impact, pressure, battery, altitude, latitude, longitude, distance;
        temperature = dataAtTime.getTemperature();
        speed = dataAtTime.getSpeed();
        impact = dataAtTime.getImpact();
        pressure = dataAtTime.getPressure();
        battery = dataAtTime.getBattery();
        altitude = dataAtTime.getAltitude();
        latitude = dataAtTime.getLatitude();
        longitude = dataAtTime.getLongitude();
        distance = dataAtTime.getDistance();


        if (temperature != null)
            ((TextView) myView.findViewById(R.id.temperature)).setText(Double.toString(temperature));
        else
            ((TextView) myView.findViewById(R.id.temperature)).setText("No data");

        if (speed != null)
            ((TextView) myView.findViewById(R.id.speed)).setText(Double.toString(speed));
        else
            ((TextView) myView.findViewById(R.id.speed)).setText("No data");

        if (impact != null)
            ((TextView) myView.findViewById(R.id.impact)).setText(Double.toString(impact));
        else
            ((TextView) myView.findViewById(R.id.impact)).setText("No data");

        if (pressure != null)
            ((TextView) myView.findViewById(R.id.pressure)).setText(Double.toString(pressure));
        else
            ((TextView) myView.findViewById(R.id.pressure)).setText("No data");

        if (battery != null)
            ((TextView) myView.findViewById(R.id.battery)).setText(Double.toString(battery));
        else
            ((TextView) myView.findViewById(R.id.battery)).setText("No data");

        if (altitude != null)
            ((TextView) myView.findViewById(R.id.altitude)).setText(Double.toString(altitude));
        else
            ((TextView) myView.findViewById(R.id.altitude)).setText("No data");

        if (latitude != null)
            ((TextView) myView.findViewById(R.id.latitude)).setText(Double.toString(latitude));
        else
            ((TextView) myView.findViewById(R.id.latitude)).setText("No data");

        if (longitude != null)
            ((TextView) myView.findViewById(R.id.longitude)).setText(Double.toString(longitude));
        else
            ((TextView) myView.findViewById(R.id.longitude)).setText("No data");

        if (distance != null)
            ((TextView) myView.findViewById(R.id.distance)).setText(Double.toString(distance));
        else
            ((TextView) myView.findViewById(R.id.distance)).setText("No data");

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // no need to check if empty, they have already been checked on main activity
        SharedPreferences prefGet = getActivity().getSharedPreferences(PREFERENCEID, Activity.MODE_PRIVATE);
        username = prefGet.getString("username", "bbbmetropolia@gmail.com");
        password = prefGet.getString("password", "badbadboys0");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realTimeRecorder != null)
            realTimeRecorder.stop();
    }

    public void getCurrentData() {

        //Log.d("INFO", "Executing post-fetching action");

        if (realTimeRecorder == null || realTimeRecorder.getLastResultState() != "OK")
            return;

        currentData = realTimeRecorder.getData().getAllAtTime(System.currentTimeMillis());

    }

}
