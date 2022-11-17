package com.cybonix.hellohelp.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;

import com.cybonix.hellohelp.Adapter.UserAdapter;
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

import java.util.ArrayList;
import java.util.List;


public class EmprunterObjetFragment extends Fragment {

    FirebaseUser firebaseUser;

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;


    String service, quartier;


    Switch switch_aliments, switch_outils;

    RadioButton blancmesnilnord, blancmesnilcentre, blancmesnilsud;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_emprunter_objet, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        switch_aliments = view.findViewById(R.id.switch_aliments);
        switch_outils = view.findViewById(R.id.switch_outils);
        blancmesnilnord = view.findViewById(R.id.blancmesnilnord);
        blancmesnilcentre = view.findViewById(R.id.blancmesnilcentre);
        blancmesnilsud = view.findViewById(R.id.blancmesnilsud);


        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), mUsers, true);
        recyclerView.setAdapter(userAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                quartier = user.getQuartier();
                readUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        switch_aliments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_aliments.isChecked()){
                    searchUsersServices();
                }
                else{
                    readUsers();
                }
            }
        });

        switch_outils.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_outils.isChecked()){
                    searchUsersServices();
                }
                else {
                    readUsers();
                }
            }
        });
        return view;
    }

    private void searchUsersServices() {


        if (switch_outils.isChecked())
            service = "pret_outil";
        if (switch_aliments.isChecked())
            service = "pret_alimentaire";

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild(service)
                .equalTo("true_" + quartier);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    mUsers.add(user);

                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUsers(){


        Query reference = FirebaseDatabase.getInstance().getReference("Users").orderByChild("quartier")
                .startAt(quartier)
                .endAt(quartier);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);
                        mUsers.add(user);
                    }

                    userAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
