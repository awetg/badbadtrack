package com.example.manuel.thingseedemo;

import android.util.Log;

import org.json.JSONArray;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by manuel on 2/19/18.
 */

public class TrackData {

    double startTimestamp, endTimestamp;
    TimeStream<LocationData> location;
    TimeStream<PressureData> pressure;
    TimeStream<ImpactData> impact;
    TimeStream<BatteryData> battery;
    TimeStream<TemperatureData> temperature;
    TimeStream<SpeedData> speed;

    void initFromThingSee(ThingSee ts, double start, double end){
        try {
            JSONArray data = ts.Events(ts.Devices(), (long) start, (long) end);
            location = ts.getLocationStream(data);
            pressure = ts.getPressureStream(data);
            impact = ts.getImpactStream(data);
            battery = ts.getBatteryStream(data);
            temperature = ts.getTemperatureStream(data);
            speed = ts.getSpeedStream(data);
            startTimestamp = start;
            endTimestamp = end;

        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
        }
    }

    public LocationData getLocationOrNull(double time){
        return location.getDataAtTimeOrNull(time);
    }

    public PressureData getPressureOrNull(double time){
        return pressure.getDataAtTimeOrNull(time);
    }

    public TemperatureData getTemperatureOrNull(double time){
        return temperature.getDataAtTimeOrNull(time);
    }

    public SpeedData getSpeedOrNull(double time){
        return speed.getDataAtTimeOrNull(time);
    }

    public BatteryData getBatteyOrNull(double time){
        return battery.getDataAtTimeOrNull(time);
    }

    public ImpactData getImpactOrNull(double time){
        return impact.getDataAtTimeOrNull(time);
    }

    public AllDataStructure getAllAtTime(double time){
        return new AllDataStructure(time,
                getLocationOrNull(time),
                getPressureOrNull(time),
                getImpactOrNull(time),
                getBatteyOrNull(time),
                getTemperatureOrNull(time),
                getSpeedOrNull(time));
    }

    public List<AllDataStructure> createSamples(double interval){
        ArrayList<AllDataStructure> lst = new ArrayList<>();
        double cur = startTimestamp;

        while (cur < endTimestamp){
            lst.add(getAllAtTime(cur));
            cur += interval;
        }

        return lst;
    }


    public class AllDataStructure{

        double timestamp; //Timestamp must not be null
        Double latitude, longitude;
        Double pressure;
        Double impact;
        Double battery;
        Double temperature;
        Double speed;

        public AllDataStructure(double time, LocationData loc, PressureData p, ImpactData i, BatteryData b, TemperatureData t, SpeedData s){
            timestamp = time;
            latitude=loc.getLatitude();
            longitude=loc.getLongitude();
            pressure=p.getPressure();
            impact=i.getImpact();
            battery=b.getBattery();
            temperature=t.getTemperature();
            speed = s.getSpeed();
        }

        public double getTimestamp(){
            return latitude;
        }

        public Double getLatitude(){
            return latitude;
        }

        public Double getLongitude(){
            return longitude;
        }

        public Double getBattery() {
            return battery;
        }

        public Double getImpact() {
            return impact;
        }

        public Double getSpeed() {
            return speed;
        }

        public Double getPressure() {
            return pressure;
        }

        public Double getTemperature() {
            return temperature;
        }
    }

}
