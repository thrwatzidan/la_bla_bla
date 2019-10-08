package com.dev.thrwat_zidan.la_bla_bla;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALARY_PERMISSION = 1001;
    private ImageView profile_image;
    private EditText edt_user_status, edt_user_name;
    private Button btn_update_user_profile;
    private String currentUserID;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loading;
    private Toolbar settings_tool_bar;
    String avatar = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");

        initViews();
        edt_user_name.setVisibility(View.INVISIBLE);
        RetriveUserInfo();

    }


    private void initViews() {
        profile_image = findViewById(R.id.profile_image);
        edt_user_status = findViewById(R.id.edt_user_status);
        edt_user_name = findViewById(R.id.edt_user_name);
        btn_update_user_profile = findViewById(R.id.btn_update_user_profile);
        loading = new ProgressDialog(this);
        settings_tool_bar = findViewById(R.id.settings_tool_bar);

        setSupportActionBar(settings_tool_bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        btn_update_user_profile.setOnClickListener(this);
        edt_user_name.setOnClickListener(this);
        edt_user_status.setOnClickListener(this);
        profile_image.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_update_user_profile:
                updateSettings();
                break;
            case R.id.profile_image:
                getImageFromGal();
                break;
        }

    }

    private void getImageFromGal() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALARY_PERMISSION);
    }

    private void updateSettings() {
        String userName = edt_user_name.getText().toString();
        String userStatus = edt_user_status.getText().toString();

        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "Please Write Your Name", Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(userStatus)) {
            Toast.makeText(this, "Please Write Your Status", Toast.LENGTH_SHORT).show();

        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", userName);
            profileMap.put("status", userStatus);

            reference.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                goToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
    }

    private void RetriveUserInfo() {
        reference.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")) {

                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveUserstatus = dataSnapshot.child("status").getValue().toString();
                            String retriveUserImage = dataSnapshot.child("image").getValue().toString();

                            Picasso.get().load(retriveUserImage).into(profile_image);
                            edt_user_name.setText(retriveUserName);
                            edt_user_status.setText(retriveUserstatus);

                        } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {

                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveUserstatus = dataSnapshot.child("status").getValue().toString();
                            edt_user_name.setText(retriveUserName);
                            edt_user_status.setText(retriveUserstatus);

                        } else {
                            edt_user_name.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set & Update your profile Info", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void goToMainActivity() {
        startActivity(new Intent(SettingsActivity.this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);




        if (requestCode == GALARY_PERMISSION && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this)
            ;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loading.setTitle("Set profileImage");
                loading.setMessage("Please wait ,your profile image is updating");
                loading.setCanceledOnTouchOutside(false);
                loading.show();


                Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg ");


                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @SuppressLint("NewApi")
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadurl = task.getResult();
                            avatar = downloadurl.toString();

                            Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                             Picasso.get().load(avatar).into(profile_image);

                            reference.child("Users").child(currentUserID).child("image")
                                    .setValue(avatar)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                loading.dismiss();
                                                Toast.makeText(SettingsActivity.this, "Image save in database successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                                                loading.dismiss();
                                            }
                                        }
                                    });
                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                            loading.dismiss();
                        }

                    }
                });


            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
