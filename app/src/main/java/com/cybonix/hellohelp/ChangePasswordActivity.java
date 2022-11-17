package com.cybonix.hellohelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    EditText password1, password2, password3;
    TextView  reset;
    Button confirm;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        password1 = findViewById(R.id.password3);
        password2 =findViewById(R.id.password4);
        password3 = findViewById(R.id.password5);
        reset = findViewById(R.id.forgot_password);
        confirm = findViewById(R.id.btn_confirm3);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChangePasswordActivity.this, ResetPasswordActivity.class));
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_password1 = password1.getText().toString();
                String txt_password2 = password2.getText().toString();
                String txt_password3 = password3.getText().toString();

                if (txt_password1.isEmpty() ||txt_password2.isEmpty()||txt_password3.isEmpty()){
                    Toast.makeText(ChangePasswordActivity.this, "Tous les champs doivent être complétés", Toast.LENGTH_SHORT).show();
                }else {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


                    AuthCredential credential = EmailAuthProvider
                            .getCredential(user.getEmail(), password1.getText().toString());

                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (password2.getText().toString().equals(password3.getText().toString())){
                                        user.updatePassword(password3.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            startActivity(new Intent(ChangePasswordActivity.this, SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                            finish();
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(ChangePasswordActivity.this, "Erreur: "+e, Toast.LENGTH_SHORT).show();
                                                    }
                                        });
                                    }else {
                                        Toast.makeText(ChangePasswordActivity.this, "Les mots de passe de correspondent pas", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ChangePasswordActivity.this, "Le mot de passe ou le nom d'utilisateur de correspond pas", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }
}
