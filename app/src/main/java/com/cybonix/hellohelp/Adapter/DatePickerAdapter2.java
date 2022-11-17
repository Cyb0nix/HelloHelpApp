package com.cybonix.hellohelp.Adapter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

public class DatePickerAdapter2 extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog dpd = new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
        DatePicker dp = dpd.getDatePicker();
        dp.setMinDate(c.getTimeInMillis());
        c.add(Calendar.DAY_OF_MONTH,14);
        dp.setMaxDate(c.getTimeInMillis());
        return dpd;
    }

}

