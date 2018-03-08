package com.example.manuel.thingseedemo;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.manuel.thingseedemo.util.CalculusTools;
import com.example.manuel.thingseedemo.util.TimeStreamMapToScalar;
import com.example.manuel.thingseedemo.util.TimestampDateHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by manuel on 2/19/18.
 */


/**
 * Contains a series of TimeStream for the data the application need to collect
 */
public class TrackData implements Serializable {

    //How much time after the last element will cause the stream to return null data instead of the last element
    //Note: In the single TimeStreams, the value can be scaled to fit specific needs
    private long outOfBoundMargin;

    private long startTimestamp, firstTimestamp; //Requested (start) and real (first) time of start of the recording

    //Timestamp of the latest sample in any of the streams
    private long currentTimestamp;

    //Data TimeStreams
    private TimeStream<LocationData> location;
    private TimeStream<ScalarData> pressure;
    private TimeStream<ScalarData> impact;
    private TimeStream<ScalarData> battery;
    private TimeStream<ScalarData> temperature;
    private TimeStream<ScalarData> speed;
    private TimeStream<ScalarData> distance;

    //Has the track been initialized (started) and does it contain data?
    boolean initialized = false;
    boolean empty = true;

    /**
     * Start the track at a specific timestamp (ignoring everything coming from before)
     * @param timestamp
     * @param outOfBoundMarginTime
     */
    public void start(long timestamp, long outOfBoundMarginTime){
        clear();
        startTimestamp = timestamp;
        firstTimestamp = timestamp;
        currentTimestamp = timestamp;
        outOfBoundMargin = outOfBoundMarginTime;

        location = new TimeStream<>(outOfBoundMargin);
        pressure = new TimeStream<>(outOfBoundMargin*3);
        speed = new TimeStream<>(outOfBoundMargin);
        impact = new TimeStream<>(outOfBoundMargin/3);
        temperature = new TimeStream<>(outOfBoundMargin*3);
        battery = new TimeStream<>(outOfBoundMargin*9);

        //impact.setInterpolation(false);

        distance = new TimeStream<>(outOfBoundMargin);
        initialized = true;

    }

    /**
     * Start the track from when there is available data
     * @param outOfBoundMarginTime
     */
    public void start(long outOfBoundMarginTime){
        clear();
        startTimestamp = 0;
        firstTimestamp = 0;
        currentTimestamp = 0;
        outOfBoundMargin = outOfBoundMarginTime;

        location = new TimeStream<>(outOfBoundMargin);
        pressure = new TimeStream<>(outOfBoundMargin*3);
        speed = new TimeStream<>(outOfBoundMargin);
        impact = new TimeStream<>(outOfBoundMargin/3);
        temperature = new TimeStream<>(outOfBoundMargin*3);
        battery = new TimeStream<>(outOfBoundMargin*9);

        distance = new TimeStream<>(outOfBoundMargin);
        initialized = true;

    }

