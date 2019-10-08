package com.dev.thrwat_zidan.la_bla_bla;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager myviewPager;
    private TabLayout myTablayout;
    private TabsAccessAdapter adapter;

    private FirebaseAuth auth;
    private DatabaseReference reference;
    private String currentUser_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        reference = FirebaseDatabase.getInstance().getReference();

        toolbar = findViewById(R.id.main_page_toolbar);
        myviewPager = findViewById(R.id.main_tabs_Viewpager);
        myTablayout = findViewById(R.id.main_tabs);
        adapter = new TabsAccessAdapter(getSupportFragmentManager());
        myviewPager.setAdapter(adapter);

        myTablayout.setupWithViewPager(myviewPager);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("La-Bla-Bla");


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            SendUserLoginActivity();
        } else {

            updateUserStatus("online");
            verifyUserExistance();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    private void verifyUserExistance() {
        String currentUserID = auth.getCurrentUser().getUid();
        reference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").exists()) {
                   // Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                    Log.i("Main_TAG", "Welcome");
                } else {
                    goToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_option) {
            updateUserStatus("offline");
            auth.signOut();
            SendUserLoginActivity();
        }
        if (item.getItemId() == R.id.main_find_friends_option) {
            goToFindFriendsActivity();

        }
        if (item.getItemId() == R.id.main_settings_option) {
            goToSettingsActivity();
        }
        if (item.getItemId() == R.id.main_create_group_option) {
            requestNewGroup();
        }
        return true;

    }



    private void requestNewGroup() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        dialog.setTitle("Enter Group Name");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("zidan group");
        dialog.setView(groupNameField);
        dialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(MainActivity.this, "Please write Group Name", Toast.LENGTH_SHORT).show();
                }else{
                    createNewGroup(groupName);
                }
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
dialog.cancel();
            }
        });
        dialog.show();

    }

    private void createNewGroup(final String groupName) {
        reference.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, groupName + "is created Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(MainActivity.this, "Error"+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToSettingsActivity() {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));

    }

    private void goToFindFriendsActivity() {
        startActivity(new Intent(MainActivity.this, FindFriendsActivity.class));

    }

    private void updateUserStatus(String stat) {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        String pattern="MMM dd, yyyy";
        SimpleDateFormat currentDate = new SimpleDateFormat(pattern);
        saveCurrentDate = currentDate.format(calendar.getTime());
        String timepattern = "hh:mm a";
        SimpleDateFormat currentTime = new SimpleDateFormat(timepattern);
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();

        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", stat);

        currentUser_ID = auth.getCurrentUser().getUid().toString();

        reference.child("Users").child(currentUser_ID).child("userState")
                .updateChildren(onlineStateMap);



    }
}
