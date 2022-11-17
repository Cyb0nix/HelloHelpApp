package com.cybonix.hellohelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cybonix.hellohelp.Model.Shop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShopProfileActivity extends AppCompatActivity {

    Intent intent;
    String shopname, type, shop_adresse;

    static String TAG = "ShopProfileActivity";

    TextView name, description, tel,adresse;
    ImageView shop_image;
    Button itineraire;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_profile);

        intent = getIntent();
        shopname = intent.getStringExtra("shop_name");
        type = intent.getStringExtra("type");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(shopname);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = findViewById(R.id.name_shop);
        description = findViewById(R.id.description_shop);
        tel = findViewById(R.id.number_shop);
        adresse = findViewById(R.id.adresse_shop);
        shop_image =  findViewById(R.id.image_shop);
        itineraire = findViewById(R.id.itineraire);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShopProfileActivity.this, VilleActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Lieu").child(type).child(shopname);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Shop shop = dataSnapshot.getValue(Shop.class);
                Log.e(TAG, ": " + shop.getNom() );

                name.setText(shop.getNom());
                tel.setText(shop.getNumero());
                adresse.setText(shop.getAdresse());
                shop_adresse = shop.getAdresse();

                if (shop.getImage() != null){
                    Glide.with(ShopProfileActivity.this).load(shop.getImage()).into(shop_image);
                }else{
                    Glide.with(getApplicationContext()).load(R.drawable.shop)
                            .apply(new RequestOptions().placeholder(R.drawable.shop))
                            .into(shop_image);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        itineraire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (ShopProfileActivity.this, NavActivity.class);
                intent.putExtra("shop_adresse",shop_adresse);
                ShopProfileActivity.this.startActivity(intent);
            }
        });


    }
}
