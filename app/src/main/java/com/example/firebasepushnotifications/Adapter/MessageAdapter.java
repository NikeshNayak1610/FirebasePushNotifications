package com.example.firebasepushnotifications.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasepushnotifications.Models.Chat;
import com.example.firebasepushnotifications.databinding.ChatItemLeftBinding;
import com.example.firebasepushnotifications.databinding.ChatItemRightBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    Context context;
    ArrayList<Chat> chatsList;
    FirebaseUser firebaseUser;

    public MessageAdapter(Context context, ArrayList<Chat> chatsList) {
        this.context = context;
        this.chatsList = chatsList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_LEFT) {
            return new ChatLeftViewHolder(ChatItemLeftBinding.inflate(LayoutInflater.from(context)));
        } else {
            return new ChatRightViewHolder(ChatItemRightBinding.inflate(LayoutInflater.from(context)));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Chat chat = chatsList.get(position);

        if (holder instanceof ChatLeftViewHolder) {
            ChatLeftViewHolder chatLeftViewHolder = (ChatLeftViewHolder) holder;
            chatLeftViewHolder.leftBinding.friendMessageTextViewId.setText(chat.getMessage());
        } else {
            ChatRightViewHolder chatRightViewHolder = (ChatRightViewHolder) holder;
            chatRightViewHolder.rightBinding.userMessageTextViewId.setText(chat.getMessage());
            if (chat.isIsseen()) {
                chatRightViewHolder.rightBinding.seen.setVisibility(View.VISIBLE);
                chatRightViewHolder.rightBinding.delivered.setVisibility(View.GONE);
            } else {
                chatRightViewHolder.rightBinding.delivered.setVisibility(View.VISIBLE);
                chatRightViewHolder.rightBinding.seen.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatsList.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    static class ChatLeftViewHolder extends RecyclerView.ViewHolder {

        ChatItemLeftBinding leftBinding;

        public ChatLeftViewHolder(ChatItemLeftBinding leftBinding) {
            super(leftBinding.getRoot());
            this.leftBinding = leftBinding;
        }
    }

    static class ChatRightViewHolder extends RecyclerView.ViewHolder {

        ChatItemRightBinding rightBinding;

        public ChatRightViewHolder(ChatItemRightBinding rightBinding) {
            super(rightBinding.getRoot());
            this.rightBinding = rightBinding;
        }
    }

}
