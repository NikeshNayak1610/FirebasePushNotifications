package com.example.firebasepushnotifications.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.firebasepushnotifications.Adapter.UserAdapter;
import com.example.firebasepushnotifications.Models.User;
import com.example.firebasepushnotifications.databinding.ActivityUserListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    ActivityUserListBinding binding;
    ArrayList<User> usersList = new ArrayList<>();
    UserAdapter userAdapter;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.userListRecyclerViewId.setLayoutManager(layoutManager);
        binding.userListRecyclerViewId.setHasFixedSize(true);
        userAdapter = new UserAdapter(this, usersList, false);
        binding.userListRecyclerViewId.setAdapter(userAdapter);
        readUsers();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void readUsers() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    assert firebaseUser != null;
                    if (!user.getId().equals(firebaseUser.getUid())) {
                        usersList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}