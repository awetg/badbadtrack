package com.example.manuel.thingseedemo.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.manuel.thingseedemo.R;
import com.example.manuel.thingseedemo.TrackService;
import com.example.manuel.thingseedemo.util.CustomAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class Track extends Fragment implements View.OnClickListener,AdapterView.OnItemClickListener {

    View myView;
    LayoutInflater inflater;
    SharedPreferences sharedPreferences;

    static final String MODE_KEY = "MODE_KEY";
    static final String MODE = "MODE";
    static final String LIST_MODE = "list";
    static final String ADD_MODE = "add";
    static final String LAST_TRACK = "name";
    static final String ALL_TRACK = "all";
    private static final String PREFERENCEID = "Credentials";



    ListView listView;
    FloatingActionButton addButton;

    Button endButton;
    TextView trackNameText;

    String trackName;
    CustomAdapter myAdapter;
    List<String> list;

    private String               username, password;

    Intent myIntent;


    public Track() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sharedPreferences = getActivity().getSharedPreferences(MODE_KEY, getActivity().MODE_PRIVATE);
        String getMode = sharedPreferences.getString(MODE, "");
        if (getMode.equals(LIST_MODE) || getMode.isEmpty()) {

            myView = inflater.inflate(R.layout.fragment_track, container, false);
            getViewItems(R.layout.fragment_track);

        } else if (getMode.equals(ADD_MODE)) {

            myView = inflater.inflate(R.layout.current_record, container, false);
            trackName = sharedPreferences.getString(LAST_TRACK,"");
            getViewItems(R.layout.current_record);

        }


        return myView;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.addButton:
                queryDialog(getActivity());
                break;

            case R.id.endButton:
                changeMode(LIST_MODE);
                setView(R.layout.fragment_track);
                getViewItems(R.layout.fragment_track);
                getActivity().stopService(myIntent);
                break;

        }

    }


    private void queryDialog(final Context context) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.track_name, null);

        String msg = "Add track name";

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView msgText = promptsView.findViewById(R.id.msgText);
        final EditText nameTExt = promptsView.findViewById(R.id.nameText);

        msgText.setText(msg);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                trackName = nameTExt.getText().toString();

                                changeMode(ADD_MODE);
                                addToTrackList();
                                setView(R.layout.current_record);
                                getViewItems(R.layout.current_record);
                                startService();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }



    private void getViewItems(int id) {


        switch (id) {
            case R.layout.fragment_track:
                listView = myView.findViewById(R.id.trackList);
                addButton = myView.findViewById(R.id.addButton);
                addButton.setOnClickListener(this);
                getTrackList();
                break;
            case R.layout.current_record:
                trackNameText = myView.findViewById(R.id.trackNameText);
                endButton = myView.findViewById(R.id.endButton);
                endButton.setOnClickListener(this);
                trackNameText.setText(trackName);
                break;
        }
    }


    private void setView(int id) {

        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myView = inflater.inflate(id, null);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(myView);

    }

    private void changeMode(String newMode) {

        SharedPreferences prefPut = getActivity().getSharedPreferences(MODE_KEY, getActivity().MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefPut.edit();
        prefEditor.putString(MODE, newMode);
        prefEditor.commit();

    }

    private void addToTrackList() {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putString(LAST_TRACK, trackName);
        Set<String> trackSet = sharedPreferences.getStringSet(ALL_TRACK, null);
        if (trackSet != null) {
            trackSet.add(trackName);
            prefEditor.putStringSet(ALL_TRACK, trackSet);
        } else {
            Set<String> set = new HashSet<String>();
            set.add(trackName);
            prefEditor.putStringSet(ALL_TRACK, set);
        }
        prefEditor.commit();


    }

    private void getTrackList() {
        Set<String> trackSet = sharedPreferences.getStringSet(ALL_TRACK, null);
        if (trackSet != null) {
            list = new ArrayList<String>(trackSet);
            myAdapter = new CustomAdapter(getContext(), R.layout.custom_list_item, list);
            listView.setAdapter(myAdapter);
            registerForContextMenu(listView);
            listView.setOnItemClickListener(this);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        view.showContextMenu();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.track_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.run:
                break;
            case R.id.delete:
                Set<String> trackSet = sharedPreferences.getStringSet(ALL_TRACK, null);
                if (trackSet != null) {
                    list.clear();
                    list.addAll(trackSet);
                    list.remove(itemInfo.position);
                    SharedPreferences.Editor prefEditor = sharedPreferences.edit();
                    trackSet = new HashSet<String>(list);
                    prefEditor.putStringSet(ALL_TRACK,trackSet);
                    prefEditor.commit();
                    myAdapter.notifyDataSetChanged();
                }
                break;
        }


        return super.onContextItemSelected(item);
    }


    private void startService() {

        final String KEY_INTERVAL = "key";
        final String KEY_USERNAME = "usr";
        final String KEY_PASSWORD = "pass";
        int interval = 10000;

        getCredentials();

//        Long timeStamp = System.currentTimeMillis()/1000;

        myIntent = new Intent(getActivity(),TrackService.class);

        myIntent.putExtra(KEY_INTERVAL,interval);
        myIntent.putExtra(KEY_USERNAME,username);
        myIntent.putExtra(KEY_PASSWORD,password);
        getActivity().startService(myIntent);
    }

    private void getCredentials() {
        SharedPreferences prefGet = getActivity().getSharedPreferences(PREFERENCEID, Activity.MODE_PRIVATE);
        username = prefGet.getString("username", "bbbmetropolia@gmail.com");
        password = prefGet.getString("password", "badbadboys0");
    }


}

