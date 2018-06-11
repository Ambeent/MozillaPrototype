package com.wireless.ambeent.mozillaprototype.pojos;
/**
 * Created by Ambeent Wireless.
 */
//Same as the message object. This object is used to fetch and upload messages on one go
public class FirebaseMessageObject {

    private String message;

    private String sender;

    private long timestamp;

    public FirebaseMessageObject(String message, String sender, long timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
