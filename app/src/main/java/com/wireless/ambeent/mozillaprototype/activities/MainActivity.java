package com.wireless.ambeent.mozillaprototype.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.wireless.ambeent.mozillaprototype.R;
import com.wireless.ambeent.mozillaprototype.adapters.ChatAdapter;
import com.wireless.ambeent.mozillaprototype.businesslogic.ChatHandler;
import com.wireless.ambeent.mozillaprototype.businesslogic.WifiApController;
import com.wireless.ambeent.mozillaprototype.businesslogic.WifiScanner;
import com.wireless.ambeent.mozillaprototype.customviews.CustomRecyclerView;
import com.wireless.ambeent.mozillaprototype.customviews.EditTextV2;
import com.wireless.ambeent.mozillaprototype.helpers.Constants;
import com.wireless.ambeent.mozillaprototype.helpers.FirebaseDatabaseHelper;
import com.wireless.ambeent.mozillaprototype.pojos.ConnectedDeviceObject;
import com.wireless.ambeent.mozillaprototype.pojos.MessageObject;
import com.wireless.ambeent.mozillaprototype.server.ServerController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Ambeent Wireless.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static boolean isVisible = false;

    //Chat RecyclerView adapter
    private static ChatAdapter mChatAdapter;

    //The set that contains the messages to be shown on the screen
    private List<MessageObject> mMessages = new ArrayList<>();

    //The list that contains the IP address of other devices that are connected to hotspot
    private HashSet<ConnectedDeviceObject> mHotspotNeighboursList = new HashSet<>();

    //The class that parses messages, insert them to local database and send them.
    public static ChatHandler mChatHandler;

    //The class that controls Hotspot and finds connected devices
    private WifiApController mWifiApController;

    //The class that handles hotspot detection
    private WifiScanner mWifiScanner;

    //A flag the check whether the user is connected to a hotspot that is created by the app
    private boolean isConnectedToAppHotspot = false;

    //List of the hotspots that are created by the app and scanned
    private List<ScanResult> mHotspotList = new ArrayList<>();

    //General purpose handler
    private Handler mHandler;

    //The switch view that controls hotspot
    private SwitchCompat mSwitchCompat;

    //SharedPreferences...
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);


        activityInitialization();



    }

    //View and class initializations
    private void activityInitialization() {

        //Set up the PHONE_NUMBER for globall access
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE);
        Constants.PHONE_NUMBER = sharedPreferences.getString(Constants.USER_PHONE_NUMBER, "");

        //Show the saved user nickname.
        Constants.NICKNAME = sharedPreferences.getString(Constants.CURRENT_USER_USERNAME, Constants.PHONE_NUMBER);
        ((TextView)ButterKnife.findById(this, R.id.textView_Nickname)).setText(Constants.NICKNAME);
        Log.i(TAG, "onCreate: PHONE NUMBER: " + Constants.PHONE_NUMBER);



        //Toolbar setup
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mHandler = new Handler();

        //Initializing chat recyclerview
        CustomRecyclerView mChatRecyclerView = ButterKnife.findById(this, R.id.recyclerView_Chat);
        mChatRecyclerView.setShouldIgnoreTouch(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        mChatAdapter = new ChatAdapter(this, mMessages);
        mChatRecyclerView.setLayoutManager(mLayoutManager);
        mChatRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mChatRecyclerView.setAdapter(mChatAdapter);

        //ChatHandler init
        mChatHandler = new ChatHandler(this, mMessages, mHotspotNeighboursList);

        //WifiApController init
        mWifiApController = new WifiApController(this, mHotspotNeighboursList);

        //WifiScanner init
        mWifiScanner = new WifiScanner(this, mHotspotList);
        mWifiScanner.startScanning(this);

        //Switch view that controls hotspot
         mSwitchCompat = ButterKnife.findById(this, R.id.switch_Hotspot);
        mSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Enabling and disabling hotspot is long. Disable the switch for five seconds to prevent spamming
                mSwitchCompat.setEnabled(false);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwitchCompat.setEnabled(true);
                        ServerController.getInstance().startServer(MainActivity.this);

                    }
                }, 5000);


                if (isChecked) {

                    mWifiApController.turnOnHotspot();
                    mWifiScanner.stopScanning(MainActivity.this);

                    Toast.makeText(MainActivity.this, "Activating Hotspot...", Toast.LENGTH_SHORT).show();

                } else {

                    mWifiApController.turnOffHotspot();
                    mWifiScanner.startScanning(MainActivity.this);

                    Toast.makeText(MainActivity.this, "Disabling Hotspot...", Toast.LENGTH_SHORT).show();

                }
            }
        });

        //All views are initialized. Make this true.
        isVisible = true;

        //When the app is launched, try to sync messages with Firebase if there is internet connection
        FirebaseApp.initializeApp(this);
        FirebaseDatabaseHelper.pushMessagesToFirebase(this);
        FirebaseDatabaseHelper.fetchEveryMessage(this);

    }

    //Checks the hotspot flag. Returns it after creating a suitable Toast.
    public boolean isTheUserConnectedToHotspot() {

        if (isTheUserConnectedToHotspot()) return true;

        Toast.makeText(this, getResources().getString(R.string.toast_hotspot_warning), Toast.LENGTH_SHORT).show();
        return false;
    }

    //Notifies the ChatAdapter for new elements and scrolls RecyclerView to bottom
    public void notifyChatAdapter() {
        mChatAdapter.notifyDataSetChanged();
        CustomRecyclerView mChatRecyclerView = ButterKnife.findById(this, R.id.recyclerView_Chat);
        mChatRecyclerView.scrollToPosition(mMessages.size() - 1);
    }

    //Notifies the WifiAdaoter for new elements
    public void notifyWifiAdapter() {

    }

    //Creates a dialog box and fills it with detected hotspots. Allows users to select and connect.
    private void showScannedHotspotDialogBox() {

        //Scan again for refreshing
        mWifiScanner.scanNow();

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_action_scan_wifi);

        //Custom title
        TextView title = new TextView(this);
        title.setText(getResources().getString(R.string.detected_hotspots));
        title.setBackgroundColor(getResources().getColor(R.color.colorBlue2));
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builderSingle.setCustomTitle(title);

        //Add hotspots to adapter
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        for (ScanResult scanResult : mHotspotList) {
            arrayAdapter.add(scanResult.SSID);
        }


        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Connect to selected WiFi hotspot
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedSsid = arrayAdapter.getItem(which);

                mWifiScanner.connectToHotspot(selectedSsid);

                //Start updating client list if not doing it already
                mWifiApController.startUpdatingClientList();

                Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_connecting, selectedSsid), Toast.LENGTH_LONG).show();

            }
        });
        builderSingle.show();

    }

    @OnClick(R.id.imageButton_SendMessage)
    public void sendMessage() {

        //Get the text message from EditText
        EditTextV2 messageEditText = (EditTextV2) ButterKnife.findById(this, R.id.editText_Message);
        String message = messageEditText.getText().toString();

        if (message.length() < 1) {
            //Do not send empty message.
            Log.i(TAG, "sendMessage: empty");
            return;
        }

        //Clear message EditText.
        messageEditText.setText("");

        //Pass it to ChatHandler to prepare and send the message.
        mChatHandler.sendMessage(message);

    }

    //Shows the dialog box that is filled with detected hotspots which allow user to connect
    @OnClick(R.id.imageButton_ScanWifi)
    public void showDetectedHotspots() {
        if(mWifiApController.isHotspotActivated() && !mWifiScanner.isWifiEnabled()){
            Toast.makeText(this, getResources().getString(R.string.toast_cannot_scan), Toast.LENGTH_SHORT).show();
            return;
        } else if (!mWifiScanner.isWifiEnabled()){
            Toast.makeText(this, getResources().getString(R.string.toast_wifi_disabled), Toast.LENGTH_SHORT).show();
            return;
        }
        showScannedHotspotDialogBox();
    }

    //Opens a dialog box to edit the nickname.
    @OnClick(R.id.textView_Nickname)
    public void changeNickname() {

        final TextView nicknameTextView = ButterKnife.findById(this, R.id.textView_Nickname);

        //Edittext with 18 character limit.
        final EditText editText = new EditText(this);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});


        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.alert_title_nickname);
        alert.setView(editText);

        alert.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if (editText.getText().toString().length() == 0) {
                    nicknameTextView.setText(Constants.PHONE_NUMBER);
                } else nicknameTextView.setText(editText.getText().toString());

                //Save nickname to sharedpreferences
                sharedPreferences.edit().putString(Constants.CURRENT_USER_USERNAME, nicknameTextView.getText().toString()).apply();
                Constants.NICKNAME =  String.valueOf(nicknameTextView.getText());

            }
        });

        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();

        //Force show the keyboard. Since textview takes focus first and closes
        // the keyboard, we must try to show keyboard after a short delay.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);

    }

    @Override
    protected void onResume() {
        isVisible = true;
        notifyChatAdapter();

        //Set switch check depending on hotspot
        if(mWifiApController.isHotspotActivated()){
            mSwitchCompat.setChecked(true);
        }else mSwitchCompat.setChecked(false);

        //Check the connection
        mWifiApController.isConnectedToAmbeentMozillaHotspot();

        ServerController.getInstance().startServer(this);

        Log.i(TAG, "Lifecycle: onResume");
        super.onResume();
    }

    @Override
    protected void onStart() {
        isVisible = true;
        Log.i(TAG, "Lifecycle: onStart");
        super.onStart();

    }

    @Override
    protected void onPause() {
        isVisible = false;

        ServerController.getInstance().stopServer();
        mWifiApController.stopUpdatingClientList();

        Log.i(TAG, "Lifecycle: onPause ");
        super.onPause();
    }


    @Override
    protected void onStop() {
        isVisible = false;
        Log.i(TAG, "Lifecycle: onStop ");
        super.onStop();
    }


    public ChatHandler getmChatHandler() {
        return mChatHandler;
    }

    public WifiApController getmWifiApController(){
        return mWifiApController;
    }
}
