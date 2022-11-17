package com.cybonix.hellohelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.cybonix.hellohelp.Fragment.CommercesFragment;
import com.cybonix.hellohelp.Fragment.InfoFragment;
import com.cybonix.hellohelp.Fragment.LaPosteFragment;
import com.cybonix.hellohelp.Fragment.PharmaciesFragment;
import com.cybonix.hellohelp.Fragment.RestaurantsFragment;
import com.cybonix.hellohelp.Fragment.medicFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class VilleActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawer;

    BottomNavigationView bottomNavigationView;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ville);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(" ");


        drawer = findViewById(R.id.ville_drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view2);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(this, drawer,toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toogle);
        toogle.syncState();

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,new InfoFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_info);
        }





        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();




        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        if (ContextCompat.checkSelfPermission(VilleActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(VilleActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(VilleActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(VilleActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.apropos:
                startActivity(new Intent(VilleActivity.this, aproposActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                return true;

            case R.id.logout:
                status("offline");
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(VilleActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();

                return true;

            case R.id.setting:
                startActivity(new Intent(VilleActivity.this, SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_maps:
                Intent intent = new Intent(VilleActivity.this, MapsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.nav_info:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,new InfoFragment()).commit();
                break;
            case R.id.nav_restau:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,new RestaurantsFragment()).commit();
                break;
            case R.id.nav_pharm:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,new PharmaciesFragment()).commit();
                break;
            case R.id.nav_laposte:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,new LaPosteFragment()).commit();
                break;
            case R.id.nav_med:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,new medicFragment()).commit();
                break;
            case R.id.nav_commerce:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,new CommercesFragment()).commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            menuItem -> {

                switch (menuItem.getItemId()){
                    case R.id.nav_home:
                        Intent intent = new Intent(VilleActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_covoiturage:
                        Intent intent2 = new Intent(VilleActivity.this, CovoiturageActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        finish();
                        break;
                    case R.id.nav_pret:
                        Intent intent1 = new Intent(VilleActivity.this, PretActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                        finish();
                        break;
                    case R.id.nav_map:
                        break;
                    case R.id.nav_chat:
                        Intent intent3 = new Intent(VilleActivity.this, ChatActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent3);
                        finish();
                        break;
                }


                return true;
            };


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
