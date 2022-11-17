package com.cybonix.hellohelp.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cybonix.hellohelp.Adapter.CovoiturageAdapter;
import com.cybonix.hellohelp.Model.Covoiturage;
import com.cybonix.hellohelp.Model.ElasticSearchAPI;
import com.cybonix.hellohelp.Model.HitsList;
import com.cybonix.hellohelp.Model.HitsObject;
import com.cybonix.hellohelp.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RechercheCovoiturageFragment extends Fragment {

    private RecyclerView recyclerView;
    private CovoiturageAdapter covoiturageAdapter;
    private List<Covoiturage> covoiturageList;

    private String mElasticSearchPassword, AdresEnd;

    private static String BASE_URL;

    private ImageView search, filter;
    private Button search_covoiturage;

    private ProgressBar progress_circular;

    public boolean AdresEnd1;

    public String GoogleMapApiKey="AIzaSyAa5DTvuXyVcXZfkoJMvMd6ZaMpAS3Mzzs";
    int AUTOCOMPLETE_REQUEST_CODE = 1;

    private static final String TAG = RechercheCovoiturageFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_recherche_covoiturage, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        covoiturageList = new ArrayList<>();
        covoiturageAdapter = new CovoiturageAdapter(getActivity().getApplicationContext(), covoiturageList);
        recyclerView.setAdapter(covoiturageAdapter);
        search = view.findViewById(R.id.post);
        search_covoiturage = view.findViewById(R.id.Search_lieu_arrivé1);
        filter = view.findViewById(R.id.filt);
        progress_circular = view.findViewById(R.id.progress_circular);

        getElasticSearchURL();
        getElasticSearchPassword();
        EndAdres();
        readCovoiturage();
        clearFilter();

        search.setOnClickListener(v -> {
            search_covoiturage.setVisibility(View.VISIBLE);
            filter.setVisibility(View.VISIBLE);
        });

        filter.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,new FilterFragment()).commit();

        });

        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            readCovoiturage();
            pullToRefresh.setRefreshing(false);
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


    private void EndAdres() {
        search_covoiturage.setOnClickListener(view -> {
            AdresEnd1 = true;
            try {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY,
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
        if (AdresEnd1) {

            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        AdresEnd = place.getName() + "; " + place.getAddress();
                        search_covoiturage.setText("Lieu de arrivé: " + AdresEnd);
                        searchCovoiturage();
                    } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                        Status status = Autocomplete.getStatusFromIntent(data);
                    } else if (resultCode == Activity.RESULT_CANCELED) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        AdresEnd1 = false;
    }

    private void  getElasticSearchPassword(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("keys");
        ref.child("elasticsearch").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mElasticSearchPassword = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void  getElasticSearchURL(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("keys");
        ref.child("covoiturageURL").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BASE_URL = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void readCovoiturage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Covoiturage");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                covoiturageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Covoiturage covoiturage = snapshot.getValue(Covoiturage.class);
                    covoiturageList.add(covoiturage);
                }

                covoiturageAdapter.notifyDataSetChanged();
                progress_circular.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void searchCovoiturage() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ElasticSearchAPI searchAPI = retrofit.create(ElasticSearchAPI.class);

        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Autorization", Credentials.basic("user",mElasticSearchPassword));

        String searchString = "";


        if(!AdresEnd.equals("")){
            searchString = searchString + " lieu_arrive:" + AdresEnd;
        }


        Call<HitsObject> call = searchAPI.search(headerMap, "AND",searchString);

        call.enqueue(new Callback<HitsObject>() {
            @Override
            public void onResponse(Call<HitsObject> call, Response<HitsObject> response) {

                HitsList hitsList = new HitsList();
                String jsonResponse = "";

                try {


                    if (response.isSuccessful()){
                        hitsList = response.body().getHits();
                    }else{
                        jsonResponse = response.errorBody().string();
                    }

                    covoiturageList.clear();
                    for(int i = 0; i < hitsList.getCovoiturageIndex().size(); i++){

                        covoiturageList.add(hitsList.getCovoiturageIndex().get(i).getCovoiturage());
                    }
                    covoiturageAdapter.notifyDataSetChanged();

                }catch (NullPointerException | IOException | IndexOutOfBoundsException e){
                }


            }

            @Override
            public void onFailure(Call<HitsObject> call, Throwable t) {
                Toast.makeText(getActivity(),"recherche raté", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void clearFilter() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(getString(R.string.depart_lieu), "");
        editor.apply();

        editor.putString(getString(R.string.arrivee_lieu), "");
        editor.apply();

        editor.putString(getString(R.string.heure), "");
        editor.apply();

        editor.putString(getString(R.string.date), "");
        editor.apply();

        editor.putString(getString(R.string.nbr_places), "");
        editor.apply();
    }

}
