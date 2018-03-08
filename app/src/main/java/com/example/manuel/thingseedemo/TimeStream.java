package com.example.manuel.thingseedemo;

import android.support.annotation.Nullable;

import com.example.manuel.thingseedemo.util.Derivative;
import com.example.manuel.thingseedemo.util.Integral;
import com.example.manuel.thingseedemo.util.TimeStreamMapToScalar;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by manuel on 2/19/18.
 */

/**
 * A stream of DataWithTime that gets filled with samples and can return values at any time by
 * linearly interpolating between the samples
 * @param <T> type of the data in the stream (must inherit DataWithTime)
 */
public class TimeStream<T extends DataWithTime> implements Serializable {

    //Samples of the time stream
    private ArrayList<T> data;

    //How much time after the last element will cause the stream to return null data instead of the last element
    long outOfBoundMargin;

    //Some streams should not interpolate, because they handle data that are not continuous by nature
    //like for example impact data. If doInterpolation == false, the stream will return the latest previous
    //sample within outOfBoundMargin, or null outside of outOfBoundMargin
    protected boolean doInterpolation = true;

    /**
     * Create a timestream
     * @param outOfBoundMarginTime How much time after the last element will cause the stream to return null
     *                            data instead of the last element
     */
    public TimeStream(long outOfBoundMarginTime){

        data = new ArrayList<>();
        outOfBoundMargin = outOfBoundMarginTime;
    }

    /**
     * Add a sample to the stream
     * @param sample
     */
    public void addSample(T sample){
        data.add(sample);
        Collections.sort(data); //Make sure the stream is always ordered by timestamp (see DataWithTime compareTo)
    }

    /**
     * Add all samples from another stream to this one, and changed the outOfBoundMargin of this stream
     * to the ine of the second stream
     * @param stream the second stream
     */
    public void addStream(TimeStream<T> stream){
        //We will establish if the first element of the second stream comes in time after all the elements
        //of the first stream
        boolean isLater = true;

        //The outOfBoundMargin of the this stream will be changed to the one of the second stream
        outOfBoundMargin = stream.outOfBoundMargin;

        //If this stream is empty, just add all the samples of the second stream
        if (data.isEmpty())
            data.addAll(stream.data);
        else { //If this stream is not empty...
            //...get the last element from it...
            long last = data.get(data.size() - 1).getTime();
            //..and compare it with every element of the second stream to check if they are all later
            for (T entry : stream.data) {
                isLater = isLater && (entry.getTime() >= last);
            }
            //Add all the element of the second stream to this stream
            data.addAll(stream.data);

            //If not all the element of the second stream are later than the last one of this stream,
            //reorder it.
            if (!isLater)
                Collections.sort(data);
        }
    }

    /**
     * How many samples in the stream
     * @return
     */
    public int sampleCount(){
        return data.size();
    }

    /**
     * Last sample in the stream
     * @return
     */
    @Nullable
    public T getLast(){
        if (data.size() <= 0)
            return null;
        return data.get(data.size() - 1);
    }

    /**
     * First sample in the stream
     * @return
     */
    @Nullable
    public T getFirst(){
        if (data.size() <= 0)
            return null;
        return data.get(0);
    }

    /**
     * Get the ith + 1 sample of the stream
     * @param i index
     * @return
     */
    @Nullable
    public T get(int i){
        if (data.size() <= 0)
            return null;
        return data.get(i);
    }

    /**
     * Is the stream empty? (Does not contain samples)
     * @return
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Is the stream not empty? (Contains at least one sample)
     * @return
     */
    public boolean isNotEmpty() {
        return !data.isEmpty();
    }

    /**
     * Get the timestamp of the last element of the stream, or 0 if the stream is empty
     * @return
     */
    @Nullable
    public long getLastTimestamp(){
        if (data.size() <= 0)
            return 0;
        return data.get(data.size()-1).getTime();
    }

    /**
     * Create a new stream with the time integral of this stream, for example distance from location
     * @param integral integral function to use
     * @return
     */
    public TimeStream<ScalarData> integrate(Integral integral){

        //Accumulator for the integral
        double acc = 0.0;

        //Resulting TimeStream
        TimeStream<ScalarData> result = new TimeStream<>(outOfBoundMargin);

        //If the TimeStream has less than 2 elements we can not compute the integral
        if (data.size() <= 1)
            return result;

        //Get the first element as the first oldElem
        T oldElem = data.get(0);

        //For each pair of elements and their dt, call the integral function, keeping track of the accumulator
        for (int i = 1; i < data.size(); i++){
            T curElem = data.get(i);
            double dt = curElem.getTime() - oldElem.getTime();
            ScalarData integralValue = new ScalarData();
            acc = integral.integral(acc, oldElem, curElem, dt);
            integralValue.setValue(acc);
            integralValue.setTime(oldElem.getTime() + (long)(dt / 2L));
            result.addSample(integralValue);
            oldElem = curElem;
        }

        return result;
    }

