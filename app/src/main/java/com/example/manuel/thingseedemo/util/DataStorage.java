package com.example.manuel.thingseedemo.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.manuel.thingseedemo.TrackData;
import com.example.manuel.thingseedemo.fragments.Track;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by awetg on 7.3.2018.
 */

public class DataStorage {

    private static Context context;
    private static TrackData trackData;
    private static String cachedFileName = null;



    public static void init(Context c) {
        context = c;
    }

    public static TrackData getTrackData(){
        return trackData;
    }
    public static String getCachedFileName(){
        return cachedFileName;
    }

    public static void setTrackData(TrackData data){
        trackData = data;
    }

    public static void setRealtime(){
        trackData = null;
    }

    public static boolean isRealtime(){
        return trackData == null;
    }

    public static boolean isTrackOrRecording(){
        return !isRealtime();
    }

    public static void setTrackData(TrackData data){
        trackData = data;
    }

    public static void setRealtime(){
        trackData = null;
    }

    public static boolean isRealtime(){
        return trackData == null;
    }

    public static boolean isTrackOrRecording(){
        return !isRealtime();
    }


    public static void storeData(TrackData trackData, String trackName) {
        String s = trackName + ".tk";
        try {


            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir(), s)));
            objectOutputStream.writeObject(trackData);
            objectOutputStream.close();
            Log.d("in data storage", "data saved");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void loadData(String trackName) {
        cachedFileName = trackName;
        String s = trackName + ".tk";
        TrackData tempData = new TrackData();

        try {

            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(context.getFilesDir(),s)));
            tempData = (TrackData) objectInputStream.readObject();
            Log.d("dataStorage :","loaded data");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        trackData = tempData;

    }


    public static ArrayList<String> savedTracksNames() {

        File dir = context.getFilesDir();
        File[] listOfFiles = dir.listFiles();
        ArrayList<String> trackList = new ArrayList<String>();
        String s;

        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                s = listOfFiles[i].getName();
                s = s.substring(0, s.length() - 3);
                Log.d("EXISTING FILE NAME ", s);
                trackList.add(s);
            }
        }

        return trackList;
    }

    public static void deleteTrack(String trackName) {

        File dir = context.getFilesDir();
        File[] listOfFiles = dir.listFiles();
        String tempName = trackName + ".tk";

        String s;

        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {

                s = listOfFiles[i].getName();

                if (s.equals(tempName)){

                    listOfFiles[i].delete();
                    Log.d("DELETE :","file deleted");
                    return;
                }
            }

            Log.d("DELETE :","file not found");

        }
    }
}
