package com.wireless.ambeent.mozillaprototype.businesslogic;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.wireless.ambeent.mozillaprototype.R;
import com.wireless.ambeent.mozillaprototype.activities.MainActivity;
import com.wireless.ambeent.mozillaprototype.helpers.ActivityHelpers;
import com.wireless.ambeent.mozillaprototype.helpers.Constants;
import com.wireless.ambeent.mozillaprototype.helpers.DatabaseHelper;
import com.wireless.ambeent.mozillaprototype.helpers.FirebaseDatabaseHelper;
import com.wireless.ambeent.mozillaprototype.httprequests.IRest;
import com.wireless.ambeent.mozillaprototype.httprequests.RetrofitRequester;
import com.wireless.ambeent.mozillaprototype.pojos.ConnectedDeviceObject;
import com.wireless.ambeent.mozillaprototype.pojos.MessageObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.wireless.ambeent.mozillaprototype.server.ServerController.postRequest;
/**
 * Created by Ambeent Wireless.
 */
public class ChatHandler {

    private static final String TAG = "ChatHandler";

    private Context mContext;
    private List<MessageObject> mMessages;
    private HashSet<ConnectedDeviceObject> mConnectedDeviceList;

    //WifiManager for general purposes
    private WifiManager mWifiManager;


    //To prevent multiple sending of the messages,
    public static long lastMessaceSyncTimestmap = 0;

    //Holds the ssid of the network that lastly synced. When this changes, it means a new sync will be needed with other devices
    public static String lastSyncedSsid = "";

    //Contains the mac addresses of the devices that are already synced in this network.
    public static Set<String> syncedDeviceMacSet = new HashSet<>();

