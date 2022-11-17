package com.cybonix.hellohelp.Fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cybonix.hellohelp.Adapter.ShopAdapter;
import com.cybonix.hellohelp.Model.Shop;
import com.cybonix.hellohelp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.turf.TurfMeasurement;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class RestaurantsFragment extends Fragment {

    private List<Shop> shopList, shopList2;
    private ShopAdapter shopAdapter, shopAdapter2;

    private ProgressBar progress_circular;
    final String TAG = "RestaurantsFragment";

    private Point user_location;
    private double distance;

    private FusedLocationProviderClient fusedLocationClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurants, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);

        recyclerView.setLayoutManager(linearLayoutManager);
        shopList = new ArrayList<>();
        shopAdapter = new ShopAdapter(requireActivity().getApplicationContext(), shopList);
        recyclerView.setAdapter(shopAdapter);

        RecyclerView recyclerView2 = view.findViewById(R.id.recycler_view2);
        recyclerView2.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        recyclerView2.setLayoutManager(linearLayoutManager2);
        shopList2 = new ArrayList<>();
        shopAdapter2 = new ShopAdapter(requireActivity().getApplicationContext(), shopList2);
        recyclerView2.setAdapter(shopAdapter2);

        progress_circular = view.findViewById(R.id.progress_circular);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());


        readShops();

        return view;
    }

    private void readShops(){

        lastLocation();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Lieu").child("Restaurants");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                shopList.clear();
                for (DataSnapshot snapshot : Objects.requireNonNull(dataSnapshot).getChildren()){
                    Shop shop = snapshot.getValue(Shop.class);
                    if(shopList.size() < 50 ){
                        assert shop != null;
                        distancesShop(shop);

                    }else break;
                }

                shopAdapter.notifyDataSetChanged();
                progress_circular.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });
    }

    private void distancesShop(Shop shop){
        Timber.e("shop act");
        String MAPBOX_KEY = "pk.eyJ1IjoiY3lib25peCIsImEiOiJjazZ2MWpvMDQwZXkxM2ZueGszaDUxbWwxIn0.LuDRemrLg05wh7eTyaDtpg";
        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(MAPBOX_KEY)
                .query(shop.getAdresse())
                .build();

        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(@NotNull Call<GeocodingResponse> call, @NotNull Response<GeocodingResponse> response) {

                assert response.body() != null;
                List<CarmenFeature> results = response.body().features();

                if (results.size() > 0) {

                    Point firstResultPoint = results.get(0).center();
                    if (user_location != null){
                        assert firstResultPoint != null;
                        distance = TurfMeasurement.distance(firstResultPoint,user_location);
                        shop.setDistance(String.valueOf(distance));
                        Log.e(TAG, String.valueOf(distance));
                        if (distance < 0.5){
                            shopList.add(shop);
                            Log.e(TAG, shop.getNom());
                            shopAdapter.notifyDataSetChanged();
                        }else {
                            shopList2.add(shop);
                            shopAdapter2.notifyDataSetChanged();
                        }
                    }else {
                        Timber.e("Location null");
                    }
                }
            }

            @SuppressLint("TimberArgCount")
            @Override
            public void onFailure(@NotNull Call<GeocodingResponse> call, @NotNull Throwable throwable) {
                throwable.printStackTrace();
                Timber.e(throwable, "onResponse: %s");
            }
        });


    }

    @SuppressLint("MissingPermission")
    private void lastLocation(){

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        user_location = Point.fromLngLat(location.getLongitude(),location.getLatitude());
                    }
                });

    }


}
