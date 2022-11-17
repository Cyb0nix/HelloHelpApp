package com.cybonix.hellohelp.Adapter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Objects;

import androidx.fragment.app.DialogFragment;

public class DatePickerAdapter extends DialogFragment {
    private DatePickerDialog.OnDateSetListener dateSetListener; // listener object to get calling fragment listener

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        dateSetListener = (DatePickerDialog.OnDateSetListener)getTargetFragment(); // getting passed fragment
        DatePickerDialog dpd = new DatePickerDialog(requireActivity(), dateSetListener, year, month, day);
        DatePicker dp = dpd.getDatePicker();
        dp.setMinDate(c.getTimeInMillis());
        c.add(Calendar.DAY_OF_MONTH,14);
        dp.setMaxDate(c.getTimeInMillis());
        return dpd;
    }

}
