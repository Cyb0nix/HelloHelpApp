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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cybonix.hellohelp.MessageActivity;
import com.cybonix.hellohelp.Model.User;
import com.cybonix.hellohelp.R;
import com.cybonix.hellohelp.UsersProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;



    public UserAdapter(Context mContexte, List<User> mUsers, boolean ischat) {
        this.mContext = mContexte;
        this.mUsers = mUsers;
        this.ischat = ischat;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,viewGroup,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {


        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());

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



        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (mContext, UsersProfileActivity.class);
                intent.putExtra("userid",user.getId());
                mContext.startActivity(intent);

            }
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


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            image_profile = itemView.findViewById(R.id.image_profile);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            verifie = itemView.findViewById(R.id.verifie1);

        }
    }

}
