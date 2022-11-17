package com.cybonix.hellohelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class ReLoginActivity extends AppCompatActivity {

    EditText password;
    Button next;
    String type;
    Intent intent;

    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_login);

        password = findViewById(R.id.password2);
        next = findViewById(R.id.btn_login2);

        mFunctions = FirebaseFunctions.getInstance();

        intent = getIntent();
        type = intent.getStringExtra("type");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_password = password.getText().toString();
                if (txt_password.isEmpty()){
                    Toast.makeText(ReLoginActivity.this, "Veuillez entrer votre mot de passe", Toast.LENGTH_SHORT).show();
                }else {
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(firebaseUser.getEmail(), password.getText().toString());

                    // Prompt the user to re-provide their sign-in credentials
                    firebaseUser.reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (type.equals("mail")) {
                                        startActivity(new Intent(ReLoginActivity.this, ChangeEmailActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        finish();
                                    }
                                    if (type.equals("delete")) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ReLoginActivity.this);
                                        builder.setTitle("Supprimer votre Compte");
                                        builder.setMessage("Voulez-vous vraiment supprimer votre compte de manière définitive ?");
                                        builder.setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                deleteUser(firebaseUser.getUid());
                                                firebaseUser.delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    startActivity(new Intent(ReLoginActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                                    finish();
                                                                }else {
                                                                    Toast.makeText(ReLoginActivity.this, "Suppression du compte raté, Veuillez réssayer", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                                        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                startActivity(new Intent(ReLoginActivity.this, SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                finish();
                                            }
                                        });
                                        builder.show();
                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ReLoginActivity.this, "Le mot de passe ne correspond pas", Toast.LENGTH_SHORT).show();
                        }

                    });
                }
            }
        });


    }

    private Task<String> deleteUser(String id){
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("user", id);

        return mFunctions
                .getHttpsCallable("deleteUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
}
