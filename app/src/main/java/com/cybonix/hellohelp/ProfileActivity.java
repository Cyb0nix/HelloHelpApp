package com.cybonix.hellohelp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cybonix.hellohelp.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileActivity extends AppCompatActivity {

    CircleImageView image_profile;
    EditText username, description;


    Switch covoiturage, pretoutil, pretalimentaire;
    Button btn_enregistrer;

    DatabaseReference reference;
    FirebaseUser fuser;

    String quartier;

    StorageReference storageReference;
    private  static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Compte");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });


        image_profile = findViewById(R.id.profile_image4);
        username = findViewById(R.id.username);

        covoiturage = findViewById(R.id.covoiturage1);
        pretoutil = findViewById(R.id.pretoutil1);
        pretalimentaire = findViewById(R.id.pretalimentaire1);
        btn_enregistrer = findViewById(R.id.btn_set);
        description = findViewById(R.id.description);



        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        storageReference = FirebaseStorage.getInstance().getReference("uploads").child(fuser.getUid());


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    image_profile.setImageResource(R.drawable.image_pp);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(image_profile);
                }

                quartier=user.getQuartier();
                description.setText(user.getDescription());

                if (user.getCovoiturage().equals("true_"+quartier)){
                    covoiturage.setChecked(true);
                }
                if (user.getPret_alimentaire().equals("true_"+quartier))
                    pretalimentaire.setChecked(true);
                if (user.getPret_outil().equals("true_"+quartier))
                    pretoutil.setChecked(true);





            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        image_profile.setOnClickListener(v -> openImage());

        btn_enregistrer.setOnClickListener(v -> {

            reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

            HashMap<String, Object> hashMapupdate = new HashMap<>();

            hashMapupdate.put("id", fuser.getUid());
            hashMapupdate.put("username", username.getText().toString());
            hashMapupdate.put("description",description.getText().toString());
            if (pretoutil.isChecked())
                hashMapupdate.put("pret_outil", "true_"+quartier);
            else
                hashMapupdate.put("pret_outil", "false");

            if (pretalimentaire.isChecked())
                hashMapupdate.put("pret_alimentaire", "true_"+quartier);
            else
                hashMapupdate.put("pret_alimentaire", "false");

            if (covoiturage.isChecked())
                hashMapupdate.put("covoiturage", "true_"+quartier);
            else
                hashMapupdate.put("covoiturage", "false");

            reference.updateChildren(hashMapupdate).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ProfileActivity.this, "erreur code 1", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void openImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setFixAspectRatio(true)
                .setMinCropResultSize(1000,1000)
                .setMaxCropResultSize(3000,3000)
                .start(this);

    }

    private  String getFileExtension(Uri uri){
        ContentResolver contentResolver = ProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null){
            final  StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if (!task.isSuccessful()){
                    throw  task.getException();
                }

                return  fileReference.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    String mUri = downloadUri.toString();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageURL", ""+mUri);
                    reference.updateChildren(map);

                    pd.dismiss();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(ProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();

                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(ProfileActivity.this, "Upload en cours", Toast.LENGTH_SHORT).show();

                } else {
                    uploadImage();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

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
