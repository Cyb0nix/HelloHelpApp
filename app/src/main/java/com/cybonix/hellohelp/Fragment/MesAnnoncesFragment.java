package com.cybonix.hellohelp.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cybonix.hellohelp.Adapter.AnnonceAdapter;
import com.cybonix.hellohelp.Model.Annonce;
import com.cybonix.hellohelp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MesAnnoncesFragment extends Fragment {

    private RecyclerView recyclerView;
    private AnnonceAdapter annonceAdapter;
    private List<Annonce> annonceList;
    FirebaseUser firebaseUser;


    ProgressBar progress_circular;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mes_annonces, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        annonceList = new ArrayList<>();
        annonceAdapter = new AnnonceAdapter(getActivity().getApplicationContext(), annonceList);
        recyclerView.setAdapter(annonceAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        progress_circular = view.findViewById(R.id.progress_circular);

        readMyAnnonce();




        return view;
    }

    private void readMyAnnonce(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Annonces");

        reference.orderByChild("publisher").equalTo(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                annonceList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Annonce annonce = snapshot.getValue(Annonce.class);
                    annonceList.add(annonce);
                }

                annonceAdapter.notifyDataSetChanged();
                progress_circular.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
