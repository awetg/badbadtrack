package com.example.manuel.thingseedemo;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by manuel on 2/19/18.
 */

public class LocationData extends DataWithTime {

    private double latitude;
    private double longitude;
    private double altitude;

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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public LatLng getLatLang (){
        return new LatLng(latitude,longitude);
    }

    public double dist2d(LocationData that){
        double result = 0.0;

        result += (this.latitude - that.latitude) * (this.latitude - that.latitude);
        result += (this.longitude - that.longitude) * (this.longitude - that.longitude);

        return Math.sqrt(result);
    }

    public double dist3d(LocationData that){
        double result = 0.0;

        result += (this.latitude - that.latitude) * (this.latitude - that.latitude);
        result += (this.longitude - that.longitude) * (this.longitude - that.longitude);
        result += (this.altitude - that.altitude) * (this.altitude - that.altitude);

        return Math.sqrt(result);
    }

    public double arcLengthDist(LocationData that){
        double R = 6371000.0; //6.371 km radius
        double lat2 = that.latitude * Math.PI / 180;
        double lat1 = this.latitude * Math.PI / 180;
        double lon2 = that.longitude * Math.PI / 180;
        double lon1 = this.longitude * Math.PI / 180;
        double dtheta = (lon2 - lon1);
        double dphi = (lat2 - lat1);

        double a =      Math.sin(dphi/2) * Math.sin(dphi/2) +
                        Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dtheta/2) * Math.sin(dtheta/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;

        return d;
    }

    @Override
    public LocationData interpolate(Object that, double time) {
        if (that instanceof LocationData) {
            LocationData thatLoc = (LocationData) that;
            LocationData result = new LocationData();
            double factor = (time - this.getTime()) / (thatLoc.getTime() - this.getTime());

            result.setLatitude(this.getLatitude() + factor * (thatLoc.getLatitude() - this.getLatitude()));
            result.setLongitude(this.getLongitude() + factor * (thatLoc.getLongitude() - this.getLongitude()));
            result.setLongitude(this.getAltitude() + factor * (thatLoc.getLatitude() - this.getAltitude()));

            return result;
        }
        return null;
    }
}
