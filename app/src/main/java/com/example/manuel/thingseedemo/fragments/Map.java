package com.example.manuel.thingseedemo.fragments;


import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.manuel.thingseedemo.DataRecorder;
import com.example.manuel.thingseedemo.LocationData;
import com.example.manuel.thingseedemo.R;
import com.example.manuel.thingseedemo.RealTimeRecorder;
import com.example.manuel.thingseedemo.ThingSee;
import com.example.manuel.thingseedemo.TimeStream;
import com.example.manuel.thingseedemo.TrackData;
import com.example.manuel.thingseedemo.util.DataStorage;
import com.example.manuel.thingseedemo.util.TimestampDateHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Map extends Fragment implements OnMapReadyCallback {

    private static final String PREFERENCEID = "Credentials";
    static final int REQUEST_DELAY = 10000;



    private View myView;

    GoogleMap myMap;
    PolylineOptions polylineOptions;


    boolean real = true;

    RealTimeRecorder realTimeRecorder;
    private String               username, password;

    ProgressDialog progressDialog;



    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            if (msg.what == RealTimeRecorder.DATA_UPDATED) {
                updateLocation();
                Log.d("INFO", "Message received from Logs");
            }
        }
    };




    public Map() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        // myView in case we want to access ui elements in this fragment

        myView = inflater.inflate(R.layout.fragment_map, container, false);
        progressDialog = new ProgressDialog(getActivity());

        if(!DataStorage.isRealtime()){

            real = false;

        }


        return myView;
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        myMap = googleMap;

        //LatLng metropolia = new LatLng(60.220941, 24.804980);


        if(real) {

            progressDialog.setMessage("Connecting to Thingsee server");
            progressDialog.show();
            StartTrack();

        }
        else {
            getTrack();

            if(polylineOptions!=null) {

            // adding this location to make the line longer, can't be seen only 3 location data yet
//            polylineOptions.add(metropolia);

            myMap.addPolyline(polylineOptions);
            List<LatLng> l = polylineOptions.getPoints();

            if (l!=null && l.size() > 0)
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(l.get(l.size() - 1), 15));


        }

        }
    }

    private void StartTrack() {

        //trackData = new TrackData();
        //trackData.start(10000);
        getCredentials();

        realTimeRecorder = new RealTimeRecorder(username, password, REQUEST_DELAY);
        realTimeRecorder.start(handler);
    }

    private void updateLocation() {

        if (realTimeRecorder.getLastResultState() != "OK")
            return;
        else {
            TrackData temp = realTimeRecorder.getData();
            if(temp!=null) {

                LocationData locationData = temp.getLocationStream().getLast();

                if (locationData == null)
                    return;
                progressDialog.dismiss();   
                String date = TimestampDateHandler.relativeTime(locationData.getTime());
                LatLng lastLatLang = new LatLng(locationData.getLatitude(), locationData.getLongitude());
                MarkerOptions options1 = new MarkerOptions();
                options1.position(lastLatLang)
                        .title("Device location ")
                .snippet(date);
                myMap.addMarker(options1);
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLang, 15));
            }

        }
    }


    private void getCredentials(){
        SharedPreferences prefGet = getActivity().getSharedPreferences(PREFERENCEID, getActivity().MODE_PRIVATE);
        username = prefGet.getString("username", "bbbmetropolia@gmail.com");
        password = prefGet.getString("password", "badbadboys0");
    }

    private void getTrack() {
        TrackData trackData = DataStorage.getTrackData();
        if(trackData!=null) {

            polylineOptions = new PolylineOptions();
            polylineOptions.color( Color.BLUE );
            polylineOptions.width( 15 );
            polylineOptions.visible( true );

            TimeStream<LocationData> locationDataTimeStream = trackData.getLocationStream();
            if(locationDataTimeStream!=null) {

                for ( int j = 0; j < locationDataTimeStream.sampleCount(); j++){
                    polylineOptions.add(locationDataTimeStream.get(j).getLatLang());
                    //Log.d("LATLANG : ",locationDataTimeStream.get(j).getLatLang().latitude + " " + locationDataTimeStream.get(j).getLatLang().longitude);
                }
            }
        }

        ArrayList<String> gh = DataStorage.savedTracksNames();

    }

}