package com.example.manuel.thingseedemo;

/**
 * Created by manuel on 2/28/18.
 */

public class ScalarData extends DataWithTime {

    private double value;


    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public ScalarData interpolate(Object that, double time) {
        if (that instanceof ScalarData) {
            ScalarData thatScalar = (ScalarData) that;
            ScalarData result = new ScalarData();

            double factor = (time - this.getTime()) / (thatScalar.getTime() - this.getTime());

            result.setValue(this.getValue() + factor * (thatScalar.getValue() - this.getValue()));

            return result;
        }
        return null;
    }
}
