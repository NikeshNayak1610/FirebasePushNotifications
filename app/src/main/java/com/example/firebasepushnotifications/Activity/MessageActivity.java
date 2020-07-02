package com.example.firebasepushnotifications.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.firebasepushnotifications.APIService;
import com.example.firebasepushnotifications.Adapter.MessageAdapter;
import com.example.firebasepushnotifications.Models.Chat;
import com.example.firebasepushnotifications.Models.User;
import com.example.firebasepushnotifications.Notification.Client;
import com.example.firebasepushnotifications.Notification.Data;
import com.example.firebasepushnotifications.Notification.MyResponse;
import com.example.firebasepushnotifications.Notification.Sender;
import com.example.firebasepushnotifications.Notification.Token;
import com.example.firebasepushnotifications.R;
import com.example.firebasepushnotifications.databinding.ActivityMessageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    ActivityMessageBinding messageBinding;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    ArrayList<Chat> chatsList = new ArrayList<>();
    MessageAdapter messageAdapter;

    ValueEventListener seenListener;
    String userid;

    APIService apiService;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageBinding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(messageBinding.getRoot());
        setSupportActionBar(messageBinding.toolbarId);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        messageBinding.toolbarId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        userid = getIntent().getStringExtra("userid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                messageBinding.usernameId.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    messageBinding.profileImage.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Picasso.get().load(user.getImageURL()).into(messageBinding.profileImage);
                }

                readMessages(firebaseUser.getUid(), userid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        messageBinding.sendId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                if (!messageBinding.textSend.getText().toString().equals("")) {
                    String msg = messageBinding.textSend.getText().toString();
                    sendMessage(firebaseUser.getUid(), userid, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                messageBinding.textSend.setText("");
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messageBinding.messageRecyclerViewId.setHasFixedSize(true);
        messageBinding.messageRecyclerViewId.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(this, chatsList);
        messageBinding.messageRecyclerViewId.setAdapter(messageAdapter);

        seenMessage(userid);
    }

    private void seenMessage(final String userId) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(final String sender, final String receiver, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = simpleDateFormat.format(Calendar.getInstance(Locale.getDefault()).getTime());
        hashMap.put("send_date", timestamp);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid()).child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg = message;

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify) {
                    Log.e(TAG, "Notify:" + notify);
                    sendNotification(receiver, user.getUsername(), user.getImageURL(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void currentUser(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences("CurrentUserPrefs", MODE_PRIVATE).edit();
        editor.putString("currentUser", userId);
        editor.apply();
    }

    private void sendNotification(final String receiver, final String username, final String imageURL, final String msg) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e(TAG, "DataSnapshot:" + snapshot.hasChildren());
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Log.e(TAG, "User => " + firebaseUser.getUid());
                    Log.e(TAG, "Body =>" + username + ": " + msg);
                    Log.e(TAG, "Sent To =>" + userid);

                    Data data = new Data(firebaseUser.getUid(), imageURL, username + ": " + msg, "New Message", userid);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {

                                if (response.body().success == 0) {
                                    Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readMessages(final String myid, final String userid) {

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid)
                            || chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                        chatsList.add(chat);
                    }
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void status(String status) {

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }
}