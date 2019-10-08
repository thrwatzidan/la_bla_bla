package com.dev.thrwat_zidan.la_bla_bla;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private String messagReciverID, messageReciverName, messageReciverImage,messageSenderID;

    private CircleImageView custom_profil_image;
    private TextView txt_custom_profile_name, txt_custom_profile_last_seen;

    private RecyclerView recycler_privet_chat;
    private ImageButton btn_send_message,btn_send_file;
    private EditText edt_input_message;
    private Toolbar chat_app_barlayout;
    private FirebaseAuth auth;
    private DatabaseReference reference;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager manager;
    private MessageAdapter adapter;
   private String saveCurrentTime, saveCurrentDate;
    private String checker = "",myUrl="";
    private Uri fileUri;
    private StorageTask uploadTask;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        auth = FirebaseAuth.getInstance();
        messageSenderID = auth.getCurrentUser().getUid();
        reference= FirebaseDatabase.getInstance().getReference();

        messagReciverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReciverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReciverImage = getIntent().getExtras().get("visit_user_image").toString();

        initViews();
        displayLastSeen();
        txt_custom_profile_name.setText(messageReciverName);
        Picasso.get().load(messageReciverImage).placeholder(R.drawable.profile_image__balck).into(custom_profil_image);

    }

    private void initViews() {

        recycler_privet_chat = findViewById(R.id.recycler_privet_chat);
        btn_send_message = findViewById(R.id.btn_send_message);
        edt_input_message = findViewById(R.id.edt_input_message);
        btn_send_file = findViewById(R.id.btn_send_file);

        chat_app_barlayout = findViewById(R.id.chat_app_barlayout);

        setSupportActionBar(chat_app_barlayout);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        custom_profil_image = findViewById(R.id.custom_profil_image);
        txt_custom_profile_name = findViewById(R.id.txt_custom_profile_name);
        txt_custom_profile_last_seen = findViewById(R.id.txt_custom_profile_last_seen);

        adapter = new MessageAdapter(messagesList);
        recycler_privet_chat = findViewById(R.id.recycler_privet_chat);
        manager = new LinearLayoutManager(this);
        recycler_privet_chat.setLayoutManager(manager);
        recycler_privet_chat.setAdapter(adapter);


        loading=new ProgressDialog(this);
        btn_send_message.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        String pattern="MMM dd, yyyy";
        SimpleDateFormat currentDate = new SimpleDateFormat(pattern);
        saveCurrentDate = currentDate.format(calendar.getTime());
        String timepattern = "hh:mm a";
        SimpleDateFormat currentTime = new SimpleDateFormat(timepattern);
        saveCurrentTime = currentTime.format(calendar.getTime());

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_send_message:
                sendMessage();
                break;
            case R.id.btn_send_file:
                sendFileAttach();
                break;
        }
    }

    private void sendFileAttach() {
        CharSequence sequence[] = new CharSequence[]{

                "Images",
                "PDF Files",
                "Ms Word Files"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);

        builder.setTitle("Select The File");
        builder.setItems(sequence, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (i == 0) {
                    checker = "image";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Select Image"),2020);
                }
                if (i == 1) {
                    checker = "pdf";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent.createChooser(intent,"Select Pdf File"),2020);

                }
                if (i == 2) {
                    checker = "docx";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/msword");
                    startActivityForResult(intent.createChooser(intent,"Select docx File"),2020);
                }


            }
        });
        builder.show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        refMessage();
    }


    private void displayLastSeen() {
        reference.child("Users").child(messagReciverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {

                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();

                            if (state.equals("online")) {
                                txt_custom_profile_last_seen.setText("Online");

                            } else if (state.equals("offline")) {
                                txt_custom_profile_last_seen.setText("Last Seen: " + "\n" + date + " "+time);

                            }

                        }else{

                            txt_custom_profile_last_seen.setText("offline");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage() {

        String messageText = edt_input_message.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "First write your message..", Toast.LENGTH_SHORT).show();
        }else{
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messagReciverID;
            String messageReciverRef = "Messages/" + messagReciverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = reference.child("Messages")
                    .child(messageSenderID).child(messagReciverID).push();

            String messagePushID = userMessageKeyRef.getKey();


            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messagReciverID);
            messageTextBody.put("message_id", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReciverRef + "/" + messagePushID, messageTextBody);
            reference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "Message Send Successfully", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ChatActivity.this, "Error in send message", Toast.LENGTH_SHORT).show();
                    }

                    edt_input_message.setText("");
                }
            });

        }
    }

    private void refMessage() {
reference.child("Messages").child(messageSenderID).child(messagReciverID)
        .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                adapter.notifyDataSetChanged();

                recycler_privet_chat.smoothScrollToPosition(recycler_privet_chat.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 2020 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            loading.setTitle("Sending File");
            loading.setMessage("Please wait ,we are sending that file...");
            loading.setCanceledOnTouchOutside(false);
            loading.show();

            fileUri = data.getData();
            if (!checker.equals("image")) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messagReciverID;
                final String messageReciverRef = "Messages/" + messagReciverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = reference.child("Messages")
                        .child(messageSenderID).child(messagReciverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            myUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();


                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messagReciverID);
                            messageTextBody.put("message_id", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReciverRef + "/" + messagePushID, messageTextBody);

                            reference.updateChildren(messageBodyDetails);
                            loading.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loading.dismiss();

                        Toast.makeText(ChatActivity.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loading.setMessage((int) p + " % Uploaded....");
                    }
                });

            }else if (checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messagReciverID;
                final String messageReciverRef = "Messages/" + messagReciverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = reference.child("Messages")
                        .child(messageSenderID).child(messagReciverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = (Uri) task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messagReciverID);
                            messageTextBody.put("message_id", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReciverRef + "/" + messagePushID, messageTextBody);
                            reference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        loading.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message Send Successfully", Toast.LENGTH_SHORT).show();
                                    }else{
                                        loading.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error in send message", Toast.LENGTH_SHORT).show();
                                    }

                                    edt_input_message.setText("");
                                }
                            });
                        }
                    }
                });

            }else{
                loading.dismiss();
                Toast.makeText(this, "Nothing Selected,Error.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
