package com.example.manuel.thingseedemo;

/**
 * Created by manuel on 2/19/18.
 */

public class BatteryData extends DataWithTime {

    double battery;

    public double getBattery() {
        return battery;
    }

    public void setBattery(double battery) {
        this.battery = battery;
    }

    @Override
    public DataWithTime interpolate(Object that, double time) {
        if (that instanceof BatteryData) {
            BatteryData thatBattery = (BatteryData) that;
            BatteryData result = new BatteryData();
            double factor = time / (thatBattery.getTime() - this.getTime());

            result.setBattery(this.getBattery() + factor * (thatBattery.getBattery() - this.getBattery()));

            return result;
        }
        return null;
    }
}
