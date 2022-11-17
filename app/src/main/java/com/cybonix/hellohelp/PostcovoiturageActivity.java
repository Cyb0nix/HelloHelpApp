package com.cybonix.hellohelp;

import android.annotation.SuppressLint;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cybonix.hellohelp.Adapter.DatePickerAdapter2;
import com.cybonix.hellohelp.Adapter.TimePickerAdapter2;
import com.cybonix.hellohelp.Model.Covoiturage;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PostcovoiturageActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    ImageView close;
    Button post, lieu_depart, lieu_arrive, heure, date;
    NumberPicker nbr_places;

    public String AdresStart, AdresEnd, heureChois, Places_Nmbr, dateChois;
    public boolean AdresStart1, AdresEnd1;
    private int nbr;

    private List<Covoiturage> covoiturageList;

    public String apiKey = "AIzaSyAa5DTvuXyVcXZfkoJMvMd6ZaMpAS3Mzzs";
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = PostcovoiturageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postcovoiturage);
        StartAdres();
        EndAdres();
        close = findViewById(R.id.close);
        post = findViewById(R.id.post);
        lieu_depart = findViewById(R.id.lieu_départ1);
        lieu_arrive = findViewById(R.id.lieu_arrivé1);
        heure = findViewById(R.id.heure1);
        date = findViewById(R.id.date1);
        nbr_places = findViewById(R.id.places);

        covoiturageList = new ArrayList<>();

        nbr_places.setMinValue(1);
        nbr_places.setMaxValue(9);
        nbr_places.setValue(2);
        nbr_places.setWrapSelectorWheel(false);

        getPostNbr();

        nbr_places.setOnValueChangedListener((picker, oldVal, newVal) -> Places_Nmbr = Integer.toString(newVal));

        close.setOnClickListener(view -> startActivity(new Intent(PostcovoiturageActivity.this, CovoiturageActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        post.setOnClickListener(view -> {
            if (AdresStart == null||AdresEnd==null||heureChois==null||Places_Nmbr==null||dateChois==null){
                Toast.makeText(PostcovoiturageActivity.this, "Tous les champs doivent être complétés", Toast.LENGTH_SHORT).show();
            }else{
                if (nbr >= 5){
                    Toast.makeText(PostcovoiturageActivity.this, "Vous avez atteint votre quota d'annonces. Veuillez en supprimer pour en poster de nouvelles.", Toast.LENGTH_LONG).show();
                }else {
                    PostAnnonce();
                }
            }
        });

        heure.setOnClickListener(v -> {
            DialogFragment timePicker = new TimePickerAdapter2();
            timePicker.show(getSupportFragmentManager(), "time picker");
        });

        date.setOnClickListener(v -> {
            DialogFragment datePicker = new DatePickerAdapter2();

            datePicker.show(getSupportFragmentManager(), "datePicker");
        });

        try {
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), apiKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void StartAdres() {
        findViewById(R.id.lieu_départ1).setOnClickListener(view -> {
            AdresStart1 = true;
            try {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN,
                        Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS))
                        .setCountries(Arrays.asList("FR"))
                        .build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void EndAdres() {
        findViewById(R.id.lieu_arrivé1).setOnClickListener(view -> {
            AdresEnd1 = true;
            try {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN,
                        Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS))
                        .setCountries(Arrays.asList("FR"))
                        .build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (AdresStart1) {

            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                try {
                    if (resultCode == RESULT_OK) {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        AdresStart = place.getName() + "; " + place.getAddress();
                        lieu_depart.setText("Lieu de départ: " + AdresStart);
                    } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                        Status status = Autocomplete.getStatusFromIntent(data);
                    } else if (resultCode == RESULT_CANCELED) {

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (AdresEnd1) {

            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                try {
                    if (resultCode == RESULT_OK) {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        AdresEnd = place.getName() + "; " + place.getAddress();
                        lieu_arrive.setText("Lieu de arrivé: " + AdresEnd);
                    } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                        Status status = Autocomplete.getStatusFromIntent(data);
                    } else if (resultCode == RESULT_CANCELED) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        AdresStart1 = false;
        AdresEnd1 = false;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String minutes = String.valueOf(minute);
        if (minutes.length() < 2) {
            minutes = "0" + minute;
        }
        heureChois = hourOfDay + ";" + minutes;
        heure.setText("Heure de départ: " + hourOfDay+":"+minutes);
    }

    @SuppressLint("SetTextI18n")
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
        date.setText("Date: " + dateChois);
    }

    private void getPostNbr(){

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Covoiturage");

        assert firebaseUser != null;
        reference.orderByChild("publisher").equalTo(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                covoiturageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Covoiturage covoiturage = snapshot.getValue(Covoiturage.class);
                    covoiturageList.add(covoiturage);
                }

                nbr = covoiturageList.size();
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });
    }

    private void PostAnnonce() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Envoi");
        pd.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Covoiturage");

        String postcovoiturageid = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("postid", postcovoiturageid);
        hashMap.put("lieu_depart", AdresStart);
        hashMap.put("lieu_arrive", AdresEnd);
        hashMap.put("heure", heureChois);
        hashMap.put("date", dateChois);
        hashMap.put("nbr_places", Places_Nmbr);
        hashMap.put("publisher", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        assert postcovoiturageid != null;
        reference.child(postcovoiturageid).setValue(hashMap);

        pd.dismiss();

        startActivity(new Intent(PostcovoiturageActivity.this, CovoiturageActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

    }

}
