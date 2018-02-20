package com.example.manuel.thingseedemo.fragments;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.manuel.thingseedemo.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class    History extends Fragment implements View.OnClickListener {

    private View myView;

    // All views in the fragment
    ImageButton FdatePicker, FtimePicker, UdatePicker, UtimePicker;
    TextView FdateText, FtimeText, UdateText, UtimeText;
    Button getHistory;


    String fromDate, fromTime, untilDate,untilTime;

    long startTimeStamp, endTimeStamp;

    java.util.Calendar currentDateTime;


    public History() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_history, container, false);

        FdatePicker = myView.findViewById(R.id.FdatePicker);
        FtimePicker = myView.findViewById(R.id.FtimePicker);
        UdatePicker = myView.findViewById(R.id.UdatePicker);
        UtimePicker = myView.findViewById(R.id.UtimePicker);


        getHistory = myView.findViewById(R.id.historyButton);
        getHistory.setOnClickListener(this);


        FdateText = myView.findViewById(R.id.Fdate);
        FtimeText = myView.findViewById(R.id.Ftime);
        UdateText = myView.findViewById(R.id.Udate);
        UtimeText = myView.findViewById(R.id.Utime);


        FdatePicker.setOnClickListener(this);
        FtimePicker.setOnClickListener(this);
        UdatePicker.setOnClickListener(this);
        UtimePicker.setOnClickListener(this);



        return myView;
    }





    @Override
    public void onClick(View view) {

        int id = view.getId();

        switch (id){
            case R.id.FdatePicker:
                SetDate(id);
                break;
            case R.id.FtimePicker:
                SetTime(id);
                break;
            case R.id.UdatePicker:
               SetDate(id);
                break;
            case R.id.UtimePicker:
                SetTime(id);
                break;
            case R.id.historyButton:
                StartyHistoryMode();
                break;
        }

    }



    private void SetDate(int id) {

        final  int receivedId = id;


        currentDateTime = java.util.Calendar.getInstance();
        int year = currentDateTime.get(java.util.Calendar.YEAR);
        int month = currentDateTime.get(java.util.Calendar.MONTH);
        int day = currentDateTime.get(java.util.Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int y, int m, int d) {

                if(receivedId == R.id.FdatePicker){

                    fromDate = d + "-" + (m+1) + "-" + y;
                    FdateText.setText(fromDate);
                }
                else {

                    untilDate = d + "-" + (m+1) + "-" + y;
                    UdateText.setText(untilDate);
                }


            }
        }, year, month, day);

        datePickerDialog.show();
    }


    private void SetTime(int id) {

        final  int receivedId = id;

        currentDateTime = java.util.Calendar.getInstance();
        int hour = currentDateTime.get(Calendar.HOUR);
        int minute = currentDateTime.get(Calendar.MINUTE);
//        int sec = currentDateTime.get(Calendar.SECOND);
//        int milisec = currentDateTime.get(Calendar.MILLISECOND);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int h, int min) {

                if(receivedId == R.id.FtimePicker){

                    fromTime = h +":" + min;
                    FtimeText.setText(fromTime);
                }
                else {

                    untilTime = h +":" + min;
                    UtimeText.setText(untilTime);
                }

            }
        },hour,minute,false);

        timePickerDialog.show();
    }



    private void StartyHistoryMode()  {
        Date date1,date2;

        DateFormat formatter = new SimpleDateFormat("HH:mm dd-MM-yyyy");

        String s1 = fromTime + " " + fromDate;
        String s2 = untilTime + " " + untilDate;


        try {
            date1 =  (Date) formatter.parse(s1);
            startTimeStamp = date1.getTime();

            date2 = (Date) formatter.parse(s2);
            endTimeStamp = date2.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.d("timestap : ",startTimeStamp+" and "+endTimeStamp);
    }



}
