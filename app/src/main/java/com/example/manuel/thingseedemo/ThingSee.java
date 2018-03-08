/*
 * ThingSee.java -- ThingSee Cloud server client
 *
 * API documentation:
 *  http://api.thingsee.com/doc/rest
 *  https://thingsee.zendesk.com/hc/en-us/articles/205188962-Thingsee-Property-API-
 *  https://thingsee.zendesk.com/hc/en-us/articles/205188982-Thingsee-Events-API-
 * Web interface:
 *  http://app.thingsee.com
 *
 * Copyright (C) 2017 by ZyMIX Oy. All rights reserved.
 * Author(s): Jarkko Vuori
 * Modification(s):
 *   First version created on 04.02.2017
 */
package com.example.manuel.thingseedemo;

import android.location.Location;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.json.*;

/**
 * API to connect to ThingSeeOne and retrieve data in the form of TimeStream
 */
public class ThingSee {
    private final static String charset = "UTF-8";
    private final static String url     = "http://api.thingsee.com/v2";

    private URLConnection connection;
    private String        accountAuthUuid;
    private String        accountAuthToken;
    private Boolean       fConnection;
    private Boolean       isFake = false;

    /**
     * Authenticates the user
     * <p>
     * Credentials returned from the cloud server are used in the following method calls
     * (so that it is not needed to send email/password information every time to the server).
     *
     * @param email      User's email address used for the authentication
     * @param passwd     User's password
     * @throws Exception Gives an exception with text information if there was an error
     */
    public ThingSee(String email, String passwd) throws Exception {
        JSONObject param = new JSONObject();

        Log.d("INFO", "ThingSee trying to log in with " + email + " " + passwd);

        param.put("email", email);
        param.put("password", passwd);

        JSONObject resp = getThingSeeObject(param, "/accounts/login");
        accountAuthUuid = resp.getString("accountAuthUuid");
        accountAuthToken = resp.getString("accountAuthToken");

        Log.d("INFO", "ThingSee account log in " + email + " " + passwd + " " + accountAuthUuid);
    }

