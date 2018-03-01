package com.example.manuel.thingseedemo;

/**
 * Created by manuel on 2/28/18.
 */

public class CalculusTools {

    public static Integral scalarPlainIntegral = new Integral() {
        @Override
        public double integral(double acc, Object x1, Object x2, double dt) {
            return acc + ((double)x2 - (double)x1)*dt;
        }
    };

    public static Integral vectorDistIntegral = new Integral() {
        @Override
        public double integral(double acc, Object x1, Object x2, double dt) {
            return acc + ((LocationData) x1).dist2d((LocationData) x2);
        }
    };

    public static Integral vectorArcDistIntegral = new Integral() {
        @Override
        public double integral(double acc, Object x1, Object x2, double dt) {
            return acc + ((LocationData) x1).arcLengthDist((LocationData) x2);
        }
    };

}
