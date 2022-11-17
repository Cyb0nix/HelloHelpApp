package com.cybonix.hellohelp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.geojson.Point;
import com.mapbox.turf.TurfMeasurement;
import com.rengwuxian.materialedittext.MaterialEditText;


import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText username, email, password, password2;
    Switch covoiturage, pretoutil, pretalimentaire;
    TextView dist_nord, dist_centre, dist_sud;
    Button btn_register;
    RadioButton blancmesnilnord, blancmesnilcentre, blancmesnilsud;
    final String TAG = "RegisterActivity";
    double distance;

    FirebaseAuth auth;
    DatabaseReference reference;
    String quartier;

    private Point user_location;
    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("S'enregister");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_register = findViewById(R.id.btn_register);
        blancmesnilnord = findViewById(R.id.blancmesnilnord);
        blancmesnilcentre = findViewById(R.id.blancmesnilcentre);
        blancmesnilsud = findViewById(R.id.blancmesnilsud);
        covoiturage = findViewById(R.id.covoiturage);
        pretoutil = findViewById(R.id.pretoutil);
        password2 = findViewById(R.id.password_conf);
        pretalimentaire = findViewById(R.id.pretalimentaire);
        dist_centre = findViewById(R.id.dist_centre);
        dist_nord = findViewById(R.id.dist_nord);
        dist_sud = findViewById(R.id.dist_sud);

        auth = FirebaseAuth.getInstance();
        if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(RegisterActivity.this);

        Point nord = Point.fromLngLat(48.948029, 2.451131);
        Point centre = Point.fromLngLat(48.938881, 2.463529);
        Point sud = Point.fromLngLat(48.926585, 2.472687);

        btn_register.setOnClickListener(v -> {
         String txt_username = Objects.requireNonNull(username.getText()).toString();
         String txt_email = Objects.requireNonNull(email.getText()).toString();
         String txt_password = Objects.requireNonNull(password.getText()).toString();
         String txt_password2 = Objects.requireNonNull(password2.getText()).toString();

            if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                Toast.makeText(RegisterActivity.this, "Tous les champs doivent être complétés", Toast.LENGTH_SHORT).show();
            } else if (txt_password.length() < 6 ) {
                Toast.makeText(RegisterActivity.this, "Le mot de passe doit faire au moins 6 caractères de long", Toast.LENGTH_SHORT).show();
            }else if (txt_username.length() > 20){
                Toast.makeText(RegisterActivity.this, "Votre nom d'utilisateur doit faire moins de 20 caractères de long", Toast.LENGTH_SHORT).show();
            } else if (!blancmesnilcentre.isChecked() && !blancmesnilnord.isChecked() && !blancmesnilsud.isChecked()){
                Toast.makeText(RegisterActivity.this, "Veuillez sélectionner un quartier", Toast.LENGTH_SHORT).show();
            } else if (!txt_password.equals(txt_password2)) {
                Toast.makeText(RegisterActivity.this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            }else{
                register(txt_username, txt_email, txt_password);
            }
        });


        distancesQuartier();

    }

    private void register(final String username, String email, String password){

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        assert firebaseUser != null;
                        String userid = firebaseUser.getUid();


                        if (blancmesnilnord.isChecked())
                            quartier = "blanc_mesnil_nord";
                        if (blancmesnilcentre.isChecked())
                            quartier = "blanc_mesnil_centre";
                        if (blancmesnilsud.isChecked())
                            quartier = "blanc_mesnil_sud";

                        //sauvegarde dans la base de données
                        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("id", userid);
                        hashMap.put("username", username);
                        hashMap.put("imageURL", "default");
                        hashMap.put("quartier", quartier);
                        hashMap.put("status", "offline");
                        hashMap.put("verified", false);


                        if (pretoutil.isChecked())
                            hashMap.put("pret_outil", "true_" + quartier);
                        else
                            hashMap.put("pret_outil", "false");

                        if (pretalimentaire.isChecked())
                            hashMap.put("pret_alimentaire", "true_" + quartier);
                        else
                            hashMap.put("pret_alimentaire", "false");

                        if (covoiturage.isChecked())
                            hashMap.put("covoiturage", "true_" + quartier);
                        else
                            hashMap.put("covoiturage", "false");

                        reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                Intent intent = new Intent(RegisterActivity.this, ConfirmationActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "erreur code 1", Toast.LENGTH_SHORT).show();
                            }
                        });


                    }else {
                        Toast.makeText(RegisterActivity.this, "Vous ne pouvez pas utiliser cet Email ou mot de passe", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void distancesQuartier(){
        fusedLocationClient.getLastLocation().addOnSuccessListener(RegisterActivity.this, location -> {
            user_location = Point.fromLngLat(location.getLatitude(),location.getLongitude());
            Point nord = Point.fromLngLat(48.948029, 2.451131);
            Point centre = Point.fromLngLat(48.938881, 2.463529);
            Point sud = Point.fromLngLat(48.926585, 2.472687);

            double dist_n = TurfMeasurement.distance(nord, user_location);
            double dist_c = TurfMeasurement.distance(centre, user_location);
            double dist_s = TurfMeasurement.distance(sud, user_location);

            Log.e(TAG, "u" + user_location.latitude() + "," + user_location.longitude());
            Log.e(TAG, "n" + nord.latitude() + "," + nord.longitude());

            dist_nord.setText(String.valueOf(dist_n));
            dist_centre.setText(String.valueOf(dist_c));
            dist_sud.setText(String.valueOf(dist_s));
        });
    }

}


