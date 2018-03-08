package com.example.manuel.thingseedemo.util;

/**
 * Created by manuel on 2/28/18.
 */

/**
 * Interface defining a map from an object of a TimeStream to a double
 */
public interface TimeStreamMapToScalar {
    double map(Object x);
}
