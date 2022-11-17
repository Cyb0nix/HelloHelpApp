package com.cybonix.hellohelp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cybonix.hellohelp.Model.Chat;
import com.cybonix.hellohelp.Notifications.Data;
import com.cybonix.hellohelp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.security.Key;
import java.util.Calendar;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{


    public  static  final int MSG_TYPE_LEFT = 0;
    public  static  final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;
    private int side;
    private boolean vu;

    FirebaseUser fuser;



    public MessageAdapter(Context mContexte, List<Chat> mChat, String imageurl) {
        this.mContext = mContexte;
        this.mChat = mChat;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        side = i;
        if (i == MSG_TYPE_RIGHT){
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,viewGroup,false);
        return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left,viewGroup,false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final Chat chat = mChat.get(position);

        vu = false;



        holder.show_message.setText(decrypt(chat.getMessage()));
        String[] dateheure = chat.getTime().split(" ");
        String[] h = dateheure[3].split(":");
        String heure = h[0]+":"+h[1];
        String date = dateheure[1]+" "+dateheure[2];



        if (position == mChat.size()-1){
            holder.txt_heure.setVisibility(View.GONE);

            if (chat.isIsseen()){
                holder.txt_seen.setText("Vu");
            } else {
                holder.txt_seen.setText("Envoyé");
            }
        }else {
            holder.txt_seen.setVisibility(View.GONE);
        }

        holder.message_layout.setOnClickListener(view -> {
                if(vu){
                    holder.txt_heure.setVisibility(View.GONE);
                    vu = false;
                }else {
                    holder.txt_heure.setVisibility(View.VISIBLE);
                    if(String.valueOf(chat.getDate()).equals(((String.valueOf(Calendar.getInstance().get(Calendar.DATE)))+"/"+(String.valueOf(Calendar.getInstance().get(Calendar.MONTH)))))){
                        holder.txt_heure.setText(heure);
                    } else {
                        holder.txt_heure.setText(date + " à " + heure);
                    }
                    vu = true;
                }

        });

        holder.message_layout.setOnLongClickListener(v -> {
            final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String msgTime = mChat.get(position).getTime();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
            Query query = reference.orderByChild("time").equalTo(msgTime);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (final DataSnapshot ds: dataSnapshot.getChildren()){
                        if (ds.child("sender").getValue().equals(myUID)){
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle("Supprimer");
                            builder.setMessage("Voulez-vous vraiment supprimer ce message ?");
                            builder.setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ds.getRef().removeValue();
                                }
                            });
                            builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();

                        }
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
    public int getItemCount() { return mChat.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message;
        public TextView txt_seen;
        public TextView txt_heure;
        public RelativeLayout message_layout;


        public ViewHolder( View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            txt_heure = itemView.findViewById(R.id.txt_heure);
            message_layout = itemView.findViewById(R.id.message_layout);


        }
    }



    public static String decrypt(String enc){
        try
        {
            String key = "(DRr$%Ug86*]qjv9";
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

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
