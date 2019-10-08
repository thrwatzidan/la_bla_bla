package com.dev.thrwat_zidan.la_bla_bla;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar group_chat_bar_layout;
    private ImageView btn_img_send;
    private EditText edt_group_message;
    private ScrollView my_scroll_view;
    private TextView txt_group_chat_display;
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    private FirebaseAuth auth;
    private DatabaseReference usersRef, groupNameRefr, groupMessageKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getStringExtra("groupName").toString();

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRefr = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        initViews();
        getUserInfo();

    }


    private void initViews() {
        group_chat_bar_layout = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(group_chat_bar_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(currentGroupName);
        }

        btn_img_send = findViewById(R.id.btn_img_send);
        edt_group_message = findViewById(R.id.edt_group_message);
        my_scroll_view = findViewById(R.id.my_scroll_view);
        txt_group_chat_display = findViewById(R.id.txt_group_chat_display);

        btn_img_send.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_img_send:
                saveMessageToDatabase();
                edt_group_message.setText("");
                my_scroll_view.fullScroll(View.FOCUS_DOWN);
                break;
        }
    }


    private void getUserInfo() {

        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void saveMessageToDatabase() {

        String message = edt_group_message.getText().toString();
        String messageKey = groupNameRefr.push().getKey();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write message first", Toast.LENGTH_SHORT).show();
        } else {

            Calendar calendar = Calendar.getInstance();
            String pattern = "MMM dd, yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            currentDate = simpleDateFormat.format(calendar.getTime());


            Calendar calendar_time = Calendar.getInstance();
            String pattern_time = "hh:mm a";
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(pattern_time);
            currentTime = simpleTimeFormat.format(calendar_time.getTime());

            HashMap<String, Object> groupMesaageKey = new HashMap<>();
            groupNameRefr.updateChildren(groupMesaageKey);
            groupMessageKeyRef = groupNameRefr.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);


        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRefr.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayMessages(DataSnapshot dataSnapshot) {

        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
            txt_group_chat_display.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n\n\n");
            my_scroll_view.fullScroll(View.FOCUS_DOWN);
        }

    }
}
