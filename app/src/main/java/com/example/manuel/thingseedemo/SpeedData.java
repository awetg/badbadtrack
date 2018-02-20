package com.example.manuel.thingseedemo;

/**
 * Created by manuel on 2/19/18.
 */

public class SpeedData extends DataWithTime {

    double speed;

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public DataWithTime interpolate(Object that, double time) {
        if (that instanceof SpeedData) {
            SpeedData thatSpeed = (SpeedData) that;
            SpeedData result = new SpeedData();
            double factor = time / (thatSpeed.getTime() - this.getTime());

            result.setSpeed(this.getSpeed() + factor * (thatSpeed.getSpeed() - this.getSpeed()));

            return result;
        }
        return null;
    }
}
