package com.cybonix.hellohelp.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cybonix.hellohelp.Adapter.DatePickerAdapter;
import com.cybonix.hellohelp.Adapter.TimePickerAdapter;
import com.cybonix.hellohelp.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;


import java.util.Arrays;


public class FilterFragment extends Fragment implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{

    Button filtrer, lieu_depart, heure, date, lieu_arrivee;
    NumberPicker nbr_places;
    public String AdresStart, heureChois, Places_Nmbr, dateChois,AdresEnd,datefil;
    public boolean AdresStart1, AdresEnd1;

    public String GoogleMapApiKey = "AIzaSyAa5DTvuXyVcXZfkoJMvMd6ZaMpAS3Mzzs";

    int AUTOCOMPLETE_REQUEST_CODE = 1;

    private static final String TAG = FilterFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_filter, container, false);

        filtrer = view.findViewById(R.id.filtrer);
        lieu_depart = view.findViewById(R.id.lieu_départ2);
        lieu_arrivee = view.findViewById(R.id.lieu_arrive2);
        heure = view.findViewById(R.id.heure2);
        date = view.findViewById(R.id.date2);
        nbr_places = view.findViewById(R.id.places2);

        nbr_places.setMinValue(1);
        nbr_places.setMaxValue(10);
        nbr_places.setValue(2);
        nbr_places.setWrapSelectorWheel(false);

        getFilterPreferences();

        StartAdres();
        EndAdres();

        nbr_places.setOnValueChangedListener((picker, oldVal, newVal) -> Places_Nmbr = Integer.toString(newVal));


        filtrer.setOnClickListener(v -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(getString(R.string.depart_lieu), AdresStart);
            editor.commit();

            editor.putString(getString(R.string.arrivee_lieu), AdresEnd);
            editor.commit();

            editor.putString(getString(R.string.heure), heureChois);
            editor.commit();

            editor.putString(getString(R.string.date), datefil);
            editor.commit();

            editor.putString(getString(R.string.nbr_places), Places_Nmbr);
            editor.commit();

            if (TextUtils.isEmpty(AdresStart) && TextUtils.isEmpty(AdresEnd) && TextUtils.isEmpty(dateChois) && TextUtils.isEmpty(Places_Nmbr)){
                Toast.makeText(getContext(), "Veuillez choisir au moins un élement.",Toast.LENGTH_SHORT);
            }else {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,new AdvancedSearchFragment()).commit();

            }

        });

        heure.setOnClickListener(v -> {
            DialogFragment timePicker = new TimePickerAdapter();
            timePicker.setTargetFragment(FilterFragment.this, 0);
            timePicker.show(getActivity().getSupportFragmentManager(), "time picker");
        });

        date.setOnClickListener(v -> {
            DialogFragment datePicker = new DatePickerAdapter();
            datePicker.setTargetFragment(FilterFragment.this, 0);
            datePicker.show(getActivity().getSupportFragmentManager(), "datePicker");
        });

        try {
            if (!Places.isInitialized()) {
                Places.initialize(getActivity().getApplicationContext(), GoogleMapApiKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void getFilterPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        String addressedep = preferences.getString(getString(R.string.depart_lieu), "");
        String datedep = preferences.getString(getString(R.string.date), "");
        String heuredep = preferences.getString(getString(R.string.heure), "");
        String nbr_places1 = preferences.getString(getString(R.string.nbr_places), "");
        String end = preferences.getString(getString(R.string.arrivee_lieu),"");

        lieu_depart.setText("Lieu de départ: " + addressedep);
        heure.setText("Heure de départ: " + heuredep);
        date.setText("Date: " + datedep);
        lieu_arrivee.setText("Lieu de arrivee: " + end);


        if (!nbr_places1.isEmpty()) {
            nbr_places.setValue(Integer.parseInt(nbr_places1));
        }

    }

    private void StartAdres() {
        lieu_depart.setOnClickListener(view -> {
            AdresStart1 = true;
            try {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN,
                        Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS))
                        .setCountries(Arrays.asList("FR"))
                        .build(this.getContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void EndAdres() {
        lieu_arrivee.setOnClickListener(view -> {
            AdresEnd1 = true;
            try {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN,
                        Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS))
                        .setCountries(Arrays.asList("FR"))
                        .build(this.getContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (AdresStart1) {

            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        AdresStart = place.getName() + "; " + place.getAddress();
                        lieu_depart.setText("Lieu de départ: " + AdresStart);
                    } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                        Status status = Autocomplete.getStatusFromIntent(data);
                    } else if (resultCode == Activity.RESULT_CANCELED) {

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (AdresEnd1) {

            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        AdresEnd = place.getName() + "; " + place.getAddress();
                        lieu_arrivee.setText("Lieu de arrivé: " + AdresEnd);
                    } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                        Status status = Autocomplete.getStatusFromIntent(data);
                    } else if (resultCode == Activity.RESULT_CANCELED) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        AdresStart1 = false;
        AdresEnd1 = false;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String minutes = String.valueOf(minute);
        if (minutes.length() < 2) {
            minutes = "0" + minute;
        }
        heureChois = hourOfDay + ";" + minutes;
        heure.setText("Heure de départ: " + heureChois);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String months = String.valueOf(month + 1);
        if (months.length() < 2) {
            months = "0" + months;
        }

        String days = String.valueOf(day);
        if (days.length() < 2) {
            days = "0" + day;
        }

        dateChois = days + "/" + months;
        datefil = days + "∕"+ months;
        date.setText("Date: " + dateChois);
    }
}