    /**
     * Create a new stream with the time derivative of this stream, for example speed from distance
     * @param derivative
     * @return
     */
    public TimeStream<ScalarData> differentiate(Derivative derivative){

        TimeStream<ScalarData> result = new TimeStream<>(outOfBoundMargin);

        if (data.size() <= 1)
            return result;

        //Get the first element as the first oldElem
        T oldElem = data.get(0);

        //For each pair of elements and their dt, call the derivative function
        for (int i = 1; i < data.size(); i++){
            T curElem = data.get(i);
            double dt = curElem.getTime() - oldElem.getTime();
            ScalarData derivativeValue = new ScalarData();
            derivativeValue.setValue(derivative.derivative(oldElem, curElem, dt));
            derivativeValue.setTime(oldElem.getTime() + (long)(dt / 2L));
            result.addSample(derivativeValue);
            oldElem = curElem;
        }

        return result;
    }

    /**
     * Create a new TimeStream of ScalarData having a real number value for each element
     * in the original TimeStream, number computed by the map function from the TimeStreamMapToScalar interface
     * @param map implementation of the map to use
     * @return a new TimeStream of ScalarData in which the current TimeStream has been mapped by the mapping method
     */
    public TimeStream<ScalarData> map(TimeStreamMapToScalar map){

        TimeStream<ScalarData> result = new TimeStream<>(outOfBoundMargin);

        //For each sample in the stream...
        for (int i = 0; i < data.size(); i++){
            T curElem = data.get(i);
            ScalarData newElem = new ScalarData();

            //...apply the mapping function
            newElem.setValue(map.map(curElem));
            newElem.setTime(curElem.getTime());
            result.addSample(newElem);
        }

        return result;
    }

    /**
     * Remove all the samples from this stream
     */
    public void clear(){
        data.clear();
    }

    /**
     * Get the value of the quantity in the stream at a specific time (interpolating between the samples
     * , if doInterpolation == true)
     * @param time timestamp
     * @return
     */
    @Nullable
    public T getDataAtTime(long time){
        //Find the element before and the one after the specified time
        int before = -1, after = -1;
        long elemTime, timeDiff = 0;

        //Find the element after and the element before the specified time
        for (int i = 0; i < data.size(); i++){
            //Log.d("Stream", "Time: " + time + " CTime: " + data.get(i).getTime() + " index: " + i + " size: " + data.size());
            elemTime = data.get(i).getTime();
            timeDiff = elemTime - time;
            if (timeDiff >= 0){
                after = i;
                before = i - 1;
                break;
            }
        }

        //If we are not interpolating and before (= after - 1) exists...
        if (!doInterpolation && before > 0 && before < data.size()){
            T itemBefore = data.get(before);

            //Return the item right before the specified time if not too old, otherwise return null
            if (time - itemBefore.getTime() <= outOfBoundMargin)
                return itemBefore;
            else
                return null;
        }

        //If there is no data, we are before the first element (after is the first element) or if after is not valid
        //the return null
        if (data.size() == 0 || after == 0 || after > data.size()){
            return null;
        }

        //If the requested time is after the last element, return the last element,
        //but only if the requested time is not much later
        if (time - getLastTimestamp() > outOfBoundMargin)
            return  null;
        if (after == -1 || before == data.size() - 1)
            return data.get(data.size() - 1);


        //Linearly interpolate between the two values
        //Note: The type of data is ArrayList<T>, so the cast will always be successful, since interpolate
        //      always returns the same type
        @SuppressWarnings("unchecked")
        T result = (T) data.get(before).interpolate(data.get(after), time);

        return result;
    }

    /**
     * Create samples of the TimeStream every certain interval of time, and return them in an ArrayList
     * @param interval
     * @return
     */
    public ArrayList<T> createSamples(long interval){
        ArrayList<T> lst = new ArrayList<>();

        if (isEmpty())
            return lst;

        long cur = getFirst().getTime();

        while (cur < getLastTimestamp()){
            lst.add(getDataAtTime(cur));
            cur += interval;
        }

        return lst;
    }

    /**
     * Create samples of the TimeStream every certain interval of time, within a specified range of time,
     * and return them in an ArrayList
     * @param interval
     * @return
     */
    public ArrayList<T> createSamples(long interval, long start, long end){
        ArrayList<T> lst = new ArrayList<>();

        if (isEmpty())
            return lst;

        long cur = start;

        while (cur < end){
            lst.add(getDataAtTime(cur));
            cur += interval;
        }

        return lst;
    }

    /**
     * Enable or disable interpolation
     * @param interpolate
     */
    public void setInterpolation(boolean interpolate){
        doInterpolation = interpolate;
    }


}
