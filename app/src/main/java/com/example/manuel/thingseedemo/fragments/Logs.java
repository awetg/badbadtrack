package com.example.manuel.thingseedemo.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.manuel.thingseedemo.R;
import com.example.manuel.thingseedemo.ThingSee;
import com.example.manuel.thingseedemo.TrackData;
import com.example.manuel.thingseedemo.DataRecorder;

import org.json.JSONArray;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by awetg on 15.2.2018.
 * A simple {@link Fragment} subclass.
 */

public class Logs extends Fragment {

    private static final int    MAXEVENTS = 10;
    private static final int    REQUEST_DELAY = 2000;
    private static final long   INSTANT_MARGIN = 500000; //500 seconds window of data
    private static final String PREFERENCEID = "Credentials";

    TrackData data = new TrackData();
    TrackData.AllDataStructure currentData = null;

    DataRecorder recorder;
    ThingSee       thingsee;

    private String               username, password;
    //private String[]             positions = new String[MAXPOSITIONS];
    private ArrayAdapter<String> myAdapter;

    private View myView;
    EditText tdate;
    private long startTimestamp = 0;
    private long realStartTimestamp = 0;

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            if (msg.what == DataRecorder.DATA_UPDATED)
                getCurrentData();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize the array so that every position has an object (even it is empty string)
        //for (int i = 0; i < positions.length; i++)
        //positions[i] = "";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.logs,container,false);



        // setup the adapter for the array
        //myAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, positions);

        // then connect it to the list in application's layout
        //ListView listView = myView.findViewById(R.id.mylist);
        //listView.setAdapter(myAdapter);

        // setup the button event listener to receive onClick events
        //myView.findViewById(R.id.mybutton).setOnClickListener(this);

        ((Button)myView.findViewById(R.id.getDataButton)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resetTrack();
                    }
                }
        );

        tdate = myView.findViewById(R.id.date);
        long date = System.currentTimeMillis();
        startTimestamp = date;
        realStartTimestamp = date;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy, h:mm a");
        String dateString = sdf.format(date);
        tdate.setText(dateString);

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



//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // check that we know username and password for the Thingsee cloud
        SharedPreferences prefGet = getActivity().getSharedPreferences(PREFERENCEID, Activity.MODE_PRIVATE);
        username = prefGet.getString("username", "bbbmetropolia@gmail.com");
        password = prefGet.getString("password", "badbadboys0");
        if (username.length() == 0 || password.length() == 0)
            // no, ask them from the user
            queryDialog(getActivity(), getResources().getString(R.string.prompt));
    }

    public void getCurrentData() {

        Log.d("INFO", "Executing post-fetching action");

        if (recorder.getLastResultState() != "OK")
            return;

        long curTimestamp = startTimestamp + (System.currentTimeMillis() - realStartTimestamp);

        //Get sensor data from 5 seconds ago
        //currentData = data.getAllAtTime(curTimestamp - 5000);
        //currentData = data.getAllAtTime(data.getCurrentTimestamp());
        currentData = data.getAllLast();

        Log.d("INFO", "Temperature Data contains n. elements: " + data.getTemperatureStream().sampleCount());

        Double temperature, speed, impact, pressure;
        temperature = currentData.getTemperature();
        speed = currentData.getImpact();
        impact = currentData.getDistance();
        pressure = currentData.getBattery();

        Log.d("INFO", "Temperature is null: " + (temperature == null));

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

    public void resetTrack() {
        Log.d("USR", "Button pressed");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy, h:mm a");

        try {
            /*if (thingsee == null)
                thingsee = new ThingSee(username, password);*/

            startTimestamp = sdf.parse(tdate.getText().toString()).getTime();
            realStartTimestamp = System.currentTimeMillis();
        } catch (Exception ex){}

        data.start(10000);

        recorder = new DataRecorder(username, password, data, startTimestamp, REQUEST_DELAY);
        recorder.start(handler);

        // we make the request to the Thingsee cloud server in backgroud
        // (AsyncTask) so that we don't block the UI (to prevent ANR state, Android Not Responding)
        //new Logs.TalkToThingsee().execute("QueryState");
    }


    private void queryDialog(final Context context, String msg) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.credentials_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView dialogMsg      = promptsView.findViewById(R.id.textViewDialogMsg);
        final EditText dialogUsername = promptsView.findViewById(R.id.editTextDialogUsername);
        final EditText dialogPassword = promptsView.findViewById(R.id.editTextDialogPassword);

        dialogMsg.setText(msg);
        dialogUsername.setText(username);
        dialogPassword.setText(password);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                username = dialogUsername.getText().toString();
                                password = dialogPassword.getText().toString();

                                SharedPreferences prefPut = context .getSharedPreferences(PREFERENCEID, Activity.MODE_PRIVATE);
                                SharedPreferences.Editor prefEditor = prefPut.edit();
                                prefEditor.putString("username", username);
                                prefEditor.putString("password", password);
                                prefEditor.commit();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

}
