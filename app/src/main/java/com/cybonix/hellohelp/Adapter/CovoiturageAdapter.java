package com.cybonix.hellohelp.Adapter;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cybonix.hellohelp.BuildConfig;
import com.cybonix.hellohelp.Fragment.APIService;
import com.cybonix.hellohelp.MessageActivity;
import com.cybonix.hellohelp.Model.Covoiturage;
import com.cybonix.hellohelp.Model.User;
import com.cybonix.hellohelp.Notifications.Client;
import com.cybonix.hellohelp.Notifications.Data;
import com.cybonix.hellohelp.Notifications.MyResponse;
import com.cybonix.hellohelp.Notifications.Sender;
import com.cybonix.hellohelp.Notifications.Token;
import com.cybonix.hellohelp.R;
import com.cybonix.hellohelp.UsersProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CovoiturageAdapter extends RecyclerView.Adapter<CovoiturageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Covoiturage> mCovoitures;
    private String crtusername, messageKey;

    private FirebaseUser firebaseUser;

    boolean notify = false;
    boolean imBlocked = false;
    boolean isBlocked = false;

    APIService apiService;


    public CovoiturageAdapter(Context context, List<Covoiturage> mCovoiturages) {
        mContext = context;
        mCovoitures = mCovoiturages;

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

    }

    @NonNull
    @Override
    public CovoiturageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.covoiturage_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CovoiturageAdapter.ImageViewHolder holder, final int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Covoiturage covoiturage = mCovoitures.get(position);


        holder.lieu_depart.setText(covoiturage.getLieu_depart());
        holder.lieu_arrive.setText(covoiturage.getLieu_arrive());
        holder.heure.setText(covoiturage.getHeure());
        holder.date.setText(covoiturage.getDate());
        holder.nbr_places.setText(covoiturage.getNbr_places());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                User user;
                user = dataSnapshot.getValue(User.class);
                assert user != null;
                crtusername = user.getUsername();

                if(user.isAdmin() || firebaseUser.getUid().equals(covoiturage.getPublisher())){
                    holder.delete_btn.setVisibility(View.VISIBLE);
                }else{
                    if (firebaseUser.getUid().equals(covoiturage.getPublisher())) {
                        holder.delete_btn.setVisibility(View.VISIBLE);
                    }else {
                        holder.delete_btn.setVisibility(View.GONE);
                    }
                }


            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });

        getmessageKey();


        if (firebaseUser.getUid().equals(covoiturage.getPublisher())) {
            holder.btn_chat.setVisibility(View.GONE);
            holder.delete_btn.setVisibility(View.VISIBLE);
        }else {
            holder.btn_chat.setVisibility(View.VISIBLE);
            holder.delete_btn.setVisibility(View.GONE);
        }

        holder.username.setOnClickListener(v -> {
            Intent intent = new Intent (mContext, UsersProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("userid",covoiturage.getPublisher());
            mContext.startActivity(intent);
        });

        holder.image_profile.setOnClickListener(v -> {
            Intent intent = new Intent (mContext, UsersProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("userid",covoiturage.getPublisher());
            mContext.startActivity(intent);
        });


        publisherInfo(holder.image_profile, holder.verifie, holder.username, covoiturage.getPublisher(), holder.btn_chat);

        imBlockedOrNot(covoiturage.getPublisher(),holder.btn_chat);
        checkIsBlocked(covoiturage.getPublisher(),holder.btn_chat);

        holder.btn_chat.setOnClickListener(v -> {
            notify = true;
            sendMessage(firebaseUser.getUid(), covoiturage.getPublisher(), (crtusername + " est intéressé par votre offre de covoiturage : "
                    + "Départ " + covoiturage.getLieu_depart() + ", Arrivé " + covoiturage.getLieu_arrive() + "."));
            Intent intent = new Intent(mContext, MessageActivity.class);
            intent.putExtra("userid", covoiturage.getPublisher()).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        });

        holder.delete_btn.setOnClickListener(v -> deletePost(covoiturage.getPostid()));


    }

    @Override
    public int getItemCount() {
        return mCovoitures.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, delete_btn,verifie;
        public TextView username, lieu_depart, lieu_arrive, heure, nbr_places, date;
        public Button btn_chat;

        public ImageViewHolder(View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            lieu_depart = itemView.findViewById(R.id.lieu_départ);
            lieu_arrive = itemView.findViewById(R.id.lieu_arrivé);
            heure = itemView.findViewById(R.id.heure_départ);
            date = itemView.findViewById(R.id.date_dep);
            btn_chat = itemView.findViewById(R.id.btn_chat);
            nbr_places = itemView.findViewById(R.id.nbr_places);
            delete_btn = itemView.findViewById(R.id.close_btn);
            verifie = itemView.findViewById(R.id.verifie3);


        }
    }

    private void checkIsBlocked(String userid, Button btn_chat) {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        assert fuser != null;
        ref.child(fuser.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(userid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            if(ds.exists()){
                                isBlocked = true;
                                btn_chat.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void publisherInfo(final ImageView image_profile, ImageView verifie, final TextView username, final String userid, Button btn_chat) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    if (user.getImageURL().equals("default")) {
                        Glide.with(mContext.getApplicationContext()).load(R.drawable.image_pp).into(image_profile);
                    } else {
                        Glide.with(mContext.getApplicationContext()).load(user.getImageURL()).into(image_profile);
                    }

                    username.setText(user.getUsername());

                    if (user.isVerified()){
                        verifie.setVisibility(View.VISIBLE);
                    }else {
                        verifie.setVisibility(View.GONE);
                    }
                }else {
                    btn_chat.setVisibility(View.GONE);
                }



            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });
    }

    private void imBlockedOrNot(String userid, Button btn_chat){
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        assert fuser != null;
        ref.child(userid).child("BlockedUsers").orderByChild("uid").equalTo(fuser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            if (ds.exists()){
                                btn_chat.setVisibility(View.GONE);
                                imBlocked = true;
                                return;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void  getmessageKey(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("keys");
        ref.child("messageKey").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageKey = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void sendMessage(String sender, final String receiver, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        String messageencrypted = MessageActivity.encrypt(message, messageKey);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", messageencrypted);
        hashMap.put("isseen", false);
        hashMap.put("date", ((Calendar.getInstance().get(Calendar.DATE)) + "/" + (Calendar.getInstance().get(Calendar.MONTH))));
        hashMap.put("time", (String.valueOf(Calendar.getInstance().getTime())));


        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(receiver);

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(receiver)
                .child(firebaseUser.getUid());

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(receiver);
                    chatRefReceiver.child("id").setValue(firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    assert user != null;
                    sendNotification(receiver, msg);
                }

                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendNotification(final String receiver, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_hello_help, message.split(":")[1], message.split(":")[0],
                            receiver);

                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(@NotNull Call<MyResponse> call, @NotNull Response<MyResponse> response) {
                                    if (BuildConfig.DEBUG && !(response.code() != 200 || response.body() != null)) {
                                        throw new AssertionError("Assertion failed");
                                    }
                                }

                                @Override
                                public void onFailure(@NotNull Call<MyResponse> call, @NotNull Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deletePost(final String postId) {
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Covoiturage").child(postId);
        reference2.setValue(null);
    }


}