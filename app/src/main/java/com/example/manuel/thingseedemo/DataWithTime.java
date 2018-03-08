package com.example.manuel.thingseedemo;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by manuel on 2/19/18.
 */


/**
 * Represents an abstract sample of data with the timestamp it's been collected.
 * Inherit this class to specify concrete data (eg. ScalarData, LocationData)
 */
public abstract class DataWithTime implements Comparable, Serializable {

    //The timestamp at which the data has been collected
    long time;

    /**
     * Get the timestamp at which the data was collected
     * @return timestamp
     */
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Compare by timestamp. The data with the lowest timestamp is the smaller.
     * @param o object to compare to
     * @return
     */
    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof DataWithTime){
            //Use the normal natural number comparison between the timestamps
            return Long.compare(this.time, ((DataWithTime) o).time);
        }
        return 0;
    }

    /**
     * Given a second object (that) and a time in between the timestamp of "this" and "that", compute
     * and return an object at such in-between time, with an in-between value that lies on the line
     * between "this" and "that". See linear interpolation.
     * @param that the second object
     * @param time the time at which the interpolated data will refer to
     * @return the intepolated data between "this" and "that" at time "time"
     */
    abstract DataWithTime interpolate(Object that, double time);

}
