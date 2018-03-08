package com.example.manuel.thingseedemo.util;

import android.support.annotation.Nullable;

import com.example.manuel.thingseedemo.LocationData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by manuel on 3/7/18.
 */

/**
 * Convert from string date to timestamp and from timestamp to string date
 */
public class TimestampDateHandler {
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy, h:mm:ss a");
    @Nullable
    public static Long dateToTimestamp(String date) {
     try {
         return sdf.parse(date).getTime();
     } catch (ParseException ex){
         return null;
     }
    }

    public static String timestampToDate(long timestamp){
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

}
