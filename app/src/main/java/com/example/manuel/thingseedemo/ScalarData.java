package com.example.manuel.thingseedemo;

/**
 * Created by manuel on 2/28/18.
 */

/**
 * Represents location data (for example temperature, speed, pressure, ...) associated with a timestamp
 * (see extends DataWithTime)
 */
public class ScalarData extends DataWithTime {

    //Value of the scalar quantity (can be a pressure, a temperature, a speed, ...)
    private double value;

    /**
     * Set the value of the scalar data
     * @param value
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Retrieve the value of the scalar data
     * @return
     */
    public double getValue() {
        return value;
    }

    /**
     * Return the linear interpolated value from a scalar value to another at time "time".
     * @param that the second object (of type ScalarData)
     * @param time the time at which the interpolated data will refer to
     * @return interpolated value
     */
    @Override
    public ScalarData interpolate(Object that, double time) {
        if (that instanceof ScalarData) {
            ScalarData thatScalar = (ScalarData) that;
            ScalarData result = new ScalarData();

            //Which percent of "that value" should be in the result, for example:
            //0 -> only this value
            //0.5 -> average between this and that value
            //1 -> only that value
            double factor;

            if (thatScalar.getTime() == this.getTime())
                factor = 0.5; //The two values refer to the same time, mix them in equal parts
            else
                //How far is the requested time from the time of "this", in units of the interval between
                //this.time and that.time, for example:
                //0 -> time == this.time
                //1 -> time == that.time
                //0.3 -> time == this.time + 0.3*(that.time - this.time)
                factor = (time - this.getTime()) / (thatScalar.getTime() - this.getTime());


            result.setValue(this.getValue() + factor * (thatScalar.getValue() - this.getValue()));

            return result;
        }
        return null;
    }
}
