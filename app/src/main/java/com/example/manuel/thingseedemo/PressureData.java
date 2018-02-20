package com.example.manuel.thingseedemo;

/**
 * Created by manuel on 2/19/18.
 */

public class PressureData extends DataWithTime {

    double pressure;

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    @Override
    public DataWithTime interpolate(Object that, double time) {
        if (that instanceof PressureData) {
            PressureData thatPressure = (PressureData) that;
            PressureData result = new PressureData();
            double factor = time / (thatPressure.getTime() - this.getTime());

            result.setPressure(this.getPressure() + factor * (thatPressure.getPressure() - this.getPressure()));

            return result;
        }
        return null;
    }
}