    public ChatHandler(Context mContext, List<MessageObject> mMessages, HashSet<ConnectedDeviceObject> mConnectedDeviceList) {
        this.mContext = mContext;
        this.mMessages = mMessages;
        this.mConnectedDeviceList = mConnectedDeviceList;

        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //Show the messages when constructed
        updateMessageList(DatabaseHelper.getMessagesFromSQLite(mContext));

        //Every three seconds, check whether the user is connected to a hotspot, if he is, sync the messages if necessary
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                syncEveryMessageWithNetwork();
                handler.postDelayed(this, 10000);
            }
        }, 10000);
    }


    //Put the necessary methods in order to send a message
    public void sendMessage(String message) {

        MessageObject messageObject = createMessageObject(message);
        DatabaseHelper.insertMessageToSQLite(mContext, messageObject);

        //Update message list by this message
        updateMessageList(messageObject);

        //Send this message to other devices that are connected
        ArrayList<MessageObject> newMessage = new ArrayList<>();
        newMessage.add(messageObject);
        postMessagesToNetwork(newMessage);

        Log.i(TAG, "sendMessage: " + messageObject);

        //If there is internet connection and if the message is a targeted message, push the message to Firebase Database
        if(messageObject.hasReceiver()) FirebaseDatabaseHelper.pushMessageToFirebase(messageObject);
    }

    //Send the given message list to everyone in the same network
    private void postMessagesToNetwork(ArrayList<MessageObject> outgoingMessageList) {

        for (ConnectedDeviceObject connectedDeviceObject : mConnectedDeviceList) {

            Log.i(TAG, "postMessagesToNetwork: Sending message to: " + connectedDeviceObject.getIpAddress());

      //      Toast.makeText(mContext, "Posting " +outgoingMessageList.size() + " messages to " +connectedDeviceObject.getIpAddress(), Toast.LENGTH_SHORT).show();


            String ipAddress = "http://" + connectedDeviceObject.getIpAddress() + ":8000/";
            IRest taskService = RetrofitRequester.createService(IRest.class, ipAddress);
            Call<ResponseBody> postCall = taskService.sendMessagesToNetwork(outgoingMessageList);

            postRequest(postCall);
        }
    }

    //Send the given message list to a target ip address in the network
    private void postMessagesToTarget(ArrayList<MessageObject> outgoingMessageList, String ipAddress) {

    //    Toast.makeText(mContext, "Posting " +outgoingMessageList.size() + " messages to " +ipAddress, Toast.LENGTH_SHORT).show();

        String fullAddress = "http://" + ipAddress + ":8000/";
        IRest taskService = RetrofitRequester.createService(IRest.class, fullAddress);
        Call<ResponseBody> postCall = taskService.sendMessagesToNetwork(outgoingMessageList);

        postRequest(postCall);

    }

    //When user connects to a network, he must sync his messages by sending all of them to network
    private void syncEveryMessageWithNetwork() {

        //We are not connected to a hotspot. Reset the fields about syncing and return
        if (mWifiManager.getConnectionInfo() == null || !((MainActivity)mContext).getmWifiApController().isConnectedToAmbeentMozillaHotspot()) {
            lastSyncedSsid = "";
            syncedDeviceMacSet.clear();
            return;
        }

        //Get connected ssid to compare with last synced network
        String connectedSsid = mWifiManager.getConnectionInfo().getSSID();

        //Recently connected to a network that is not synced with this device. Clear the syncedDeviceMacSet and prepare for a new sync
        if (!connectedSsid.equalsIgnoreCase(lastSyncedSsid)) {
            lastSyncedSsid = connectedSsid;
            syncedDeviceMacSet.clear();
        }

        //Create a dummy list to prevent multiple thread calls to the list
        ArrayList<ConnectedDeviceObject> connectedDeviceObjects = new ArrayList<>();
        connectedDeviceObjects.addAll(mConnectedDeviceList);

        //Start syncing
        for (ConnectedDeviceObject connectedDeviceObject : connectedDeviceObjects) {

            //Already synced with this device
            if (syncedDeviceMacSet.contains(connectedDeviceObject.getMacAddress())) continue;

            //Get every message from database and prepare the list
            ArrayList<MessageObject> allMessages = DatabaseHelper.getTargetedMessagesFromSQLite(mContext);


            if(allMessages.size() < 1) {
                //Add the device to synced address set
                syncedDeviceMacSet.add(connectedDeviceObject.getMacAddress());
                continue; //No messages, do not post empty list
            }

            postMessagesToTarget(allMessages, connectedDeviceObject.getIpAddress());

            Log.i(TAG, "syncEveryMessageWithNetwork: Synced with " + connectedDeviceObject.getIpAddress());

            //Add the device to synced address set
            syncedDeviceMacSet.add(connectedDeviceObject.getMacAddress());

        }


    }


    //Updates message List with the given message list
    public void updateMessageList(ArrayList<MessageObject> savedMessages) {

        //Check the message sender and receiver. If one of them matches with this users phone number or is a group message, add it to list to show on the screen.
        for (MessageObject messageObject : savedMessages) {
            if (messageObject.getReceiver().equalsIgnoreCase(Constants.PHONE_NUMBER)
                    || messageObject.getSender().equalsIgnoreCase(Constants.PHONE_NUMBER)
                    || messageObject.getReceiver().equalsIgnoreCase(mContext.getResources().getString(R.string.message_with_no_receiver)))
                mMessages.add(messageObject);
        }

        ((MainActivity) mContext).notifyChatAdapter();

    }

    //Updates the message list with only the given object
    public void updateMessageList(MessageObject message) {

        //Check the message sender and receiver. If one of them matches with this users phone number or is a group message, add it to list to show on the screen.
        if (message.getReceiver().equalsIgnoreCase(Constants.PHONE_NUMBER)
                || message.getSender().equalsIgnoreCase(Constants.PHONE_NUMBER)
                || message.getReceiver().equalsIgnoreCase(mContext.getResources().getString(R.string.message_with_no_receiver))) mMessages.add(message);

        ((MainActivity) mContext).notifyChatAdapter();

    }

    //This method parses the message and determines if the message has a receiver or a group message.
    //Then creates a suitable MessageObject to be sent and returns it.
    private MessageObject createMessageObject(String message) {

        //Create a globally unique key.
        String randomUUID = UUID.randomUUID().toString();

        //Get senders phone number from SharedPreferences
        String sender = Constants.PHONE_NUMBER;

        //Create a placeholder receiver string. If it stays the same, then it is a group message
        String receiver = mContext.getResources().getString(R.string.message_with_no_receiver);

        //If the message is not targeted, then message and actualMessage are the same.
        //If the message is targeted, the targeting part will be removed
        String actualMessage = message;

        //If the message starts with '@' then it is most likely a targeted message.
        char firstChar = message.charAt(0);
        boolean isTargetedMessage = firstChar == '@';

        if (isTargetedMessage) {
            //TODO: We are cheating here by using only turkish phone numbers right now. fix it

            //Get the target of the message by parsing the message
            receiver = message.substring(1, 14);
            actualMessage = message.substring(15, message.length());

        }

        long timestamp = ActivityHelpers.getCurrentTimeSeconds();

        MessageObject messageObject = new MessageObject(randomUUID, actualMessage, sender, receiver, timestamp);

        //    Log.i(TAG, "createMessageObject: " +messageObject.toString());

        return messageObject;
    }

}
