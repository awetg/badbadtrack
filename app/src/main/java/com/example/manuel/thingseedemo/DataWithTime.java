package com.example.manuel.thingseedemo;

import android.support.annotation.NonNull;

/**
 * Created by manuel on 2/19/18.
 */

public abstract class DataWithTime implements Comparable {

    double time;

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof DataWithTime){
            return Double.compare(this.time, ((DataWithTime) o).time);
        }
        return 0;
    }

    abstract DataWithTime interpolate(Object that, double time);

}
