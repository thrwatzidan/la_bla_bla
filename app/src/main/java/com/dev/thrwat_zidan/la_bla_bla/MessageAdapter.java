package com.dev.thrwat_zidan.la_bla_bla;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> messagesList;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_reciver_message, txt_sender_message;
        public CircleImageView img_message_profile;
        public ImageView message_reciver_img, message_sender_img;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_reciver_message = itemView.findViewById(R.id.txt_reciver_message);
            txt_sender_message = itemView.findViewById(R.id.txt_sender_message);
            img_message_profile = itemView.findViewById(R.id.img_message_profile);
            message_sender_img = itemView.findViewById(R.id.message_sender_img);
            message_reciver_img = itemView.findViewById(R.id.message_reciver_img);
        }
    }

    public MessageAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_message_layout, parent, false);
        auth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String messageSenderID = auth.getCurrentUser().getUid();
        Messages messages = messagesList.get(position);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String reciverProfile_Img = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(reciverProfile_Img).placeholder(R.drawable.profile_image__balck).into(holder.img_message_profile);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.txt_reciver_message.setVisibility(View.GONE);
        holder.img_message_profile.setVisibility(View.GONE);
        holder.txt_sender_message.setVisibility(View.GONE);
        holder.message_sender_img.setVisibility(View.GONE);
        holder.message_reciver_img.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {


            if (fromUserID.equals(messageSenderID)) {

                holder.txt_sender_message.setVisibility(View.VISIBLE);


                holder.txt_sender_message.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.txt_sender_message.setTextColor(Color.BLACK);
                holder.txt_sender_message.setText(messages.getMessage() + "\n \n " + messages.getTime() + " - " + messages.getDate());
            } else {

                holder.img_message_profile.setVisibility(View.VISIBLE);
                holder.txt_reciver_message.setVisibility(View.VISIBLE);

                holder.txt_reciver_message.setBackgroundResource(R.drawable.reciver_messages_layout);
                holder.txt_reciver_message.setTextColor(Color.BLACK);
                holder.txt_reciver_message.setText(messages.getMessage() + "\n \n " + messages.getTime() + " - " + messages.getDate());
            }
        } else if (fromMessageType.equals("image")) {

            if (fromUserID.equals(messageSenderID)) {

                holder.message_sender_img.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.message_sender_img);
            } else {
                holder.message_reciver_img.setVisibility(View.VISIBLE);
                holder.img_message_profile.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.message_reciver_img);
            }

        } else if (fromMessageType.equals("docx")) {

            if (fromUserID.equals(messageSenderID)) {

                holder.message_sender_img.setVisibility(View.VISIBLE);
                holder.message_sender_img.setBackgroundResource(R.drawable.ic_insert_drive_file_black_24dp);


            } else {
                holder.message_reciver_img.setVisibility(View.VISIBLE);
                holder.img_message_profile.setVisibility(View.VISIBLE);
                holder.message_reciver_img.setBackgroundResource(R.drawable.ic_insert_drive_file_black_24dp);


            }
        } else if (fromMessageType.equals("pdf")) {
            if (fromUserID.equals(messageSenderID)) {

                holder.message_sender_img.setVisibility(View.VISIBLE);
                holder.message_sender_img.setBackgroundResource(R.drawable.pdf_icon);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });


            } else {
                holder.message_reciver_img.setVisibility(View.VISIBLE);
                holder.img_message_profile.setVisibility(View.VISIBLE);
                holder.message_reciver_img.setBackgroundResource(R.drawable.pdf_icon);


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }


        if (fromUserID.equals(messageSenderID)) {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messagesList.get(position).getType().equals("pdf") ||
                            messagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View This Document",
                                        "Cancel",
                                        "Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    delteSentMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 2) {

                                } else if (i == 3) {
                                    delteMessagesForEveryOne(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (messagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel",
                                        "Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    delteSentMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 2) {
                                    delteMessagesForEveryOne(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (messagesList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "View This Image",
                                        "Cancel",
                                        "Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    delteSentMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 1) {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewer.class);
                                    intent.putExtra("url", messagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 3) {
                                    delteMessagesForEveryOne(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messagesList.get(position).getType().equals("pdf") || messagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View This Document",
                                        "Cancel",
                                        // "Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    delteReceivedMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 2) {

                                }

                            }
                        });
                        builder.show();
                    } else if (messagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel",
                                        //"Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    delteReceivedMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

//
//                                else if (position == 2) {
//
//                                }
                            }
                        });
                        builder.show();
                    } else if (messagesList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "View This Image",
                                        "Cancel",
                                        // "Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    delteReceivedMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 1) {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewer.class);
                                    intent.putExtra("url", messagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

//                                else if (position == 3) {
//
//                                }

                            }
                        });
                        builder.show();
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    private void delteSentMessages(final int position, final MessageViewHolder holder) {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        root.child("Messages")
                .child(messagesList.get(position).getFrom())
                .child(messagesList.get(position).getTo())
                .child(messagesList.get(position).getMessage_id())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(holder.itemView.getContext(), "Error" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void delteReceivedMessages(final int position, final MessageViewHolder holder) {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        root.child("Messages")
                .child(messagesList.get(position).getTo())
                .child(messagesList.get(position).getFrom())
                .child(messagesList.get(position).getMessage_id())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(holder.itemView.getContext(), "Error" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void delteMessagesForEveryOne(final int position, final MessageViewHolder holder) {
        final DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        root.child("Messages")
                .child(messagesList.get(position).getTo())
                .child(messagesList.get(position).getFrom())
                .child(messagesList.get(position).getMessage_id())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    root.child("Messages")
                            .child(messagesList.get(position).getFrom())
                            .child(messagesList.get(position).getTo())
                            .child(messagesList.get(position).getMessage_id())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                } else {

                    Toast.makeText(holder.itemView.getContext(), "Error" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
