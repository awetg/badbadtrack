package com.example.manuel.thingseedemo.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.manuel.thingseedemo.R;
import com.example.manuel.thingseedemo.TrackerService;


/**
 * A simple {@link Fragment} subclass.
 */
public class Tracker extends Fragment implements View.OnClickListener {

    final String keyIntent = "start";


    public Tracker() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tracker, container, false);
    }

    @Override
    public void onClick(View view) {
        //
        //
        // we call service here by sending time stamp

        Long timeStamp = System.currentTimeMillis()/1000;

        Intent intent = new Intent(getActivity(),TrackerService.class);

        intent.putExtra(keyIntent,timeStamp);

        getActivity().startService(intent);
    }
}
