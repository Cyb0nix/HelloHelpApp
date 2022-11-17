package com.cybonix.hellohelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangeEmailActivity extends AppCompatActivity {

    EditText email1, email2;
    Button confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        email1 = findViewById(R.id.email2);
        email2 = findViewById(R.id.email3);
        confirm = findViewById(R.id.btn_confirm2);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email1 = email1.getText().toString();
                String txt_email2 = email2.getText().toString();
                if (txt_email1.isEmpty() ||txt_email2.isEmpty()){
                    Toast.makeText(ChangeEmailActivity.this, "Tous les champs doivent être complétés", Toast.LENGTH_SHORT).show();
                }else {
                    if (email1.getText().toString().equals(email2.getText().toString())) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        user.updateEmail(email2.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            startActivity(new Intent(ChangeEmailActivity.this, SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                            finish();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ChangeEmailActivity.this, "Le mot de passe ou le nom d'utilisateur de correspond pas", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(ChangeEmailActivity.this, "Les Emails de correspondent pas", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
