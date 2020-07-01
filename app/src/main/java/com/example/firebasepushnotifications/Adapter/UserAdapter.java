package com.example.firebasepushnotifications.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasepushnotifications.Models.Chat;
import com.example.firebasepushnotifications.Activity.MessageActivity;
import com.example.firebasepushnotifications.R;
import com.example.firebasepushnotifications.Models.User;
import com.example.firebasepushnotifications.databinding.UserItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    boolean isChat;
    Context context;
    ArrayList<User> usersList;
    String theLastMessage;

    public UserAdapter(Context context, ArrayList<User> usersList, boolean isChat) {
        this.context = context;
        this.usersList = usersList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(UserItemBinding.inflate(LayoutInflater.from(context)));
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        final User user = usersList.get(position);
        holder.userItemBinding.usernameId.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.userItemBinding.profileImage.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.get().load(user.getImageURL()).into(holder.userItemBinding.profileImage);
        }

        if(isChat){
            lastMessage(user.getId(), holder.userItemBinding.lastMessage);
        }
        else{
            holder.userItemBinding.lastMessage.setVisibility(View.GONE);
        }

        if (isChat) {
            if (user.getStatus().equals("online")) {
                holder.userItemBinding.online.setVisibility(View.VISIBLE);
                holder.userItemBinding.offline.setVisibility(View.GONE);
            } else {
                holder.userItemBinding.online.setVisibility(View.GONE);
                holder.userItemBinding.offline.setVisibility(View.VISIBLE);
            }
        } else {
            holder.userItemBinding.online.setVisibility(View.GONE);
            holder.userItemBinding.offline.setVisibility(View.GONE);
        }

        holder.userItemBinding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        UserItemBinding userItemBinding;

        public UserViewHolder(UserItemBinding binding) {
            super(binding.getRoot());
            this.userItemBinding = binding;
        }
    }


    private void lastMessage(final String userId, final TextView last_msg) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)
                            || chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) {
                        theLastMessage = chat.getMessage();
                    }
                }

                if ("default".equals(theLastMessage)) {
                    last_msg.setText("No Message");
                } else {
                    last_msg.setText(theLastMessage);
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
