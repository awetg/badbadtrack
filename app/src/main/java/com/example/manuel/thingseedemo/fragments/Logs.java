package com.example.manuel.thingseedemo.fragments;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.manuel.thingseedemo.R;
import com.example.manuel.thingseedemo.TrackData;
import com.example.manuel.thingseedemo.DataRecorder;
import com.example.manuel.thingseedemo.util.TimestampDateHandler;

import java.sql.Time;
import java.text.SimpleDateFormat;

/**
 * Created by awetg on 15.2.2018.
 * A simple {@link Fragment} subclass.
 */

public class Logs extends Fragment {

    private static final int    REQUEST_DELAY = 2000;
    private static final String PREFERENCEID = "Credentials";

    TrackData trackData = new TrackData();
    TrackData.AllDataStructure currentData = null;

    DataRecorder recorder;

    private String               username, password;


    private View myView;
    private EditText tdate;
    private SeekBar timeSeekBar;
    private long startTimestamp = 0;
//    private long realStartTimestamp = 0;

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            if (msg.what == DataRecorder.DATA_UPDATED)
                //getCurrentData();
                Log.d("INFO", "Message received from Logs");
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.logs,container,false);



        ((Button)myView.findViewById(R.id.getDataButton)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resetTrack();
                    }
                }
        );

        tdate = myView.findViewById(R.id.date);
        timeSeekBar = myView.findViewById(R.id.mainSeekBar);
        timeSeekBar.setProgress(0);
        startTimestamp = System.currentTimeMillis();
//        realStartTimestamp = startTimestamp;
        String dateString  = TimestampDateHandler.timestampToDate(startTimestamp);
        tdate.setText(dateString);


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
        //handler.postDelayed(runnable, 100);

        /*tdate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy, h:mm a");
                try {
                   curTimestamp = sdf.parse(editable.toString()).getTime();
                   getCurrentData(myView);
                } catch (Exception ex){}
            }
        });*/

        return myView;
    }

    private long progressToTimestamp(){
        long timeStart = trackData.getFirstTimestamp();
        long timeEnd = trackData.getCurrentTimestamp();
        double progStart = 0;
        double progEnd = timeSeekBar.getMax();
        double progress = timeSeekBar.getProgress();

        if (progEnd == progStart)
            return 0;

        long timeProgress = (long) ((timeEnd - timeStart) * progress / (progEnd - progStart));
        long timestamp = timeStart + timeProgress;

        Log.d("TIMESEEK", "First: " + timeStart);
        Log.d("TIMESEEK", "Last: " + timeEnd);

        Log.d("TIMESEEK", "Result: " + timestamp);

        return timestamp == 0 ? 1 : timestamp;
    }

    public void showDataAtTime(long timestamp){
        if (!trackData.isInitialized())
            return;

        currentData = trackData.getAllAtTime(timestamp);

        tdate.setText(TimestampDateHandler.timestampToDate(timestamp));

        Log.d("INFO", "Temperature Data contains n. elements: " + trackData.getTemperatureStream().sampleCount());

        Double temperature, speed, impact, pressure;
        temperature = currentData.getTemperature();
        speed = currentData.getImpact();
        impact = currentData.getDistance();
        pressure = currentData.getBattery();


        if (temperature != null)
            ((TextView) myView.findViewById(R.id.temperature)).setText(Double.toString(temperature));
        else
            ((TextView) myView.findViewById(R.id.temperature)).setText("No data");

        if (speed != null)
            ((TextView) myView.findViewById(R.id.velocity)).setText(Double.toString(speed));
        else
            ((TextView) myView.findViewById(R.id.velocity)).setText("No data");

        if (impact != null)
            ((TextView) myView.findViewById(R.id.impact)).setText(Double.toString(impact));
        else
            ((TextView) myView.findViewById(R.id.impact)).setText("No data");

        if (pressure != null)
            ((TextView) myView.findViewById(R.id.pressure)).setText(Double.toString(pressure));
        else
            ((TextView) myView.findViewById(R.id.pressure)).setText("No data");

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // no need to check if empty, they have already been checked on main activity
        SharedPreferences prefGet = getActivity().getSharedPreferences(PREFERENCEID, Activity.MODE_PRIVATE);
        username = prefGet.getString("username", "bbbmetropolia@gmail.com");
        password = prefGet.getString("password", "badbadboys0");
    }

    public void getCurrentData() {

        Log.d("INFO", "Executing post-fetching action");

        if (recorder.getLastResultState() != "OK")
            return;


    }

    public void resetTrack() {
        Log.d("USR", "Button pressed");

        //SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy, h:mm a");

        try {
            /*if (thingsee == null)
                thingsee = new ThingSee(username, password);*/
            startTimestamp = TimestampDateHandler.dateToTimestamp(tdate.getText().toString());
//            realStartTimestamp = System.currentTimeMillis();
        } catch (Exception ex){}

        trackData.start(System.currentTimeMillis(),15000);

        recorder = new DataRecorder(username, password, trackData, startTimestamp, REQUEST_DELAY);
        recorder.start(handler);

        // we make the request to the Thingsee cloud server in backgroud
        // (AsyncTask) so that we don't block the UI (to prevent ANR state, Android Not Responding)
        //new Logs.TalkToThingsee().execute("QueryState");
    }

}
