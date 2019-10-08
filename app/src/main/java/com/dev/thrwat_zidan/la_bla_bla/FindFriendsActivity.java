package com.dev.thrwat_zidan.la_bla_bla;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar find_friends_toolbar;
    private RecyclerView find_friend_recycler;
    private DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        initViews();

        setSupportActionBar(find_friends_toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }

    private void initViews() {
        find_friend_recycler = findViewById(R.id.find_friend_recycler);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        find_friend_recycler.setLayoutManager(manager);
        find_friend_recycler.setHasFixedSize(true);

        find_friends_toolbar = findViewById(R.id.find_friends_toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(reference, Contacts.class)
                        .build();

        Log.i("FIND_TAG", options.toString());

        FirebaseRecyclerAdapter<Contacts,FindFreindViewHolder>adapter=
                new FirebaseRecyclerAdapter<Contacts, FindFreindViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFreindViewHolder holder, final int position, @NonNull Contacts model) {


                   holder.txt_user_profile_name.setText(model.getName());
                   holder.txt_user_profile_status.setText(model.getStatus());
                   Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image__balck).into(holder.profile_image);


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String user_id = getRef(position).getKey();
                        Intent intent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        intent.putExtra("user_id", user_id);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFreindViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_layout, viewGroup, false);
                return new FindFreindViewHolder(view);
            }
        };

        find_friend_recycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFreindViewHolder extends RecyclerView.ViewHolder {

         TextView txt_user_profile_name, txt_user_profile_status;
        CircleImageView profile_image;

        public FindFreindViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_user_profile_name = itemView.findViewById(R.id.txt_user_profile_name);
            txt_user_profile_status = itemView.findViewById(R.id.txt_user_profile_status);
            profile_image = itemView.findViewById(R.id.img_users);
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
