package com.cybonix.hellohelp.Adapter;

import android.content.Context;
import android.content.Intent;
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
import com.cybonix.hellohelp.Model.Annonce;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceAdapter.ImageViewHolder>{

    private Context mContext;
    private List<Annonce> mAnnonces;
    private String crtusername, messageKey;


    boolean notify = false;

    boolean imBlocked = false;
    boolean isBlocked = false;

    private FirebaseUser firebaseUser;

    APIService apiService;

    public AnnonceAdapter(Context context, List<Annonce> annonces){
        mContext = context;
        mAnnonces = annonces;

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    }

    @NonNull
    @Override
    public AnnonceAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.annonce_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AnnonceAdapter.ImageViewHolder holder, final int position) {

        final Annonce annonce = mAnnonces.get(position);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
      

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                crtusername = user.getUsername();

                if(user.isAdmin()){
                    holder.delete_btn.setVisibility(View.VISIBLE);
                }else{
                    holder.delete_btn.setVisibility(View.GONE);
                    assert firebaseUser != null;
                    if (firebaseUser.getUid().equals(annonce.getPublisher())) {
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

        assert firebaseUser != null;
        if (firebaseUser.getUid().equals(annonce.getPublisher())) {
            holder.btn_chat.setVisibility(View.GONE);
        }else {
            holder.btn_chat.setVisibility(View.VISIBLE);
        }


        publisherInfo(holder.image_profile,holder.verifie,holder.username, annonce.getPublisher(), holder.btn_chat);
        holder.contenu.setText(annonce.getContenu());

        imBlockedOrNot(annonce.getPublisher(),holder.btn_chat);
        checkIsBlocked(annonce.getPublisher(),holder.btn_chat);

        holder.username.setOnClickListener(v -> {
            Intent intent = new Intent (mContext, UsersProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("userid",annonce.getPublisher());
            mContext.startActivity(intent);
        });

        holder.image_profile.setOnClickListener(v -> {
            Intent intent = new Intent (mContext, UsersProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("userid",annonce.getPublisher());
            mContext.startActivity(intent);
        });

        holder.btn_chat.setOnClickListener(v -> {
            notify = true;
            sendMessage(firebaseUser.getUid(), annonce.getPublisher(), (crtusername + " est intéressé par votre annonce : "+ annonce.getContenu()));
            Intent intent = new Intent(mContext, MessageActivity.class);
            intent.putExtra("userid", annonce.getPublisher()).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        });

        holder.delete_btn.setOnClickListener(v -> deletePost(annonce.getAnnonceid()));


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

    private void publisherInfo(final ImageView image_profile, ImageView verifie,final TextView username, final String userid, Button btn_chat) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    if (user.getImageURL().equals("default")) {
                        Glide.with(mContext).load(R.drawable.image_pp).into(image_profile);
                    } else {
                        Glide.with(mContext).load(user.getImageURL()).into(image_profile);
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

    @Override
    public int getItemCount() {
        return mAnnonces.size();
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
                    Sender sender;
                    sender = new Sender(data, token.getToken());

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

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        public Button btn_chat;
        public ImageView image_profile, delete_btn,verifie;
        public TextView username, contenu;

        public ImageViewHolder(View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            contenu = itemView.findViewById(R.id.contenu);
            btn_chat = itemView.findViewById(R.id.btn_chat);
            delete_btn = itemView.findViewById(R.id.close_btn);
            verifie = itemView.findViewById(R.id.verifie2);

        }
    }

    private void deletePost(final String postId) {
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Annonces").child(postId);
        reference2.setValue(null);
    }
}
