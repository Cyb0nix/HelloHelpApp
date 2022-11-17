package com.cybonix.hellohelp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseUser fuser;
    DatabaseReference reference;

    public String GoogleMapApiKey;

    int AUTOCOMPLETE_REQUEST_CODE = 1;

    private static final String TAG = MapsActivity.class.getSimpleName();
    private FloatingActionButton nav;

    public String destinationPoint;

    private GoogleMap mMap;
    Marker JustMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        nav = findViewById(R.id.fab_location_nav);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chercher une adresse");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(MapsActivity.this, VilleActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        try {
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), GoogleMapApiKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getGoogleMapApi();

        AdressSearch();

        findViewById(R.id.fab_location_nav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, NavActivity.class);
                intent.putExtra("shop_adresse", destinationPoint);
                MapsActivity.this.startActivity(intent);
            }
        });
    }

    private void AdressSearch() {
        findViewById(R.id.fab_location_search).setOnClickListener(view -> {
            try {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY,
                        Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG))
                        .setCountries(Arrays.asList("FR"))
                        .build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    private void  getGoogleMapApi(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("keys");
        ref.child("googleMapApi").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GoogleMapApiKey = Objects.requireNonNull(snapshot.getValue()).toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                try {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    Log.i(TAG, "Place: " + place.getLatLng());
                    destinationPoint = place.getAddress();
                    LatLng LocChoise = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                    MarkerOptions LocChoiseMarker = new MarkerOptions().position(LocChoise)
                            .title(place.getName());
                    if (JustMarker != null) {
                        JustMarker.remove();
                    }
                    JustMarker = mMap.addMarker(LocChoiseMarker);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(LocChoise));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
                System.out.print(status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
            }

            nav.setVisibility(View.VISIBLE);
        }

    }

    private void status(String status) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }


    @Override
    public void onMapReady(GoogleMap mapgoogle) {
        mMap = mapgoogle;

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(false);
            LatLng LeBlancMesnil = new LatLng(48.938308, 2.460548);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LeBlancMesnil));
            Toast.makeText(this, "localisation non activé", Toast.LENGTH_LONG).show();

        } else {
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng MyPostion = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(MyPostion));
            mMap.setMyLocationEnabled(true);
        }


    }

}