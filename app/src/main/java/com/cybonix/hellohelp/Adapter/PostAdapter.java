package com.cybonix.hellohelp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cybonix.hellohelp.Model.Post;
import com.cybonix.hellohelp.Model.User;
import com.cybonix.hellohelp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Post> mPosts;

    static String TAG = "PostAdapter";

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    public PostAdapter(Context context, List<Post> posts){
        mContext = context;
        mPosts = posts;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = mPosts.get(position);

        Glide.with(mContext).load(post.getPostimage())
                .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                .into(holder.post_image);

        holder.titre.setText(post.getTitre());

        if (post.getDescription().equals("")){
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
        }

        isLiked(post.getPostid(), holder.like);
        nrLikes(holder.likes, post.getPostid());

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user.isAdmin()){
                    holder.delete_btn.setVisibility(View.VISIBLE);
                }else{
                    holder.delete_btn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (post.getLien().equals("")){
            holder.browser.setVisibility(View.GONE);
            holder.share.setVisibility(View.GONE);
        }else {
            holder.browser.setVisibility(View.VISIBLE);
            holder.share.setVisibility(View.VISIBLE);
        }

        holder.like.setOnClickListener(view -> {
            if (holder.like.getTag().equals("like")) {
                FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                        .child(firebaseUser.getUid()).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                        .child(firebaseUser.getUid()).removeValue();
            }
        });

        holder.browser.setOnClickListener(v -> {
            if (!post.getLien().equals("")){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(post.getLien()));
                mContext.startActivity(intent);
            }

        });

        holder.delete_btn.setOnClickListener(v -> deletePost(post.getPostid()));

        holder.share.setOnClickListener(v -> {
            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String shareBody = post.getLien();
            String shareSub = "Partagé depuis HelloHelp: ";
            myIntent.putExtra(Intent.EXTRA_TITLE,shareSub);
            myIntent.putExtra(Intent.EXTRA_TEXT, shareSub + shareBody);
            mContext.startActivities(new Intent[]{Intent.createChooser(myIntent, "Partagé avec")});
        });


    }

    private void isLiked(final String postid, final ImageView imageView){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else{
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView post_image, like, delete_btn, share, browser;
        public TextView  likes, description,titre;

        public ImageViewHolder(View itemView) {
            super(itemView);

            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            description = itemView.findViewById(R.id.description);
            likes = itemView.findViewById(R.id.likes);
            titre = itemView.findViewById(R.id.title);
            delete_btn = itemView.findViewById(R.id.close_btn);
            share = itemView.findViewById(R.id.share);
            browser = itemView.findViewById(R.id.browser);
        }
    }



    private void nrLikes(final TextView likes, String postId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0){
                    likes.setText("Like");
                }else {
                    likes.setText(dataSnapshot.getChildrenCount()+" Likes");
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void deletePost(final String postId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        reference.setValue(null);

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Likes").child(postId);
        reference2.setValue(null);


    }


}