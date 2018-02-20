package com.example.manuel.thingseedemo;

/**
 * Created by manuel on 2/19/18.
 */

public class TemperatureData extends DataWithTime {

    double temperature;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public DataWithTime interpolate(Object that, double time) {
        if (that instanceof TemperatureData) {
            TemperatureData thatTemperature = (TemperatureData) that;
            TemperatureData result = new TemperatureData();
            double factor = time / (thatTemperature.getTime() - this.getTime());

            result.setTemperature(this.getTemperature() + factor * (thatTemperature.getTemperature() - this.getTemperature()));

            return result;
        }
        return null;
    }
}
