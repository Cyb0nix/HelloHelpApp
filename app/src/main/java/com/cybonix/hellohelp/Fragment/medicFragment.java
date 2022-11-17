package com.cybonix.hellohelp.Fragment;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cybonix.hellohelp.Adapter.ShopAdapter;
import com.cybonix.hellohelp.Model.Shop;
import com.cybonix.hellohelp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.List;


public class medicFragment extends Fragment {
    private RecyclerView recyclerView, recyclerView2;
    private List<Shop> shopList, shopList2;
    private ShopAdapter shopAdapter, shopAdapter2;

    private static String MAPBOX_KEY;

    private ProgressBar progress_circular;

    private Point user_location;
    private double distance;

    private FusedLocationProviderClient fusedLocationClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medic, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(linearLayoutManager);
        shopList = new ArrayList<>();
        shopAdapter = new ShopAdapter(getActivity().getApplicationContext(), shopList);
        recyclerView.setAdapter(shopAdapter);

        recyclerView2 = view.findViewById(R.id.recycler_view2);
        recyclerView2.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        recyclerView2.setLayoutManager(linearLayoutManager2);
        shopList2 = new ArrayList<>();
        shopAdapter2 = new ShopAdapter(getActivity().getApplicationContext(), shopList2);
        recyclerView2.setAdapter(shopAdapter2);

        progress_circular = view.findViewById(R.id.progress_circular);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        getMapBoxKey();
        readShops();
        // Inflate the layout for this fragment
        return view;
    }

    private void  getMapBoxKey(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("keys");
        ref.child("MAPBOX_KEY").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MAPBOX_KEY = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readShops(){

        lastLocation();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Lieu").child("Medical");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                shopList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Shop shop = snapshot.getValue(Shop.class);
                    if(shopList.size() < 50 ){
                        distancesShop(shop);
                    }else break;
                }

                shopAdapter.notifyDataSetChanged();
                progress_circular.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void distancesShop(Shop shop){
        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(MAPBOX_KEY)
                .query(shop.getAdresse())
                .build();

        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                List<CarmenFeature> results = response.body().features();

                if (results.size() > 0) {

                    Point firstResultPoint = results.get(0).center();
                    if (user_location != null){
                        distance = TurfMeasurement.distance(firstResultPoint,user_location);
                        shop.setDistance(String.valueOf(distance));
                        if (distance < 1){
                            shopList.add(shop);
                            shopAdapter.notifyDataSetChanged();
                        }else {
                            shopList2.add(shop);
                            shopAdapter2.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });


    }

    @SuppressLint("MissingPermission")
    private void lastLocation(){

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            user_location = Point.fromLngLat(location.getLongitude(),location.getLatitude());
                        }
                    }
                });

    }
}
