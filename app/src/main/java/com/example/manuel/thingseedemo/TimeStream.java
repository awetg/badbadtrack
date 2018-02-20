package com.example.manuel.thingseedemo;

import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by manuel on 2/19/18.
 */

public class TimeStream<T extends DataWithTime> {

    ArrayList<T> data;

    public TimeStream(){
        data = new ArrayList<>();
    }

    public void addSample(T sample){
        data.add(sample);
        Collections.sort(data);
    }

    public T getDataAtTimeOrNull(double time){
        //Find the element before and the one after the specified time
        int before = -1, after = -1;
        for (int i = 0; i < data.size(); i++){
            if (time > data.get(i).getTime()){
                after = i;
                before = i - 1;
                break;
            }
        }

        if (after < 1 || after >= data.size()){
            return null;
        }

        //Linearly interpolate between the two values
        //Note: The type of data is ArrayList<T>, so the cast will always be successful, since interpolate
        //      always returns the same type
        @SuppressWarnings("unchecked")
        T result = (T) data.get(before).interpolate(data.get(after), time);

        return result;
    }

}
