package com.cybonix.hellohelp.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cybonix.hellohelp.Model.Annonce;
import com.cybonix.hellohelp.Model.User;
import com.cybonix.hellohelp.PretActivity;
import com.cybonix.hellohelp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class PostAnnonceFragment extends Fragment {

    EditText service, contenu;
    Button post;
    int nbr;
    private List<Annonce> annonceList;

    String quartier;

    FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_annonce, container, false);

        service = view.findViewById(R.id.service_type);
        contenu = view.findViewById(R.id.service_contenu);
        post = view.findViewById(R.id.post);

        annonceList = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                quartier = Objects.requireNonNull(user).getQuartier();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getAnnonceNbr();

        post.setOnClickListener(v -> {
            String txt_service = service.getText().toString();
            String txt_contenu = contenu.getText().toString();

            if (TextUtils.isEmpty(txt_service)||TextUtils.isEmpty(txt_contenu)){
                Toast.makeText(getContext(), "Tous les champs doivent être complétés", Toast.LENGTH_SHORT).show();

            }else{
                if (nbr >= 5){
                    Toast.makeText(getContext(), "Vous avez atteint votre quota d'annonces. Veuillez en supprimer pour en poster de nouvelles.", Toast.LENGTH_LONG).show();
                }else{
                    Post();
                }
            }
        });



        return view;
    }

    private void Post() {
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Envoi");
        pd.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Annonces");

        String annonceid = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("annonceid", annonceid);
        hashMap.put("type", service.getText().toString());
        hashMap.put("contenu", contenu.getText().toString());
        hashMap.put("quartier", quartier);
        hashMap.put("publisher", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());


        assert annonceid != null;
        reference.child(annonceid).setValue(hashMap);
        pd.dismiss();

        startActivity(new Intent(getContext(), PretActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private void getAnnonceNbr(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Annonces");
        reference.orderByChild("publisher").equalTo(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                annonceList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Annonce annonce = snapshot.getValue(Annonce.class);
                    annonceList.add(annonce);
                }

                nbr = annonceList.size();
            }



            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });
    }
}
