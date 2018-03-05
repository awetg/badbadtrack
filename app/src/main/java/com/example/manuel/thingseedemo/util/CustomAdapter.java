package com.example.manuel.thingseedemo.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.manuel.thingseedemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by awetg on 1.3.2018.
 */

public class CustomAdapter extends ArrayAdapter<String> {

    private List list = new ArrayList();
    private Context context;


    public CustomAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        this.list = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View myView = layoutInflater.inflate(R.layout.custom_list_item, null, true);

        TextView txt = (TextView) myView.findViewById(R.id.tx);
        txt.setText(list.get(position).toString());

        return myView;
    }
}
