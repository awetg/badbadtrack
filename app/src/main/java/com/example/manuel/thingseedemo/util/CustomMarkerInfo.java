package com.example.manuel.thingseedemo.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.manuel.thingseedemo.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by awetg on 8.3.2018.
 */

public class CustomMarkerInfo implements GoogleMap.InfoWindowAdapter {

    View myView;
    Context context;

    public CustomMarkerInfo(Context context) {
        this.context = context;
        myView = LayoutInflater.from(context).inflate(R.layout.custom_marker_info,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView title = myView.findViewById(R.id.markerTitle);
        TextView more = myView.findViewById(R.id.moreInfo);
        String titleString = marker.getTitle();
        String moreString = marker.getSnippet();
        if(titleString!=null)
            title.setText(titleString);
        if(moreString!=null)
                more.setText(moreString);
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        setView(marker);
        return null;
    }

    private void setView (Marker marker){

    }
}
