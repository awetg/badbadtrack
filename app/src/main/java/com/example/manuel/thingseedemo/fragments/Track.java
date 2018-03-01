package com.example.manuel.thingseedemo.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.manuel.thingseedemo.R;

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

    static final String modeKey = "modeKey";
    static final String mode = "mode";
    static final String listMode = "list";
    static final String addMode = "add";
    static final String lastTrack = "name";
    static final String allTrack = "all";


    ListView listView;
    FloatingActionButton addButton;

    Button endButton;
    TextView trackNameText;

    String trackName;
    ArrayAdapter<String> myAdapter;
    List<String> list;

    public Track() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sharedPreferences = getActivity().getSharedPreferences(mode, getActivity().MODE_PRIVATE);
        String getMode = sharedPreferences.getString(mode, "");
        if (getMode.equals(listMode) || getMode.isEmpty()) {

            myView = inflater.inflate(R.layout.fragment_track, container, false);
            getViewItems(R.layout.fragment_track);

        } else if (getMode.equals(addMode)) {

            myView = inflater.inflate(R.layout.custom_list, container, false);
            getViewItems(R.layout.custom_list);

        }


        // Inflate the layout for this fragment

        return myView;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.addButton:
                queryDialog(getActivity());
                break;

            case R.id.endButton:
                changeMode(listMode);
                setView(R.layout.fragment_track);
                getViewItems(R.layout.fragment_track);
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

                                changeMode(addMode);
                                addToTrackList();
                                setView(R.layout.custom_list);
                                getViewItems(R.layout.custom_list);
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
            case R.layout.custom_list:
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

        SharedPreferences prefPut = getActivity().getSharedPreferences(mode, getActivity().MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefPut.edit();
        prefEditor.putString(modeKey, newMode);
        prefEditor.commit();

    }

    private void addToTrackList() {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putString(lastTrack, trackName);
        Set<String> trackSet = sharedPreferences.getStringSet(allTrack, null);
        if (trackSet != null) {
            trackSet.add(trackName);
            prefEditor.putStringSet(allTrack, trackSet);
        } else {
            Set<String> set = new HashSet<String>();
            set.add(trackName);
            prefEditor.putStringSet(allTrack, set);
        }
        prefEditor.commit();


    }

    private void getTrackList() {
        Set<String> trackSet = sharedPreferences.getStringSet(allTrack, null);
        if (trackSet != null) {
            list = new ArrayList<String>(trackSet);
            myAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list);
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
                Set<String> trackSet = sharedPreferences.getStringSet(allTrack, null);
                if (trackSet != null) {
                    list.clear();
                    list.addAll(trackSet);
                    list.remove(itemInfo.position);
                    SharedPreferences.Editor prefEditor = sharedPreferences.edit();
                    trackSet = new HashSet<String>(list);
                    prefEditor.putStringSet(allTrack,trackSet);
                    prefEditor.commit();
                    myAdapter.notifyDataSetChanged();
                }
                break;
        }


        return super.onContextItemSelected(item);
    }

}

