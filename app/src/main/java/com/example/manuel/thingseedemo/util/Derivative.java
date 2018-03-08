package com.example.manuel.thingseedemo.util;

/**
 * Created by manuel on 2/28/18.
 */

/**
 * Interface defining a derivative function for a TimeStream
 */
public interface Derivative {
    double derivative(Object x1, Object x2, double dt);
}
