package com.example.manuel.thingseedemo;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.manuel.thingseedemo.util.CalculusTools;
import com.example.manuel.thingseedemo.util.TimeStreamMapToScalar;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by manuel on 2/19/18.
 */

public class TrackData {

    private long outOfBoundMargin;
    private long startTimestamp;
    private long currentTimestamp;
    private TimeStream<LocationData> location;
    private TimeStream<ScalarData> pressure;
    private TimeStream<ScalarData> impact;
    private TimeStream<ScalarData> battery;
    private TimeStream<ScalarData> temperature;
    private TimeStream<ScalarData> speed;
    private TimeStream<ScalarData> distance;

    boolean initialized = false;

    public void start(long timestamp, long outOfBoundMarginTime){
        clear();
        startTimestamp = timestamp;
        currentTimestamp = timestamp;
        outOfBoundMargin = outOfBoundMarginTime;

        location = new TimeStream<>(outOfBoundMargin);
        pressure = new TimeStream<>(outOfBoundMargin);
        speed = new TimeStream<>(outOfBoundMargin);
        impact = new TimeStream<>(outOfBoundMargin);
        temperature = new TimeStream<>(outOfBoundMargin);
        battery = new TimeStream<>(outOfBoundMargin);

        distance = new TimeStream<>(outOfBoundMargin);

    }

    public void start(long outOfBoundMarginTime){
        clear();
        startTimestamp = 0;
        currentTimestamp = 0;
        outOfBoundMargin = outOfBoundMarginTime;

        location = new TimeStream<>(outOfBoundMargin);
        pressure = new TimeStream<>(outOfBoundMargin);
        speed = new TimeStream<>(outOfBoundMargin);
        impact = new TimeStream<>(outOfBoundMargin);
        temperature = new TimeStream<>(outOfBoundMargin);
        battery = new TimeStream<>(outOfBoundMargin);

        distance = new TimeStream<>(outOfBoundMargin);

    }

    void recordMore(ThingSee ts){
        try {
            Log.d("INFO", "TrackData fetching events...");
            JSONArray eventData = ts.Events(ts.Devices(), currentTimestamp);

            //Fake data
            //ts.setFake();

            Log.d("INFO", "Fetched " + eventData.length() + " events");

            if (eventData.length() == 0)
                return;

            long last = eventData.getJSONObject(eventData.length()-1).getLong("timestamp");

            Log.d("INFO", "Got " + eventData.length() + " data up to " + last);

            TimeStream<LocationData> incomingLocations = ts.getLocationStream(eventData, outOfBoundMargin);

            TimeStream<ScalarData> incomingTemperatures = ts.getScalarStream(eventData, ThingSee.TEMPERATURE_DATA, outOfBoundMargin*3);
            Log.d("INFO", "Got " + incomingTemperatures.sampleCount() + " temperatures");
            pressure.addStream(ts.getScalarStream(eventData, ThingSee.PRESSURE_DATA, outOfBoundMargin*3));
            impact.addStream(ts.getScalarStream(eventData, ThingSee.IMPACT_DATA, outOfBoundMargin));
            battery.addStream(ts.getScalarStream(eventData, ThingSee.BATTERY_DATA, outOfBoundMargin*9));
            temperature.addStream(incomingTemperatures);
            speed.addStream(ts.getScalarStream(eventData, ThingSee.SPEED_DATA, outOfBoundMargin));

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

            distance.addStream(
                    incomingLocations.integrate(CalculusTools.vectorArcDistIntegral).map(new TimeStreamMapToScalar() {
                        @Override
                        public double map(Object x) {
                            return finalConstant + ((ScalarData) x).getValue();
                        }
                    })
            );

            location.addStream(incomingLocations);

            currentTimestamp = last + 1;
            Log.d("INFO", "TrackData.currentTimestamp = " + currentTimestamp);

            initialized = true;

        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
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

    public List<AllDataStructure> createSamples(double interval){
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
