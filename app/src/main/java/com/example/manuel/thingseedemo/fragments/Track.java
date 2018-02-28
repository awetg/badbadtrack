package com.example.manuel.thingseedemo.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.manuel.thingseedemo.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class Track extends Fragment implements View.OnClickListener {

    final String keyIntent = "start";
    View myView;
    LayoutInflater inflater;

    static  final String modeKey = "modeKey";
    static  final String mode = "mode";
    static  final String listMode = "list";
    static  final String addMode = "add";



    ListView listView;
    FloatingActionButton addButton;

    Button endButton;
    TextView trackNameText;

    String trackName;

    public Track() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_track, container, false);

        SharedPreferences sharedPreferences = getActivity() .getSharedPreferences(mode, getActivity().MODE_PRIVATE);
        String getmode = sharedPreferences.getString(mode ,"");
        if(!getmode.isEmpty()){
            if (getmode.equals(listMode)) {

                listView = myView.findViewById(R.id.trackList);
                addButton = myView.findViewById(R.id.addButton);
                addButton.setOnClickListener(this);


            }else {

                trackNameText = myView.findViewById(R.id.trackNameText);
                endButton = myView.findViewById(R.id.endButton);

            }
        }


        // Inflate the layout for this fragment

        return  myView;
    }

    @Override
    public void onClick(View view) {

        queryDialog(getActivity());
    }


    private void queryDialog(final Context context) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.track_name, null);

        String msg = "Add track name";

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView msgText      = promptsView.findViewById(R.id.msgText);
        final EditText nameTExt = promptsView.findViewById(R.id.nameText);

        msgText.setText(msg);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                trackName = nameTExt.getText().toString();

                                SharedPreferences prefPut = context .getSharedPreferences(mode, getActivity().MODE_PRIVATE);
                                SharedPreferences.Editor prefEditor = prefPut.edit();
                                prefEditor.putString(modeKey, addMode);
                                prefEditor.commit();

                                setView(R.layout.custom_list);
                                getViewItems();
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

    private void getViewItems() {

        trackNameText = myView.findViewById(R.id.trackNameText);
        endButton = myView.findViewById(R.id.endButton);
    }

    private void setView(int id) {

        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myView=inflater.inflate(id, null);
        ViewGroup rootView=(ViewGroup)getView();
        rootView.removeAllViews();
        rootView.addView(myView);

    }
}
