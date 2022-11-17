package com.cybonix.hellohelp.Adapter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;


public class TimePickerAdapter extends DialogFragment {

    private TimePickerDialog.OnTimeSetListener timeSetListener;
 
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        timeSetListener = (TimePickerDialog.OnTimeSetListener)getTargetFragment();

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), timeSetListener, hour, minute, DateFormat.is24HourFormat(getActivity()));

        return timePickerDialog;
    }
}