package com.cybonix.hellohelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingActivity extends AppCompatActivity {

    Button email,password, logout,delete,verifie;
    ImageView close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        email = findViewById(R.id.email_edit);
        password = findViewById(R.id.password_edit);
        delete = findViewById(R.id.delete);
        logout = findViewById(R.id.logout2);
        verifie = findViewById(R.id.verifie);
        close = findViewById(R.id.close);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser.isEmailVerified()){
            verifie.setVisibility(View.GONE);
        }

        email.setText(firebaseUser.getEmail());

        email.setOnClickListener(v -> {
            Intent intent = new Intent (SettingActivity.this, ReLoginActivity.class);
            intent.putExtra("type","mail");
            SettingActivity.this.startActivity(intent);

        });

        password.setOnClickListener(v -> startActivity(new Intent(SettingActivity.this, ChangePasswordActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(SettingActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });

        delete.setOnClickListener(v -> {
            Intent intent = new Intent (SettingActivity.this, ReLoginActivity.class);
            intent.putExtra("type","delete");
            SettingActivity.this.startActivity(intent);
        });

        verifie.setOnClickListener(v -> firebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingActivity.this,"Un Email de vérification vous à été envoyé.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }));

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });



    }


}
