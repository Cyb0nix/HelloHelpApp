package com.cybonix.hellohelp.Fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cybonix.hellohelp.Adapter.CovoiturageAdapter;
import com.cybonix.hellohelp.Model.Covoiturage;
import com.cybonix.hellohelp.Model.ElasticSearchAPI;
import com.cybonix.hellohelp.Model.HitsList;
import com.cybonix.hellohelp.Model.HitsObject;
import com.cybonix.hellohelp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;


public class AdvancedSearchFragment extends Fragment {

    private CovoiturageAdapter covoiturageAdapter;
    private List<Covoiturage> covoiturageList;

    private String mElasticSearchPassword;
    private String mLieu_depart, mHeure,mDate;
    private String mNbr_place;
    private String AdresEnd;

    private static  String BASE_URL;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.fragment_advanced_search, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        covoiturageList = new ArrayList<>();
        covoiturageAdapter = new CovoiturageAdapter(getContext(), covoiturageList);
        recyclerView.setAdapter(covoiturageAdapter);

        getElasticSearchURL();
        getElasticSearchPassword();
        getFilters();
        searchCovoiturage();

        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(this::searchCovoiturage);


        return view;
    }

    private void getFilters(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mLieu_depart = preferences.getString(getString(R.string.depart_lieu),"");
        mDate = preferences.getString(getString(R.string.date),"");
        mHeure = preferences.getString(getString(R.string.heure), "");
        mNbr_place = preferences.getString(getString(R.string.nbr_places),"");
        AdresEnd = preferences.getString(getString(R.string.arrivee_lieu),"");
    }

    private void  getElasticSearchURL(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("keys");
        ref.child("annoncesURL").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BASE_URL = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    private void searchCovoiturage() {
        getFilters();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ElasticSearchAPI searchAPI = retrofit.create(ElasticSearchAPI.class);

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Autorization", Credentials.basic("user",mElasticSearchPassword));

        String searchString = "";

        if(!mLieu_depart.equals("")){
            searchString = searchString + " lieu_depart:" + mLieu_depart;
        }

        if(!AdresEnd.equals("")){
            searchString = searchString + " lieu_arrive:" + AdresEnd;
        }

        if(!mNbr_place.equals("")){
            searchString = searchString +" nbr_places:"+ mNbr_place;
        }

        if(!mDate.equals("")){
            searchString = searchString +" date:"+ mDate;
        }

        if(!mHeure.equals("")){
            searchString = searchString +" heure:"+ mHeure;
        }



        Call<HitsObject> call = searchAPI.search(headerMap, "AND",searchString);

        call.enqueue(new Callback<HitsObject>() {
            @SuppressLint({"BinaryOperationInTimber", "LogNotTimber"})
            @Override
            public void onResponse(@NotNull Call<HitsObject> call, @NotNull Response<HitsObject> response) {

                HitsList hitsList = new HitsList();
                String jsonResponse = "";

                try {

                    if (response.isSuccessful()){
                        assert response.body() != null;
                        hitsList = response.body().getHits();
                    }else{
                        assert response.errorBody() != null;
                        jsonResponse = response.errorBody().string();
                    }

                    covoiturageList.clear();
                    for(int i = 0; i < hitsList.getCovoiturageIndex().size(); i++){

                        covoiturageList.add(hitsList.getCovoiturageIndex().get(i).getCovoiturage());
                    }
                    covoiturageAdapter.notifyDataSetChanged();

                }catch (NullPointerException | IndexOutOfBoundsException | IOException e){
                }


            }

            @SuppressLint("LogNotTimber")
            @Override
            public void onFailure(@NotNull Call<HitsObject> call, @NotNull Throwable t) {
                Toast.makeText(getActivity(),"recherche raté", Toast.LENGTH_SHORT).show();
            }
        });

    }
}