package com.example.manuel.thingseedemo.util;

/**
 * Created by manuel on 2/28/18.
 */

/**
 * Interface defining an integral function for a TimeStream
 */
public interface Integral {
    double integral(double acc, Object x1, Object x2, double dt);
}