    /**
     * Send a request to the ThingSee server at the subpath
     * <p>
     * Authentication is supposed to have been done before (using a constructor)
     *
     * @param  request   Request parameter (optional, null if not needed)
     * @param  path      URI-name for the object to be requested
     * @return           Requested object in JSON-format
     * @throws Exception Gives an exception with text information if there was an error
     */
    private JSONObject getThingSeeObject(JSONObject request, String path) throws Exception {
        JSONObject     resp     = null;
        InputStream    response = null;
        BufferedReader reader   = null;


        fConnection = false;
        try {
            Log.d("NET", "Trying to connect to " + url + path);
            connection = new URL(url + path).openConnection();
            Log.d("NET", "Connection is null:" + (connection == null));
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/json;charset=" + charset);
            if (accountAuthToken != null)
                connection.setRequestProperty("Authorization", "Bearer " + accountAuthToken);

            // send a request (if needed)
            if (request != null) {
                Log.d("NET", "Sending request");
                connection.setDoOutput(true);   // Triggers HTTP POST request
                Log.d("NET", "Output enabled, connection is null: " + (connection == null));
                OutputStream output = connection.getOutputStream();
                Log.d("NET", "Output stream is null: " + (output == null));
                output.write(request.toString().getBytes(charset));
            }

            // wait for the reply
            response = connection.getInputStream();
            Log.d("NET", "Response is null:" + (response == null));
            reader = new BufferedReader(new InputStreamReader(response));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            //System.out.println("Responce: " + out.toString());
            resp = new JSONObject(out.toString());
            Log.d("NET", "Response string built");
            fConnection = true;
        } catch (Exception ex) {
            Log.d("NET", "Error " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            // ensure that streams are closed in all situations
            try {
                if (response != null)
                    response.close();
                if (reader != null)
                    reader.close();

                Log.d("NET", "Stream closed");
            } catch (IOException ioe) {
                Log.d("NET", "Error " + ioe.getMessage());
            }
        }

        return (resp);
    }

    /**
     * Request the handle to the first ThingSee device at the cloud account
     * <p>
     * Cloud account may have multiple devices, this function selects always the first device
     *
     * @return Return JSON decription of the selected device
     */
    public JSONObject Devices() {
        JSONObject item, resp;

        try {
            resp = getThingSeeObject(null, "/devices");
            JSONArray devices = (JSONArray) resp.get("devices");
            item = devices.getJSONObject(0);
        } catch (Exception e) {
            Log.d("THINGSEE", "No Thingsee device");
            item = null;
        }

        return (item);
    }

    /**
     * Request all device events
     * <p>
     * Every device has an event log. This method read the given devices event log.
     *
     * @param  device    Device JSON description (given by Devices() method
     * @param  start     Initial timestamp
     * @param  end       Ending timestamp
     * @return           Events in JSON format
     * @throws Exception Gives an exception with text information if there was an error
     */
    public JSONArray Events(JSONObject device, long start, long end) throws Exception {
        JSONObject resp;
        JSONArray  events;

        try {
            resp = getThingSeeObject(null, "/events/" + device.getString("uuid") + "?type=sense&start=" + start + "&end=" + end);
            events  = (JSONArray)resp.get("events");
        } catch (Exception e) {
            Log.d("THINGSEE", "ThingseeEvents error " + e);
            throw new Exception("No events");
        }

        return (events);
    }

    /**
     * Request all device events
     * <p>
     * Every device has an event log. This method read the given devices event log.
     *
     * @param  device    Device JSON description (given by Devices() method
     * @param  start     Initial timestamp
     * @return           Events in JSON format
     * @throws Exception Gives an exception with text information if there was an error
     */
    public JSONArray Events(JSONObject device, long start) throws Exception {
        JSONObject resp;
        JSONArray  events;

        try {
            resp = getThingSeeObject(null, "/events/" + device.getString("uuid") + "?type=sense&start=" + start);
            events  = (JSONArray)resp.get("events");
            Log.d("INFO", "GET request to /events/" + device.getString("uuid") + "?type=sense&start=" + start );
        } catch (Exception e) {
            Log.d("THINGSEE", "ThingseeEvents error " + e);
            throw new Exception("No events");
        }

        return (events);
    }

    /**
     * Request all device events
     * <p>
     * Every device has an event log. This method read the given devices event log.
     *
     * @param  device    Device JSON description (given by Devices() method
     * @param  limit     Limits the amount of events to request
     * @return           Events in JSON format
     * @throws Exception Gives an exception with text information if there was an error
     */
    public JSONArray Events(JSONObject device, int limit) throws Exception {
        JSONObject resp;
        JSONArray  events;

        try {
            resp = getThingSeeObject(null, "/events/" + device.getString("uuid") + "?limit=" + limit);
            events  = (JSONArray)resp.get("events");
        } catch (Exception e) {
            Log.d("THINGSEE", "ThingseeEvents error " + e);
            throw new Exception("No events");
        }

        return (events);
    }


    /* senseID groupID field */
    private static final int GROUP_LOCATION     = 0x01 << 16;
    private static final int GROUP_SPEED        = 0x02 << 16;
    private static final int GROUP_ENERGY       = 0x03 << 16;
    private static final int GROUP_ORIENTATION  = 0x04 << 16;
    private static final int GROUP_ACCELERATION = 0x05 << 16;
    private static final int GROUP_ENVIRONMENT  = 0x06 << 16;
    private static final int GROUP_HW_KEYS      = 0x07 << 16;

    /* senseID propertyID field */
    private static final int PROPERTY1          = 0x01 << 8;
    private static final int PROPERTY2          = 0x02 << 8;
    private static final int PROPERTY3          = 0x03 << 8;
    private static final int PROPERTY4          = 0x04 << 8;

    //Constants added by Manuel

    //Group Location
    private static final int LATITUDE          = PROPERTY1;
    private static final int LONGITUDE          = PROPERTY2;
    private static final int ALTITUDE          = PROPERTY3;
    private static final int ACCURACY          = PROPERTY4;

    //Group Speed
    private static final int SPEED          = PROPERTY1;

    //Group Energy
    private static final int BATTERY_LEVEL          = PROPERTY2;

    //Group Acceleration (Impact)
    private static final int IMPACT          = PROPERTY4;

    //Group Environment
    private static final int TEMPERATURE          = PROPERTY1;
    private static final int HUMIDITY          = PROPERTY2;
    private static final int PRESSURE          = PROPERTY4;


    //Public Enum for sensor type
    public static final int LOCATION_DATA = 1;
    public static final int SPEED_DATA = 2;
    public static final int PRESSURE_DATA = 3;
    public static final int BATTERY_DATA = 4;
    public static final int TEMPERATURE_DATA = 5;
    public static final int IMPACT_DATA = 6;


    /**
     * Get a TimeStream of location data from the events
     * @param events JSONArray containing a list of events retrieved from ThingSeeOne
     * @param outOfBoundMarginTime how much time after the last viable data the constructed stream will return null data
     * @return TimeStream of the locations contained in events
     * @throws Exception
     */
    public TimeStream<LocationData> getLocationStream(JSONArray events, long outOfBoundMarginTime) throws Exception {
        //Create an empty resulting stream
        TimeStream<LocationData> stream = new TimeStream<>(outOfBoundMarginTime);

        //Go through every real event, or if ThingSee is set to return fake data, through 10 fake events
        for (int i = 0; i < (isFake ? 10 : events.length()); i++) {
            try {
                //Get the real event or null if we are generating a fake event
                JSONObject event = isFake ? null : events.getJSONObject(i);
                //Get the location data from the event
                LocationData data = (LocationData)getEventData(event, LOCATION_DATA);
                if (data != null)
                    //If the event contained valid location data, add it to the resulting stream
                    stream.addSample(data);

            } catch (Exception e) {
                //If there is an error, ignore the current event
            }
        }

        return stream;
    }

    /**
     * Get a TimeStream of scalar data from the events
     * @param events JSONArray containing a list of events retrieved from ThingSeeOne
     * @param outOfBoundMarginTime how much time after the last viable data the constructed stream will return null data
     * @param type One of ThingSee.TEMPERATURE_DATA, ThingSee.IMPACT_DATA, ThingSee.PRESSURE_DATA, ThingSee.BATTERY_DATA, ThingSee.SPEED_DATA
     * @return TimeStream of the locations contained in events
     * @throws Exception
     */
    public TimeStream<ScalarData> getScalarStream(JSONArray events, int type, long outOfBoundMarginTime) throws Exception {
        //Create an empty resulting stream
        TimeStream<ScalarData> stream = new TimeStream<>(outOfBoundMarginTime);

        //Go through every real event, or if ThingSee is set to return fake data, through 10 fake events
        for (int i = 0; i < (isFake ? 10 : events.length()); i++) {
            try {
                //Get the real event or null if we are generating a fake event
                JSONObject event = isFake ? null : events.getJSONObject(i);
                //Get the location data from the event
                ScalarData data = (ScalarData) getEventData(event, type);
                if (data != null)
                    //If the event contained valid scalar data, add it to the resulting stream
                    stream.addSample(data);

            } catch (Exception e) {
                //If there is an error, ignore the current event
            }
        }

        return stream;
    }

    /**
     * Get a DataWithTime of type ScalarData (and more specifically temperature, pressure, ...) or
     * location data if the current event contains relevant value.
     * @param event the event that might contain data of type "type", or null if in fake data mode
     * @param type the type of data to try to extract from the event
     * @return the value extracted from the event, or null in case the event does not contain data of type "type"
     * @throws Exception
     */
    private DataWithTime getEventData(JSONObject event, int type) throws Exception {

        //The data we will return
        DataWithTime data = null;

        //If we are in fake data mode, return fake data, don't care about the real event
        if (isFake)
            return getEventFakeData(type);

        try {
            //Data that could be extracted
            Double latitude = null, longitude = null, altitude = null, temperature = null,
                    pressure = null, impact = null, speed = null, battery = null;

            //Timestamp of the event
            long time = event.getLong("timestamp");
            //Extract the values contained in the event
            JSONArray senses = event.getJSONObject("cause").getJSONArray("senses");
            //For each value...
            for (int j = 0; j < senses.length(); j++) {
                //... get a reference to it ...
                JSONObject sense   = senses.getJSONObject(j);
                //... get its type ...
                int        senseID = Integer.decode(sense.getString("sId"));
                //... get its actual value as a real number
                double     value   = sense.getDouble("val");

                //Depending on the value of the event, fill the appropriate container variable
                switch (senseID) {
                        case GROUP_LOCATION | LATITUDE:
                            latitude = value;
                            break;

                        case GROUP_LOCATION | LONGITUDE:
                            longitude = value;
                            break;

                        case GROUP_LOCATION | ALTITUDE:
                            altitude = value;
                            break;

                        case GROUP_ENVIRONMENT | TEMPERATURE:
                            temperature = value;
                            break;

                        case GROUP_ENVIRONMENT | PRESSURE:
                            pressure = value;
                            break;


                        case GROUP_ACCELERATION | IMPACT:
                            impact = value;
                            break;

                        case GROUP_SPEED | SPEED:
                            speed = value;
                            break;

                        case GROUP_ENERGY | BATTERY_LEVEL:
                            battery = value;
                            break;
                        }

                    }

                    //Create the appopriate Data depending on the requested type
                    if (type == LOCATION_DATA && latitude != null && longitude != null){
                        LocationData loc = new LocationData();
                        loc.setLongitude(longitude);
                        loc.setLatitude(latitude);
                        if (altitude == null)
                            altitude = 0.0;
                        loc.setAltitude(altitude);
                        data = loc;
                    } else if (type == IMPACT_DATA && impact != null){
                        ScalarData imp = new ScalarData();
                        imp.setValue(impact);
                        data = imp;
                    } else if (type == BATTERY_DATA && battery != null) {
                        ScalarData bat = new ScalarData();
                        bat.setValue(battery);
                        data = bat;
                    } else if (type == TEMPERATURE_DATA && temperature != null) {
                        ScalarData temp = new ScalarData();
                        temp.setValue(temperature);
                        //Log.d("TempData", "Got temperature data " + temperature);
                        data = temp;
                    }else if (type == PRESSURE_DATA && pressure != null) {
                        ScalarData p = new ScalarData();
                        p.setValue(pressure);
                        data = p;
                    }else if (type == SPEED_DATA && speed != null) {
                        ScalarData sp = new ScalarData();
                        sp.setValue(speed);
                        data = sp;
                    }

                    //Finally set the timestamp
                    if (data != null)
                        data.setTime(time);

        } catch (Exception e) {
            throw new Exception("Error in retrieving data");
        }

        return data;
    }


    /**
     * Like getEventData, but the data is fake (randomly generated)
     * @param type the type of data to get
     * @return
     * @throws Exception
     */
    private DataWithTime getEventFakeData(int type) throws Exception {

        DataWithTime data = null;

        Double latitude = null, longitude = null, altitude = null, temperature = null,
                pressure = null, impact = null, speed = null, battery = null;

        Random rnd = new Random();

        latitude = rnd.nextDouble()/100000 + 60;
        longitude = rnd.nextDouble()/100000 + 25;
        altitude = rnd.nextDouble() * 10 + 100;
        impact = rnd.nextDouble() + 1;
        speed = rnd.nextDouble() * 10;
        battery = 89.0;
        temperature = rnd.nextDouble()*3 + 20;
        pressure = rnd.nextDouble()*20 + 1000;


        if (type == LOCATION_DATA && latitude != null && longitude != null){
            LocationData loc = new LocationData();
            loc.setLongitude(longitude);
            loc.setLatitude(latitude);
            if (altitude == null)
                altitude = 0.0;
            loc.setAltitude(altitude);
            data = loc;
        } else if (type == IMPACT_DATA && impact != null){
            ScalarData imp = new ScalarData();
            imp.setValue(impact);
            data = imp;
        } else if (type == BATTERY_DATA && battery != null) {
            ScalarData bat = new ScalarData();
            bat.setValue(battery);
            data = bat;
        } else if (type == TEMPERATURE_DATA && temperature != null) {
            ScalarData temp = new ScalarData();
            temp.setValue(temperature);
            Log.d("TempData", "Got temperature data " + temperature);
            data = temp;
        }else if (type == PRESSURE_DATA && pressure != null) {
            ScalarData p = new ScalarData();
            p.setValue(pressure);
            data = p;
        }else if (type == SPEED_DATA && speed != null) {
            ScalarData sp = new ScalarData();
            sp.setValue(speed);
            data = sp;
        }

        long time = System.currentTimeMillis();

        if (data != null)
            data.setTime(time);

        return data;
    }

    /**
     * Set the ThingSee object to use fake randomly generated data (for debugging purposes)
     */
    public void setFake(){
        isFake = true;
    }

    /**
     * Set the ThingSee object to use the real ThingSeeOne cloud data (active by default)
     */
    public void setReal(){
        isFake = false;
    }


    @Override
    public String toString() {
        String s;

        if (fConnection)
            s = "Uuid: " + accountAuthUuid + "\nToken: " + accountAuthToken;
        else
            s = "Not authenticated";

        return (s);
    }

    /**
     * Convert events to string
     * <p>
     * Converts timestamp and senses information of the event to string
     *
     * @param  events Events in JSON format
     * @return        Events in string format
     */
    public String toString(JSONArray events) {
        StringBuilder s = new StringBuilder();
        String        ss;

        try {
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);

                s.append(new Date(event.getLong("timestamp")) + ": ");
                //System.out.println("    type: " + event.getString("type"));
                JSONArray senses = event.getJSONObject("cause").getJSONArray("senses");
                for (int j = 0; j < senses.length(); j++) {
                    JSONObject sense = senses.getJSONObject(j);

                    s.append("sId " + sense.getString("sId") + ": " + sense.getDouble("val") + ",");
                }
                s.append("\n");
            }
            ss = s.toString();
        } catch (Exception e) {
            ss = null;
        }

        return ss;
    }
}