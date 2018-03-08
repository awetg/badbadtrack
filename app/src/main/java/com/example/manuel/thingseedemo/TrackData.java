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



public class TrackData implements Serializable {

    private long outOfBoundMargin;
    private long startTimestamp, firstTimestamp; //Requested (start) and real (first) time of start of the recording
    private long currentTimestamp;
    private TimeStream<LocationData> location;
    private TimeStream<ScalarData> pressure;
    private TimeStream<ScalarData> impact;
    private TimeStream<ScalarData> battery;
    private TimeStream<ScalarData> temperature;
    private TimeStream<ScalarData> speed;
    private TimeStream<ScalarData> distance;

    boolean initialized = false;
    boolean empty = true;

    public void start(long timestamp, long outOfBoundMarginTime){
        clear();
        startTimestamp = timestamp;
        firstTimestamp = timestamp;
        currentTimestamp = timestamp;
        outOfBoundMargin = outOfBoundMarginTime;

        location = new TimeStream<>(outOfBoundMargin);
        pressure = new TimeStream<>(outOfBoundMargin);
        speed = new TimeStream<>(outOfBoundMargin);
        impact = new TimeStream<>(outOfBoundMargin);
        temperature = new TimeStream<>(outOfBoundMargin);
        battery = new TimeStream<>(outOfBoundMargin);

        distance = new TimeStream<>(outOfBoundMargin);
        initialized = true;

    }

    public void start(long outOfBoundMarginTime){
        clear();
        startTimestamp = 0;
        firstTimestamp = 0;
        currentTimestamp = 0;
        outOfBoundMargin = outOfBoundMarginTime;

        location = new TimeStream<>(outOfBoundMargin);
        pressure = new TimeStream<>(outOfBoundMargin);
        speed = new TimeStream<>(outOfBoundMargin);
        impact = new TimeStream<>(outOfBoundMargin);
        temperature = new TimeStream<>(outOfBoundMargin);
        battery = new TimeStream<>(outOfBoundMargin);

        distance = new TimeStream<>(outOfBoundMargin);
        initialized = true;

    }

    void recordMore(ThingSee ts){
        final boolean fake = false;

        try {
            Log.d("INFO", "TrackData fetching events...");
            JSONArray eventData = null;
            ArrayList<Long> times = new ArrayList<>();

            if (!fake){
               eventData = ts.Events(ts.Devices(), currentTimestamp);
            }

            if (fake)
                ts.setFake();

            Log.d("INFO", "Fetched " + (eventData == null ? 0 : eventData.length()) + " events");

            if (!fake && eventData != null && eventData.length() == 0)
                return;



            TimeStream<LocationData> incomingLocations = ts.getLocationStream(eventData, outOfBoundMargin);
            TimeStream<ScalarData> incomingTemperatures = ts.getScalarStream(eventData, ThingSee.TEMPERATURE_DATA, outOfBoundMargin*3);
            TimeStream<ScalarData> incomingPressure = ts.getScalarStream(eventData, ThingSee.PRESSURE_DATA, outOfBoundMargin*3);
            TimeStream<ScalarData> incomingImpact = ts.getScalarStream(eventData, ThingSee.IMPACT_DATA, outOfBoundMargin);
            TimeStream<ScalarData> incomingBattery = ts.getScalarStream(eventData, ThingSee.BATTERY_DATA, outOfBoundMargin*9);
            TimeStream<ScalarData> incomingSpeed = ts.getScalarStream(eventData, ThingSee.SPEED_DATA, outOfBoundMargin);
            Log.d("INFO", "Got " + incomingTemperatures.sampleCount() + " temperatures");
            Log.d("INFO", "Got " + incomingLocations.sampleCount() + " locations");
            Log.d("INFO", "Got " + incomingBattery.sampleCount() + " battery");
            Log.d("INFO", "Got " + incomingSpeed.sampleCount() + " speed");
            Log.d("INFO", "Got " + incomingImpact.sampleCount() + " impact");
            Log.d("INFO", "Got " + incomingPressure.sampleCount() + " pressure");
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

            if (times.size() < 1)
                return;

            long first = times.get(0);
            long last = first;

            for (int i = 0; i < times.size(); i++){
                long cur = times.get(i);
                if (cur > last)
                    last = cur;

                if (cur < first)
                    first = cur;
            }

            if (isEmpty()) {
                firstTimestamp = first;
                Log.d("TIMESEEK", "Set first timestamp: " + firstTimestamp);
            }

            Log.d("TIMESEEK", "First fetched: " + TimestampDateHandler.timestampToDate(first));
            Log.d("TIMESEEK", "Last fetched: " + TimestampDateHandler.timestampToDate(last));


            Log.d("INFO", "Got " + (eventData == null ? 0 : eventData.length()) + " data up to " + last);

            Log.d("INFO", "Total temperatures " + temperature.sampleCount());

            double constant = 0.0;

            if (location.isNotEmpty() && incomingLocations.isNotEmpty()){
                //double dt = incomingLocations.getLast().getTime() - location.getLast().getTime();
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

            final double finalConstant = constant;

            TimeStream<ScalarData> newDistances = incomingLocations.integrate(CalculusTools.vectorArcDistIntegral).map(new TimeStreamMapToScalar() {
                @Override
                public double map(Object x) {
                    return finalConstant + ((ScalarData) x).getValue();
                }
            });

            distance.addStream(newDistances);

            location.addStream(incomingLocations);

            currentTimestamp = last + 1;
            Log.d("INFO", "TrackData.currentTimestamp = " + currentTimestamp);

            initialized = true;
            empty = false;

        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }

    public boolean isInitialized(){
        return initialized;
    }


    void clear(){
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
        }
    }

    public boolean isEmpty(){
        return empty;
    }

    public boolean isNotEmpty(){
        return !isEmpty();
    }

    public long getFirstTimestamp(){
        return firstTimestamp;
    }

    public long getStartTimestamp(){
        return startTimestamp;
    }

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

    public ArrayList<AllDataStructure> createSamples(long interval){
        ArrayList<AllDataStructure> lst = new ArrayList<>();
        long cur = startTimestamp;

        while (cur < currentTimestamp){
            lst.add(getAllAtTime(cur));
            cur += interval;
        }

        return lst;
    }


    public class AllDataStructure{

        long timestamp; //Timestamp must not be null
        Double latitude, longitude;
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
