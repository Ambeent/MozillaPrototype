package com.wireless.ambeent.mozillaprototype.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wireless.ambeent.mozillaprototype.activities.MainActivity;
import com.wireless.ambeent.mozillaprototype.pojos.FirebaseMessageObject;
import com.wireless.ambeent.mozillaprototype.pojos.MessageObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by Ambeent Wireless.
 */
public class FirebaseDatabaseHelper {

    private static final String TAG = "FirebaseDatabaseHelper";

    private static DatabaseReference dbMessageContentReference;
    private static DatabaseReference dbUsersMessagesReference;


    public static synchronized DatabaseReference getMessageContentInstance() {

        if (dbMessageContentReference == null) {
            Log.i(TAG, "getMessageContentInstance: init");
            dbMessageContentReference = FirebaseDatabase.getInstance().getReference().child("mozillaPrototype").child("messageContent");
        }

        return dbMessageContentReference;
    }

    public static synchronized DatabaseReference getUsersMessagesInstance() {

        if (dbUsersMessagesReference == null) {
            Log.i(TAG, "getUsersMessagesInstance: init");
            dbUsersMessagesReference = FirebaseDatabase.getInstance().getReference().child("mozillaPrototype").child("usersMessages");
        }

        return dbUsersMessagesReference;
    }


    //TODO: optimize this with timestamps...
    //Gets every targeted message from SQLite database and pushes them to Firebase Database
    public static void pushMessagesToFirebase(Context mContext) {

        //Get every targeted message from SQLite
        ArrayList<MessageObject> messageObjects = DatabaseHelper.getTargetedMessagesFromSQLite(mContext);

        //Push the values to Firebase database
        for (MessageObject messageObject : messageObjects) {

            pushMessageToFirebase(messageObject);

        }

    }

    //Push the given message to Firebase
    public static void pushMessageToFirebase(MessageObject messageObject) {

        String id = messageObject.getId();
        String message = messageObject.getMessage();
        String sender = messageObject.getSender();
        String senderNickname = messageObject.getSenderNickname();
        String receiver = messageObject.getReceiver();
        long timestamp = messageObject.getTimestamp();

        Log.i(TAG, "pushMessageToFirebase: TIMESTAMP TEST " + timestamp);

        //The message id is the key
        Map<String,Object> taskMap = new HashMap<String,Object>();
        taskMap.put("message", message);
        taskMap.put("sender", sender);
        taskMap.put("senderNickname", senderNickname);
        taskMap.put("receiver", receiver);
        taskMap.put("timestamp", timestamp);

        getMessageContentInstance().child(id).updateChildren(taskMap);

        /*getMessageContentInstance().child(id).child("message").setValue(message);
        getMessageContentInstance().child(id).child("sender").setValue(sender);
        getMessageContentInstance().child(id).child("receiver").setValue(receiver);
        getMessageContentInstance().child(id).child("timestamp").setValue(timestamp);*/

        //The receiver is the main the key
        FirebaseMessageObject firebaseMessageObject = new FirebaseMessageObject(message, sender, timestamp);

        getUsersMessagesInstance().child(receiver).child(id).setValue(firebaseMessageObject);

    }

    //Listens to the messages that are send to users phone number
    public static synchronized void initPrivateMessageListener(final Context context) {

       /* Query query = getUsersMessagesInstance().child(Constants.PHONE_NUMBER).orderByChild("timestamp");

        //Add listener to phone number
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Log.i(TAG, "onChildAdded: " + dataSnapshot);

                //Create the received message object
                String id = dataSnapshot.getKey();
                String message = dataSnapshot.child("message").getValue(String.class);
                String sender = dataSnapshot.child("sender").getValue(String.class);
                long timestamp = dataSnapshot.child("timestamp").getValue(long.class);

                MessageObject receivedMessage = new MessageObject(id, message, sender, Constants.PHONE_NUMBER, timestamp);

                //Insert message to local database
                boolean isNewMessage = DatabaseHelper.insertMessageToSQLite(context, receivedMessage);

                //Notify the chat adapter if the app is visible
                if (MainActivity.isVisible && isNewMessage) {
                    ((MainActivity) context).getmChatHandler().updateMessageList(receivedMessage);
                    ((MainActivity) context).notifyChatAdapter();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Log.i(TAG, "onChildChanged: ");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }

    //
    public static void fetchEveryMessage(final Context context) {

        Query query = getMessageContentInstance().orderByChild("timestamp");

        //Add listener to phone number
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "fetchEveryMessage: " + dataSnapshot);

                //Create the received message object
                String id = dataSnapshot.getKey();
                String message = dataSnapshot.child("message").getValue(String.class);
                String sender = dataSnapshot.child("sender").getValue(String.class);
                String senderNickname = dataSnapshot.child("senderNickname").getValue(String.class);
                String receiver = dataSnapshot.child("receiver").getValue(String.class);
                long timestamp = dataSnapshot.child("timestamp").getValue(long.class);

                MessageObject receivedMessage = new MessageObject(id, message, sender, senderNickname, receiver, timestamp);

                //Insert message to local database
                boolean isNewMessage = DatabaseHelper.insertMessageToSQLite(context, receivedMessage);

                if(isNewMessage && receiver.equalsIgnoreCase(Constants.PHONE_NUMBER)){
                    MainActivity.mChatHandler.updateMessageList(receivedMessage);
                    ((MainActivity) context).notifyChatAdapter();
                }


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
