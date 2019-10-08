package com.dev.thrwat_zidan.la_bla_bla;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView contacts_recycler;
    private DatabaseReference reference,userREf;
    private FirebaseAuth auth;
    private String currentUserID;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        contacts_recycler = contactsView.findViewById(R.id.contects_recycler);
        contacts_recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();

        reference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userREf = FirebaseDatabase.getInstance().getReference().child("Users");
        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(reference,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int i, @NonNull Contacts contacts) {
                String userIDs = getRef(i).getKey();
                userREf.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();

                                if (state.equals("online")) {
                                    holder.img_user_online.setVisibility(View.VISIBLE);
                                } else if (state.equals("offline")) {
                                    holder.img_user_online.setVisibility(View.INVISIBLE);
                                }
                            }else{
                                holder.img_user_online.setVisibility(View.INVISIBLE);

                            }

                            if (dataSnapshot.hasChild("image")) {

                                String profileImage = dataSnapshot.child("image").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.txt_user_profile_name.setText(profileName);
                                holder.txt_user_profile_status.setText(profileStatus);
                                Picasso.get().load(profileImage).placeholder(R.drawable.profile_image__balck).into(holder.profile_image);
                            }
                            else{
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.txt_user_profile_name.setText(profileName);
                                holder.txt_user_profile_status.setText(profileStatus);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        contacts_recycler.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView txt_user_profile_name, txt_user_profile_status;
        CircleImageView profile_image;
        ImageView img_user_online;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_user_profile_name = itemView.findViewById(R.id.txt_user_profile_name);
            txt_user_profile_status = itemView.findViewById(R.id.txt_user_profile_status);
            profile_image = itemView.findViewById(R.id.img_users);
            img_user_online = itemView.findViewById(R.id.img_user_online);
        }
    }
}