    /**
     * Fetch more data from the ThingSee object and insert it into the streams of the DataTrack
     * @param ts ThingSee object
     */
    void recordMore(ThingSee ts){
        //Change "fake" to true to use fake data instead of real one (for debugging)
        final boolean fake = false;

        //If the track has not been started we can not record
        if (!initialized) {
            Log.e("TRACK", "Error: Trying to record a non-initialized track.");
            return;
        }

        try {
            //Log.d("INFO", "TrackData fetching events...");
            JSONArray eventData = null;

            //Array of the timestamps of the events, used to compute the time range of the data
            ArrayList<Long> times = new ArrayList<>();

            if (!fake){
                //Get real data
               eventData = ts.Events(ts.Devices(), currentTimestamp);
            }

            //Set the ThingSee to generate fake data
            if (fake)
                ts.setFake();

            //Log.d("INFO", "Fetched " + (eventData == null ? 0 : eventData.length()) + " events");

            //If we are in real mode but there is no data, then exit
            if (!fake && eventData != null && eventData.length() == 0)
                return;


            //Get all the stream from the data (real or fake)
            TimeStream<LocationData> incomingLocations = ts.getLocationStream(eventData, outOfBoundMargin);
            TimeStream<ScalarData> incomingTemperatures = ts.getScalarStream(eventData, ThingSee.TEMPERATURE_DATA, outOfBoundMargin*3);
            TimeStream<ScalarData> incomingPressure = ts.getScalarStream(eventData, ThingSee.PRESSURE_DATA, outOfBoundMargin*3);
            TimeStream<ScalarData> incomingImpact = ts.getScalarStream(eventData, ThingSee.IMPACT_DATA, outOfBoundMargin/3);
            TimeStream<ScalarData> incomingBattery = ts.getScalarStream(eventData, ThingSee.BATTERY_DATA, outOfBoundMargin*9);
            TimeStream<ScalarData> incomingSpeed = ts.getScalarStream(eventData, ThingSee.SPEED_DATA, outOfBoundMargin);
            /*Log.d("INFO", "Got " + incomingTemperatures.sampleCount() + " temperatures");
            Log.d("INFO", "Got " + incomingLocations.sampleCount() + " locations");
            Log.d("INFO", "Got " + incomingBattery.sampleCount() + " battery");
            Log.d("INFO", "Got " + incomingSpeed.sampleCount() + " speed");
            Log.d("INFO", "Got " + incomingImpact.sampleCount() + " impact");
            Log.d("INFO", "Got " + incomingPressure.sampleCount() + " pressure");*/

            //Add the incoming streams to the stored ones, if they don't require additional computation
            pressure.addStream(incomingPressure);
            impact.addStream(incomingImpact);
            battery.addStream(incomingBattery);
            temperature.addStream(incomingTemperatures);
            speed.addStream(incomingSpeed);

            //ArrayList of all the first and last timestamps of each stream to find the earliest and latest
            if (incomingBattery.sampleCount() > 0)
            {times.add(incomingBattery.getLastTimestamp()); times.add(incomingBattery.getFirst().getTime());}
            if (incomingImpact.sampleCount() > 0)
            {times.add(incomingImpact.getLastTimestamp()); times.add(incomingImpact.getFirst().getTime());}
            if (incomingLocations.sampleCount() > 0)
            {times.add(incomingLocations.getLastTimestamp()); times.add(incomingLocations.getFirst().getTime());}
            if (incomingPressure.sampleCount() > 0)
            {times.add(incomingPressure.getLastTimestamp()); times.add(incomingPressure.getFirst().getTime());}
            if (incomingSpeed.sampleCount() > 0)
            {times.add(incomingSpeed.getLastTimestamp()); times.add(incomingSpeed.getFirst().getTime());}
            if (incomingTemperatures.sampleCount() > 0)
            {times.add(incomingTemperatures.getLastTimestamp()); times.add(incomingTemperatures.getFirst().getTime());}

            //If there is no timestamp, exit (this should never happen)
            if (times.size() < 1)
                return;

            //Initialize the first and last timestamp for the min and max algorithms
            long first = times.get(0);
            long last = first;

            //Find the min (first) and max (last) of the timestamps
            for (int i = 0; i < times.size(); i++){
                long cur = times.get(i);
                if (cur > last)
                    last = cur;

                if (cur < first)
                    first = cur;
            }

            //If there is no data in the track, use the timestamp of the first piece of data as
            //the beginning of the track
            if (isEmpty()) {
                firstTimestamp = first;
                //Log.d("TIMESEEK", "Set first timestamp: " + firstTimestamp);
            }

            /*Log.d("TIMESEEK", "First fetched: " + TimestampDateHandler.timestampToDate(first));
            Log.d("TIMESEEK", "Last fetched: " + TimestampDateHandler.timestampToDate(last));


            Log.d("INFO", "Got " + (eventData == null ? 0 : eventData.length()) + " data up to " + last);

            Log.d("INFO", "Total temperatures " + temperature.sampleCount());*/

            //Value of previous distance to add to the new distances computed from the incoming data
            double constant = 0.0;

            //If both the old and the new locations are not empty, we need to take the distance computed
            //at the last old location, and then add the distance between the last old and the first new
            //location. This value will be the constant to add to the locations computed in the incoming streams
            if (location.isNotEmpty() && incomingLocations.isNotEmpty()){
                ScalarData lastDistance = distance.getLast();
                LocationData lastLocation = location.getLast();

                /*Log.d("DIST", "Distance calculation");
                Log.d("DIST", "Last loc: " + lastLocation.getLatitude());
                Log.d("DIST", "Inc loc: " + incomingLocations.getFirst().getLatitude());*/

                if (lastDistance != null && lastLocation != null) {
                    constant = distance.getLast().getValue();
                    //Log.d("DIST", "Last dist: " + constant);

                    constant += location.getLast().arcLengthDist(incomingLocations.getFirst());
                }

               // Log.d("DIST", "Distance calculation constant: " + constant);
            }

            //Turn the constant to final, so it can be used in the anonymous class below
            final double finalConstant = constant;

            //Compute the new distances by first taking the length integral of the incoming location,
            //and the summing to each of its elements (by using map) the previous distance computed
            //before in finalConstant
            TimeStream<ScalarData> newDistances = incomingLocations.integrate(CalculusTools.vectorArcDistIntegral).map(new TimeStreamMapToScalar() {
                @Override
                public double map(Object x) {
                    return finalConstant + ((ScalarData) x).getValue();
                }
            });

            //Added the new distances and locations to the stored streams
            distance.addStream(newDistances);
            location.addStream(incomingLocations);

            //Set the current timestamp right after the last data we got
            currentTimestamp = last + 1;
            //Log.d("INFO", "TrackData.currentTimestamp = " + currentTimestamp);

            //The track is now surely initialized and not empty
            initialized = true;
            empty = false;

        } catch (Exception ex) {
            Log.e("TRACK", "Error: " + ex.getMessage());
        }
    }

