package com.cybonix.hellohelp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cybonix.hellohelp.MessageActivity;
import com.cybonix.hellohelp.Model.Chat;
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

import java.security.Key;
import java.util.Calendar;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter2 extends RecyclerView.Adapter<UserAdapter2.ViewHolder>{

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;

    String theLastMessage, chatdate, heure, date, messageKey;
    boolean issen;

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;


    public UserAdapter2(Context mContexte, List<User> mUsers, boolean ischat) {
        this.mContext = mContexte;
        this.mUsers = mUsers;
        this.ischat = ischat;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item2,viewGroup,false);
        return new UserAdapter2.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());

        getmessageKey();

        if (user.isVerified()){
            holder.verifie.setVisibility(View.VISIBLE);
        }else {
            holder.verifie.setVisibility(View.GONE);
        }

        if (user.getImageURL() != null) {
            if (user.getImageURL().equals("default")){
                holder.image_profile.setImageResource(R.drawable.image_pp);
            }else {
                Glide.with(mContext).load(user.getImageURL()).into(holder.image_profile);
            }
        }

        if (ischat){
            lastMessage(user.getId(), holder.last_msg,holder.username,holder.last_time);
        } else {
            holder.last_msg.setVisibility(View.GONE);
            holder.last_time.setVisibility(View.GONE);
        }

        if (ischat){
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.GONE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, MessageActivity.class);
            intent.putExtra("userid", user.getId());
            mContext.startActivity(intent);
        });

        holder.user_item2.setOnLongClickListener(v -> {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());
            Query query = reference.orderByChild("id").equalTo(user.getId());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (final DataSnapshot ds: dataSnapshot.getChildren()){
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Supprimer");
                        builder.setMessage("Voulez-vous vraiment supprimer cette conversation ?");
                        builder.setPositiveButton("Supprimer", (dialog, which) -> ds.getRef().removeValue());
                        builder.setNegativeButton("Non", (dialog, which) -> dialog.dismiss());
                        builder.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return false;
        });





    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public CircleImageView image_profile;
        private ImageView img_on;
        private ImageView img_off;
        private ImageView verifie;
        private TextView last_msg, last_time;
        public RelativeLayout user_item2;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            image_profile = itemView.findViewById(R.id.image_profile);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            verifie = itemView.findViewById(R.id.verifie2);
            last_msg = itemView.findViewById(R.id.last_msg);
            last_time = itemView.findViewById(R.id.last_time);
            user_item2 = itemView.findViewById(R.id.user_item2);

        }
    }

    private void lastMessage(final String userid, final TextView last_msg, final TextView username, final TextView last_time){
        theLastMessage = "default";
        issen = true;
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = decrypt(chat.getMessage(),messageKey);
                            chatdate = chat.getDate();
                            String[] dateheure = chat.getTime().split(" ");
                            String[] h = dateheure[3].split(":");
                            heure = h[0]+":"+h[1];
                            date = dateheure[1]+" "+dateheure[2];

                            if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen() ){
                                issen = false;
                            }
                        }
                    }
                }

                switch (theLastMessage){
                    case  "default":
                        last_msg.setText("No Message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        if(String.valueOf(chatdate).equals(((String.valueOf(Calendar.getInstance().get(Calendar.DATE)))+"/"+(String.valueOf(Calendar.getInstance().get(Calendar.MONTH)))))){
                            last_time.setText(heure);
                        } else {
                            last_time.setText(date);
                        }

                        if (!issen){
                            last_msg.setTypeface(null,Typeface.BOLD);
                            username.setTypeface(null,Typeface.BOLD);
                            last_time.setTypeface(null,Typeface.BOLD);

                        }else {
                            last_msg.setTypeface(null,Typeface.NORMAL);
                            username.setTypeface(null,Typeface.NORMAL);
                            last_time.setTypeface(null,Typeface.NORMAL);
                        }
                        break;
                }

                theLastMessage = "default";
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



    public static String decrypt(String enc,String key){
        try
        {

            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            // now convert the string to byte array
            // for decryption
            byte[] bb = new byte[enc.length()];
            for (int i=0; i<enc.length(); i++) {
                bb[i] = (byte) enc.charAt(i);
            }

            // decrypt the text
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            String decrypted = new String(cipher.doFinal(bb));

            return decrypted;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }



}
