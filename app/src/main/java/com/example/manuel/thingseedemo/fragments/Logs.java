package com.example.manuel.thingseedemo.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.manuel.thingseedemo.R;
import com.example.manuel.thingseedemo.ThingSee;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by awetg on 15.2.2018.
 * A simple {@link Fragment} subclass.
 */

public class Logs extends Fragment implements View.OnClickListener {

    private static final int    MAXPOSITIONS = 20;
    private static final String PREFERENCEID = "Credentials";

    private String               username, password;
    private String[]             positions = new String[MAXPOSITIONS];
    private ArrayAdapter<String> myAdapter;

    private View myView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize the array so that every position has an object (even it is empty string)
        for (int i = 0; i < positions.length; i++)
            positions[i] = "";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.logs,container,false);



        // setup the adapter for the array
        myAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, positions);

        // then connect it to the list in application's layout
        ListView listView = myView.findViewById(R.id.mylist);
        listView.setAdapter(myAdapter);

        // setup the button event listener to receive onClick events
        myView.findViewById(R.id.mybutton).setOnClickListener(this);


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

    @Override
    public void onClick(View view) {
        Log.d("USR", "Button pressed");

        // we make the request to the Thingsee cloud server in backgroud
        // (AsyncTask) so that we don't block the UI (to prevent ANR state, Android Not Responding)
        new Logs.TalkToThingsee().execute("QueryState");
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





    /* This class communicates with the ThingSee client on a separate thread (background processing)
     * so that it does not slow down the user interface (UI)
     */
    private class TalkToThingsee extends AsyncTask<String, Integer, String> {
        ThingSee thingsee;
        List<Location> coordinates = new ArrayList<Location>();

        @Override
        protected String doInBackground(String... params) {
            String result = "NOT OK";

            // here we make the request to the cloud server for MAXPOSITION number of coordinates
            try {
                thingsee = new ThingSee(username, password);

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
                }
            } else {
                // no, tell that to the user and ask a new username/password pair
                positions[0] = getResources().getString(R.string.no_connection);
                    queryDialog(getActivity(), getResources().getString(R.string.info_prompt));
            }
            myAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            // first clear the previous entries (if they exist)
            for (int i = 0; i < positions.length; i++)
                positions[i] = "";
            myAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {}
    }
}
