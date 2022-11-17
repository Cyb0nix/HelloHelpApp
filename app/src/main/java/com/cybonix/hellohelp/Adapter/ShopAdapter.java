package com.cybonix.hellohelp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cybonix.hellohelp.Model.Shop;
import com.cybonix.hellohelp.R;
import com.cybonix.hellohelp.ShopProfileActivity;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Shop> mShops;

    public ShopAdapter(Context context, List<Shop> shops){
        mContext = context;
        mShops = shops;
    }

    @NonNull
    @Override
    public ShopAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.shop_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ShopAdapter.ImageViewHolder holder, final int position) {

        final Shop shop = mShops.get(position);

        if (!shop.getImage().equals("")){
            Glide.with(mContext).load(shop.getImage()).into(holder.shop_image);
        }else{
            Glide.with(mContext).load(R.drawable.shop)
                    .apply(new RequestOptions().placeholder(R.drawable.shop))
                    .into(holder.shop_image);
        }

        holder.shop_name.setText(shop.getNom());
        holder.shop_distance.setText(shop.getDistance());

        holder.item.setOnClickListener(v -> {
            Intent intent = new Intent (mContext, ShopProfileActivity.class);
            intent.putExtra("shop_name",shop.getNom());
            intent.putExtra("type",shop.getType());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return mShops.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView shop_image;
        public TextView shop_name, shop_distance;
        private RelativeLayout item;

        public ImageViewHolder(View itemView) {
            super(itemView);

            shop_image = itemView.findViewById(R.id.shop_image);
            item =  itemView.findViewById(R.id.item);
            shop_name = itemView.findViewById(R.id.shop_name);
            shop_distance = itemView.findViewById(R.id.shop_dist);

        }
    }

}
