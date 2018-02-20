package com.example.manuel.thingseedemo;

/**
 * Created by manuel on 2/19/18.
 */

public class LocationData extends DataWithTime {

    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public LocationData interpolate(Object that, double time) {
        if (that instanceof LocationData) {
            LocationData thatLoc = (LocationData) that;
            LocationData result = new LocationData();
            double factor = time / (thatLoc.getTime() - this.getTime());

            result.setLatitude(this.getLatitude() + factor * (thatLoc.getLatitude() - this.getLatitude()));
            result.setLongitude(this.getLongitude() + factor * (thatLoc.getLongitude() - this.getLongitude()));

            return result;
        }
        return null;
    }
}
