package com.wireless.ambeent.mozillaprototype.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class MessageObject {

    @SerializedName("id")
    private String id;

    @SerializedName("message")
    private String message;

    @SerializedName("sender")
    private String sender;

    @SerializedName("receiver")
    private String receiver;

    @SerializedName("timestamp")
    private long timestamp;

    public MessageObject(String id, String message, String sender, long timestamp) {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public MessageObject(String id, String message, String sender, String receiver, long timestamp) {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "MessageObject{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageObject that = (MessageObject) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    public boolean hasReceiver(){

        return !receiver.equalsIgnoreCase("null");

    }

}
