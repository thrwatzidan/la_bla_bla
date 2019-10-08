package com.dev.thrwat_zidan.la_bla_bla;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {
    private View privetChatView;
    private RecyclerView chats_list_recycler;
    private DatabaseReference chatsRef,UsersRef;
    private String currentUser_ID ;
    private FirebaseAuth auth;



    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privetChatView = inflater.inflate(R.layout.fragment_chats, container, false);

        auth = FirebaseAuth.getInstance();
        currentUser_ID = auth.getCurrentUser().getUid();

        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUser_ID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chats_list_recycler = privetChatView.findViewById(R.id.chats_list_recycler);
        chats_list_recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        return privetChatView;
    }


    @Override
    public void onStart() {
        super.onStart();
        viewChatsList();
    }

    private void viewChatsList() {

        FirebaseRecyclerOptions<Contacts>
                options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHOlder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHOlder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHOlder chatsViewHOlder, int i, @NonNull Contacts contacts) {

                        final String usersIDs = getRef(i).getKey();
                        final String[] retImage = {"defult_image"};

                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    if (dataSnapshot.hasChild("image")) {
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image__balck).into(chatsViewHOlder.profile_image);

                                    }

                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();

                                    chatsViewHOlder.txt_user_profile_name.setText(retName);
                                    chatsViewHOlder.txt_user_profile_status.setText("Last Seen: " + "\n" + "Data " + " Time");



                                    if (dataSnapshot.child("userState").hasChild("state")) {
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();

                                        if (state.equals("online")) {
                                            chatsViewHOlder.txt_user_profile_status.setText("Online");

                                        } else if (state.equals("offline")) {
                                            chatsViewHOlder.txt_user_profile_status.setText("Last Seen: " + "\n" + date + " "+time);

                                        }

                                    }else{

                                        chatsViewHOlder.txt_user_profile_status.setText("offline");

                                    }


                                    chatsViewHOlder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", usersIDs);
                                            chatIntent.putExtra("visit_user_name", retName);
                                            chatIntent.putExtra("visit_user_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHOlder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout, parent, false);
                        ChatsViewHOlder viewHOlder = new ChatsViewHOlder(view);
                        return viewHOlder;
                    }
                };

        chats_list_recycler.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ChatsViewHOlder extends RecyclerView.ViewHolder {


        TextView txt_user_profile_name, txt_user_profile_status;
        CircleImageView profile_image;
        Button btn_cancel_request, btn_accept_request;

        public ChatsViewHOlder(@NonNull View itemView) {
            super(itemView);

            txt_user_profile_name = itemView.findViewById(R.id.txt_user_profile_name);
            txt_user_profile_status = itemView.findViewById(R.id.txt_user_profile_status);
            profile_image = itemView.findViewById(R.id.img_users);
            btn_accept_request = itemView.findViewById(R.id.btn_accept_request);
            btn_cancel_request = itemView.findViewById(R.id.btn_cancel_request);
        }
    }
}
