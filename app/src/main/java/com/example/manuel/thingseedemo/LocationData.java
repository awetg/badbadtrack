package com.example.manuel.thingseedemo;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by manuel on 2/19/18.
 */

/**
 * Represents location data (latitude, longitude and altitude) associated with a timestamp (see extends DataWithTime)
 */
public class LocationData extends DataWithTime {

    //Location data: latitude, longitude and altitude
    private double latitude;
    private double longitude;
    private double altitude;

    //********************************************************
    //Setters and getters for latitude, longitude and altitude

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

    //Setters and getters for latitude, longitude and altitude
    //********************************************************

    /**
     * Compute a 2d distance of the coordinates in degrees
     * @param that second location (arrival point)
     * @return distance in degrees between this and that
     */
    public double dist2d(LocationData that){
        double result = 0.0;

        result += (this.latitude - that.latitude) * (this.latitude - that.latitude);
        result += (this.longitude - that.longitude) * (this.longitude - that.longitude);

        return Math.sqrt(result);
    }

    /*
    //Not useful for real purposes (as it mixes degrees and meters)
    //Used only for debugging
    public double dist3d(LocationData that){
        double result = 0.0;

        result += (this.latitude - that.latitude) * (this.latitude - that.latitude);
        result += (this.longitude - that.longitude) * (this.longitude - that.longitude);
        result += (this.altitude - that.altitude) * (this.altitude - that.altitude);

        return Math.sqrt(result);
    }*/

    /**
     * Compute the distance between two points on the earth by using Haversine's formula
     * @param that
     * @return
     */
    public double arcLengthDist(LocationData that){
        double R = 6371000.0; //6.371 km radius

        //Convert latutide and longitude to radians
        double lat2 = that.latitude * Math.PI / 180;
        double lat1 = this.latitude * Math.PI / 180;
        double lon2 = that.longitude * Math.PI / 180;
        double lon1 = this.longitude * Math.PI / 180;

        //Compute the difference in longitude and latitude in radians
        double dtheta = (lon2 - lon1);
        double dphi = (lat2 - lat1);

        //Haversine argument of the square root
        double a =      Math.sin(dphi/2) * Math.sin(dphi/2) +
                        Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dtheta/2) * Math.sin(dtheta/2);

        //Equivalent to 2*asin(sqrt(a)) (see definition of atan2 and asin)
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        //Scale to the radius of the Earth
        double d = R * c;

        return d;
    }

    /**
     * Return the linear interpolated value from a location to another at time "time".
     * @param that the second object (of type LocationData)
     * @param time the time at which the interpolated data will refer to
     * @return interpolated value
     */
    @Override
    public LocationData interpolate(Object that, double time) {
        if (that instanceof LocationData) {
            //Convert the generic object to location data
            LocationData thatLoc = (LocationData) that;
            LocationData result = new LocationData();

            //Which percent of "that value" should be in the result, for example:
            //0 -> only this value
            //0.5 -> average between this and that value
            //1 -> only that value
            double factor;

            if (thatLoc.getTime() == this.getTime())
                factor = 0.5; //The two values refer to the same time, mix them in equal parts
            else
                //How far is the requested time from the time of "this", in units of the interval between
                //this.time and that.time, for example:
                //0 -> time == this.time
                //1 -> time == that.time
                //0.3 -> time == this.time + 0.3*(that.time - this.time)
                factor = (time - this.getTime()) / (thatLoc.getTime() - this.getTime());

            //Set the latitude, longitude and altitude of the results
            result.setLatitude(this.getLatitude() + factor * (thatLoc.getLatitude() - this.getLatitude()));
            result.setLongitude(this.getLongitude() + factor * (thatLoc.getLongitude() - this.getLongitude()));
            result.setAltitude(this.getAltitude() + factor * (thatLoc.getAltitude() - this.getAltitude()));

            return result;
        }

        //Can not compare something that is not a location to a location
        return null;
    }
}
