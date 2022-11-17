package com.cybonix.hellohelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cybonix.hellohelp.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class UsersProfileActivity extends AppCompatActivity {

    Intent intent;
    String userid, crtuser, quartier;

    CircleImageView image_profile;
    TextView username, description, covoiturage, pret_outils, pret_aliments;
    ImageView  verifie;
    Button btn_block, btn_chat, btn_signal;
    User crt_user;
    boolean isBlocked = false;
    static String TAG = "UsersProfileActivity";

    DatabaseReference reference,refu, reference2;
    private FirebaseUser firebaseUser;
    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UsersProfileActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        mFunctions = FirebaseFunctions.getInstance();


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        image_profile = findViewById(R.id.profile_image5);
        username = findViewById(R.id.username);
        btn_block = findViewById(R.id.block);
        btn_signal = findViewById(R.id.signal);
        description = findViewById(R.id.description);
        covoiturage = findViewById(R.id.covoiturage_dsp);
        pret_outils = findViewById(R.id.pret_outil_dsp);
        pret_aliments = findViewById(R.id.pret_aliments_dsp);
        btn_chat = findViewById(R.id.btn_chat);
        verifie = findViewById(R.id.verifie);

        covoiturage.setVisibility(View.GONE);
        pret_outils.setVisibility(View.GONE);
        pret_aliments.setVisibility(View.GONE);
        btn_chat.setVisibility(View.GONE);

        btn_block.setTextColor(getResources().getColor(R.color.colorUnBlock));
        btn_block.setText("Bloquer");
        imBlockedOrNot(userid);
        checkIsBlocked(userid);

        if (userid.equals(firebaseUser.getUid())){
            btn_block.setVisibility(View.GONE);
            btn_signal.setVisibility(View.GONE);
        }else {
            btn_block.setVisibility(View.VISIBLE);
            btn_signal.setVisibility(View.VISIBLE);
        }

        refu =  FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        refu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                crtuser = user.getUsername();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                description.setText(user.getDescription());
                crt_user = user;

                quartier = user.getQuartier();

                if (user.isVerified()){
                    verifie.setVisibility(View.VISIBLE);
                }

                if (user.getCovoiturage().equals("true_"+quartier)){
                    covoiturage.setVisibility(View.VISIBLE);
                }
                if (user.getPret_alimentaire().equals("true_"+quartier))
                    pret_aliments.setVisibility(View.VISIBLE);
                if (user.getPret_outil().equals("true_"+quartier))
                    pret_outils.setVisibility(View.VISIBLE);

                if (user.getImageURL().equals("default")){
                    image_profile.setImageResource(R.drawable.image_pp);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(image_profile);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        btn_block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBlocked){
                    unBlockUser(userid);
                }
                else {
                    blockUser();
                }
            }
        });

        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (UsersProfileActivity.this, MessageActivity.class);
                intent.putExtra("userid",userid);
                startActivity(intent);

            }
        });

        btn_signal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference2 = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                reference2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);

                        final EditText taskEditText = new EditText(UsersProfileActivity.this);
                        taskEditText.setPadding(30,10,30,10);
                        AlertDialog dialog = new AlertDialog.Builder(UsersProfileActivity.this)
                                .setTitle("Signaler "+ user.getUsername())
                                .setMessage("Entrer le motif de votre signalement")
                                .setView(taskEditText)
                                .setPositiveButton("Signaler", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String motif = String.valueOf(taskEditText.getText());
                                        reportUser(user.getUsername(),crtuser,motif);
                                    }
                                })
                                .setNegativeButton("Annuler", null)
                                .create();
                        dialog.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    private Task<String> reportUser(String user, String sender, String motif){
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("sender", sender);
        data.put("motif", motif);

        return mFunctions
                .getHttpsCallable("sendReport")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                }).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.e(TAG,"reported");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"reporte failed:");
                    }
                });
    }



    private void imBlockedOrNot(final String userid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(userid).child("BlockedUsers").orderByChild("uid").equalTo(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            if (ds.exists()){
                                btn_chat.setVisibility(View.GONE);
                                return;
                            }
                        }
                        if (!userid.equals(firebaseUser.getUid())){
                                btn_chat.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void checkIsBlocked(String userid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(userid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            if(ds.exists()){
                                btn_block.setTextColor(getResources().getColor(R.color.colorBlock));
                                btn_block.setText("Débloquer");
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void blockUser() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", userid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid()).child("BlockedUsers").child(userid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"Utilisateur bloqué", Toast.LENGTH_SHORT).show();
                        btn_block.setTextColor(getResources().getColor(R.color.colorBlock));
                        btn_block.setText("Débloquer");
                        isBlocked = true;

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Erreur:"+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void unBlockUser(String userid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(userid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            if (ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(),"Utilisateur débloqué", Toast.LENGTH_SHORT).show();
                                                btn_block.setTextColor(getResources().getColor(R.color.colorUnBlock));
                                                btn_block.setText("Bloquer");
                                                isBlocked = false;
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(),"Erreur:"+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

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
}
