package com.example.firebasepushnotifications.Models;

public class Chat {

    String sender;
    String receiver;
    String message;
    String send_date;
    boolean isseen;

    public Chat(String sender, String receiver, String message, String send_date, boolean isseen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.send_date = send_date;
        this.isseen = isseen;
    }

    public Chat() {
    }

    public String getSend_date() {
        return send_date;
    }

    public void setSend_date(String send_date) {
        this.send_date = send_date;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