    /**
     * Has the track been started?
     * @return
     */
    public boolean isInitialized(){
        return initialized;
    }

    /**
     * Clear an already started strack.
     */
    public void clear(){
        if (initialized) {
            location.clear();
            pressure.clear();
            impact.clear();
            battery.clear();
            temperature.clear();
            speed.clear();
            distance.clear();
            startTimestamp = 0;
            currentTimestamp = 0;
            empty = true;
        }
    }

    /**
     * Is there not any data recorded?
     * @return
     */
    public boolean isEmpty(){
        return empty;
    }

    /**
     * Is there any data recorded?
     * @return
     */
    public boolean isNotEmpty(){
        return !isEmpty();
    }

    /**
     * Get the timestamp of the first piece of data recorded by this track
     * @return
     */
    public long getFirstTimestamp(){
        return firstTimestamp;
    }

    /**
     * Get the timestamp passed to this track on its start
     * @return
     */
    public long getStartTimestamp(){
        return startTimestamp;
    }

    /**
     * Get the timestamp of the last recorded element in the track
     * @return
     */
    public long getCurrentTimestamp(){
        return currentTimestamp;
    }


    public TimeStream<LocationData> getLocationStream() {return location;}
    public TimeStream<ScalarData> getSpeedStream() {return speed;}
    public TimeStream<ScalarData> getTemperatureStream() {return temperature;}
    public TimeStream<ScalarData> getPressureStream() {return pressure;}
    public TimeStream<ScalarData> getDistanceStream() {return distance;}
    public TimeStream<ScalarData> getImpactStream() {return impact;}
    public TimeStream<ScalarData> getBatteryStream() {return battery;}

    /**
     * Get the last values of all the streams
     * @return
     */
    public AllDataStructure getAllLast(){
        ArrayList<Long> times = new ArrayList<>();

        times.add(location.getLastTimestamp());
        times.add(pressure.getLastTimestamp());
        times.add(temperature.getLastTimestamp());
        times.add(impact.getLastTimestamp());
        times.add(speed.getLastTimestamp());
        times.add(distance.getLastTimestamp());
        times.add(battery.getLastTimestamp());

        while (times.remove(null));

        if (times.size() == 0)
            return new AllDataStructure(0, null, null, null, null, null, null, null);

        long time = Collections.max(times);

        return new AllDataStructure(time,
                location.getLast(),
                pressure.getLast(),
                impact.getLast(),
                battery.getLast(),
                temperature.getLast(),
                speed.getLast(),
                distance.getLast());
    }

    /**
     * Get the values of all the streams at a certain point in time
     * @return
     */
    public AllDataStructure getAllAtTime(long time){
        return new AllDataStructure(time,
                location.getDataAtTime(time),
                pressure.getDataAtTime(time),
                impact.getDataAtTime(time),
                battery.getDataAtTime(time),
                temperature.getDataAtTime(time),
                speed.getDataAtTime(time),
                distance.getDataAtTime(time));
    }

    /**
     * Get an ArrayList of the values of all the streams every interval amount of time
     * @return
     */
    public ArrayList<AllDataStructure> createSamples(long interval){
        ArrayList<AllDataStructure> lst = new ArrayList<>();
        long cur = startTimestamp;

        while (cur < currentTimestamp){
            lst.add(getAllAtTime(cur));
            cur += interval;
        }

        return lst;
    }

    /**
     * Helper class holding a reference to an instance of all the physical data the application handles at
     * a certain point in time
     */
    public class AllDataStructure{

        long timestamp; //Timestamp must not be null
        Double latitude, longitude, altitude;
        Double pressure;
        Double impact;
        Double battery;
        Double temperature;
        Double speed;
        Double distance;

        public AllDataStructure(long time, LocationData loc, ScalarData p, ScalarData i, ScalarData b, ScalarData t, ScalarData s, ScalarData d){
            timestamp = time;
            if (loc != null) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
                altitude = loc.getAltitude();
            }
            if (p != null)
                pressure=p.getValue();
            if (i != null)
                impact=i.getValue();
            if (b != null)
                battery=b.getValue();
            if (t != null)
                temperature=t.getValue();
            if (s != null)
                speed = s.getValue();
            if (d != null)
                distance = d.getValue();
        }

        public long getTimestamp(){
            return timestamp;
        }

        @Nullable
        public Double getLatitude(){
            return latitude;
        }

        @Nullable
        public Double getAltitude(){
            return altitude;
        }

        @Nullable
        public Double getLongitude(){
            return longitude;
        }

        @Nullable
        public Double getDistance() {
            return distance;
        }

        @Nullable
        public Double getBattery() {
            return battery;
        }

        @Nullable
        public Double getImpact() {
            return impact;
        }

        @Nullable
        public Double getSpeed() {
            return speed;
        }

        @Nullable
        public Double getPressure() {
            return pressure;
        }

        @Nullable
        public Double getTemperature() {
            return temperature;
        }
    }
}
