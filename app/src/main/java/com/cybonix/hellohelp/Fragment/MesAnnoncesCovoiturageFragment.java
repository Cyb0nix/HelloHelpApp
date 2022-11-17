package com.cybonix.hellohelp.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cybonix.hellohelp.Adapter.CovoiturageAdapter;
import com.cybonix.hellohelp.Model.Covoiturage;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MesAnnoncesCovoiturageFragment extends Fragment {

    private RecyclerView recyclerView;
    private CovoiturageAdapter covoiturageAdapter;
    private List<Covoiturage> covoiturageList;
    FirebaseUser firebaseUser;


    ProgressBar progress_circular;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mes_annonces_covoiturage, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        covoiturageList = new ArrayList<>();
        covoiturageAdapter = new CovoiturageAdapter(getActivity().getApplicationContext(), covoiturageList);
        recyclerView.setAdapter(covoiturageAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        progress_circular = view.findViewById(R.id.progress_circular);

        readMyPosts();

        return view;
    }

    private void readMyPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Covoiturage");

        reference.orderByChild("publisher").equalTo(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                covoiturageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
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
}
