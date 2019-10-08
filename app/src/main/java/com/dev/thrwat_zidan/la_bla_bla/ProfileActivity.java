package com.dev.thrwat_zidan.la_bla_bla;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String reciverUserID, currentStatus, senderUserId;
    private CircleImageView img_profile;
    private TextView txt_user_name, txt_user_status;
    private Button btn_send_request, btn_decline_request;
    private FirebaseAuth auth;
    private DatabaseReference reference, chatRequestRef, contactRef, notificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        reciverUserID = getIntent().getExtras().get("user_id").toString();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        senderUserId = auth.getCurrentUser().getUid();

        initViews();

        retriveUserInfo();

    }

    private void initViews() {
        img_profile = findViewById(R.id.img_profile);
        txt_user_name = findViewById(R.id.txt_user_name);
        txt_user_status = findViewById(R.id.txt_user_status);
        btn_send_request = findViewById(R.id.btn_send_request);
        btn_decline_request = findViewById(R.id.btn_decline_request);

        currentStatus = "new";



    }

    private void retriveUserInfo() {
        reference.child(reciverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {

                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image__balck).into(img_profile);
                    txt_user_name.setText(userName);
                    txt_user_status.setText(userStatus);
                    manageChatRequest();

                } else {

                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    txt_user_name.setText(userName);
                    txt_user_status.setText(userStatus);

                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {

        chatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(reciverUserID)) {

                            String request_Type = dataSnapshot.child(reciverUserID).child("request_type")
                                    .getValue().toString();

                            if (request_Type.equals("sent")) {

                                currentStatus = "request_sent";
                                btn_send_request.setText("Cancel Chat request");

                            } else if (request_Type.equals("received")) {
                                currentStatus = "request_received";
                                btn_send_request.setText("Accept Chat Request");
                                btn_decline_request.setVisibility(View.VISIBLE);
                                btn_decline_request.setEnabled(true);
                                btn_decline_request.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        } else {
                            contactRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(reciverUserID)) {
                                                currentStatus = "Friends";
                                                btn_send_request.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        if (!senderUserId.equals(reciverUserID)) {
            btn_send_request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btn_send_request.setEnabled(false);
                    if (currentStatus.equals("new")) {
                        sendChatRequest();
                    }
                    if (currentStatus.equals("request_sent")) {
                        cancelChatRequest();
                    }

                    if (currentStatus.equals("request_received")) {
                        acceptChatRequest();
                    }
                    if (currentStatus.equals("Friends")) {
                        removeSpacificContent();
                    }
                }
            });

        } else {

            btn_send_request.setVisibility(View.INVISIBLE);
        }

    }

    private void removeSpacificContent() {
        contactRef.child(senderUserId).child(reciverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactRef.child(reciverUserID).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btn_send_request.setEnabled(true);
                                                currentStatus = "new";
                                                btn_send_request.setText("Send Message");


                                                btn_decline_request.setVisibility(View.INVISIBLE);
                                                btn_decline_request.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest() {
        contactRef.child(senderUserId).child(reciverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactRef.child(reciverUserID).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                chatRequestRef.child(senderUserId).child(reciverUserID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    chatRequestRef.child(reciverUserID).child(senderUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        btn_send_request.setEnabled(true);
                                                                                        currentStatus = "Frienda";
                                                                                        btn_send_request.setText("Remove this Contact");

                                                                                        btn_decline_request.setVisibility(View.INVISIBLE);
                                                                                        btn_decline_request.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest() {

        chatRequestRef.child(senderUserId).child(reciverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(reciverUserID).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btn_send_request.setEnabled(true);
                                                currentStatus = "new";
                                                btn_send_request.setText("Send Message");


                                                btn_decline_request.setVisibility(View.INVISIBLE);
                                                btn_decline_request.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(reciverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(reciverUserID).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> chatNotification_Map = new HashMap<>();

                                                chatNotification_Map.put("from", senderUserId);
                                                chatNotification_Map.put("type", "request");

                                                notificationRef.child(reciverUserID).push()
                                                        .setValue(chatNotification_Map)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    btn_send_request.setEnabled(true);
                                                                    currentStatus = "request_sent";
                                                                    btn_send_request.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });


                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
