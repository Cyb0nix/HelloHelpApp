package com.cybonix.hellohelp.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cybonix.hellohelp.Adapter.AnnonceAdapter;
import com.cybonix.hellohelp.Model.Annonce;
import com.cybonix.hellohelp.Model.ElasticSearch2API;
import com.cybonix.hellohelp.Model.HitsList2;
import com.cybonix.hellohelp.Model.HitsObject2;
import com.cybonix.hellohelp.Model.User;
import com.cybonix.hellohelp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


public class AnnoncesServicesFragment extends Fragment {

    private EditText search_annonce;
    private ProgressBar progress_circular;
    private String quartier,service;
    private String mElasticSearchPassword;

    final String TAG = "AnnoncesServicesFragmen";

    private AnnonceAdapter annonceAdapter;
    private List<Annonce> annonceList;
    private static String BASE_URL;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_annonces_services, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        annonceList = new ArrayList<>();
        annonceAdapter = new AnnonceAdapter(requireActivity().getApplicationContext(), annonceList);
        recyclerView.setAdapter(annonceAdapter);
        ImageView search = view.findViewById(R.id.post);
        search_annonce = view.findViewById(R.id.Search_annonce);
        progress_circular = view.findViewById(R.id.progress_circular);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        getElasticSearchURL();

        assert firebaseUser != null;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                assert user != null;
                quartier = user.getQuartier();
                readAnnonces();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getElasticSearchPassword();



        search.setOnClickListener(v -> search_annonce.setVisibility(View.VISIBLE));



        search_annonce.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                service = charSequence.toString().toLowerCase();
                searchAnn();


            }

            @Override
            public void afterTextChanged(Editable editable) {
                readAnnonces();

            }
        });

        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            readAnnonces();
            pullToRefresh.setRefreshing(false);
        });


        return view;
    }

    private void readAnnonces() {
        getElasticSearchURL();
        Query reference = FirebaseDatabase.getInstance().getReference("Annonces").orderByChild("quartier")
                .startAt(quartier)
                .endAt(quartier);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (search_annonce.getText().toString().equals("")){
                    annonceList.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Annonce annonce = snapshot.getValue(Annonce.class);
                        annonceList.add(annonce);
                    }

                    annonceAdapter.notifyDataSetChanged();
                    progress_circular.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

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

    private void searchAnn() {
        getElasticSearchURL();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ElasticSearch2API searchAPI = retrofit.create(ElasticSearch2API.class);

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Autorization", Credentials.basic("user", mElasticSearchPassword));

        String searchString = "";

        if (!service.equals("")){
            searchString = searchString + service + "*";
        }

        if (!quartier.equals("")) {
            searchString = searchString + " quartier:" + quartier;
        }


        Call<HitsObject2> call = searchAPI.search(headerMap, "AND", searchString);

        call.enqueue(new Callback<HitsObject2>() {
            @SuppressLint("LogNotTimber")
            @Override
            public void onResponse(@NotNull Call<HitsObject2> call, @NotNull Response<HitsObject2> response) {

                HitsList2 hitsList = new HitsList2();
                String jsonResponse = "";

                try {

                    Log.d(TAG, "onResponse: serveur response: " + response.toString());

                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        hitsList = response.body().getHits2();
                    } else {
                        assert response.errorBody() != null;
                        jsonResponse = response.errorBody().string();
                    }

                    Log.d(TAG, "onResponse: hits: " + hitsList);
                    annonceList.clear();
                    for (int i = 0; i < hitsList.getAnnonceIndex().size(); i++) {
                        Log.d(TAG, "onResponse: data : " + hitsList.getAnnonceIndex().get(i).getAnnonce().toString());

                        annonceList.add(hitsList.getAnnonceIndex().get(i).getAnnonce());
                    }
                    annonceAdapter.notifyDataSetChanged();

                } catch (NullPointerException | IndexOutOfBoundsException | IOException e) {
                    Log.e(TAG, "onResponse: NullPointerException: " + e.getMessage());
                }


            }

            @SuppressLint("LogNotTimber")
            @Override
            public void onFailure(@NotNull Call<HitsObject2> call, @NotNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(getActivity(), "recherche raté", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
