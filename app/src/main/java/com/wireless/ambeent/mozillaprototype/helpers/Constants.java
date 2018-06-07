package com.wireless.ambeent.mozillaprototype.helpers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

/**
 * Created by Atakan on 12.05.2017.
 */

public class Constants {

    public static final String SHARED_PREF = "AMBEENT_MINI";

    //Used for user information storage.
    public static final String USER_PHONE_NUMBER = "User_Phone_Number";

    //The timestamp of the last message sync with Firebase
    public static final String LAST_SYNC_TIMESTAMP = "Last_Sync_Timestamp";


    //Used for global access in app. Initialized in MainActivity
    public static String PHONE_NUMBER;

    //Intent extra names
    public static final String INTENT_EXTRA_CHANNEL = "Channel";
    public static final String INTENT_EXTRA_SSID = "Ssid";
    public static final String INTENT_EXTRA_MAC_ADDRESS = "MacAddress";
    public static final String INTENT_EXTRA_RSSI = "Rssi";
    public static final String INTENT_EXTRA_IS_CONNECTED = "IsUserConnectedToThatRouter";

    //Contains Id, Mac and name of registered devices.
    public static final String DEVICE_LIST = "Device_List";

    //Used for comparison while registering notification hubs.
    public static final String PREVIOUS_USER_TAG = "Previous_User_Tag";
    public static final String REGISTRATION_ID = "registrationID";
    public static final String FCM_TOKEN = "FCMtoken";

    //Used to save and get profile picture from internal storage
    public static final String PROFILE_PICTURE_URL = "Profile_Picture_Url";
    public static final String PROFILE_PICTURE_PATH = "profilePictureDir";
    public static final String PROFILE_PICTURE_NAME = "profile.jpg";

    //Used for calculating the remaining time of AlarmManager to prevent it from rescheduling at every MainActivity launch.
    public static final String ALARM_LAST_UPDATE = "Alarm_Last_Update";
    public static final String ALARM_REMANINING_TIME = "Alarm_Remaining_Time";

    //Router brand list
    public static final List<String> BRAND_LIST = ImmutableList.of("AirTies"
            , "Asus"
            , "Linksys"
            , "TP-Link"
            , "Zyxel");


    //AirTies model list
    public static final List<String> AIRTIES_MODEL_LIST = ImmutableList.of("Air5750");
    //Asus model list
    public static final List<String> ASUS_MODEL_LIST = ImmutableList.of("Dummy");
    //Huawei model list
    public static final List<String> HUAWEI_MODEL_LIST = ImmutableList.of("HG255s");
    //Linksys model list
    public static final List<String> LINKSYS_MODEL_LIST = ImmutableList.of("WRT54GCv3");
    //TP-Link model list
    public static final List<String> TP_LINK_MODEL_LIST = ImmutableList.of("Archer MR200");
    //Zyxel model list
    public static final List<String> ZYXEL_MODEL_LIST = ImmutableList.of("Dummy");


    //Router brand and model mapping. Just change these list contents and mapping for the adapter.
    public static final  Map<String, List<String>> BRAND_MODEL_MAP = ImmutableMap.<String, List<String>>builder()
            .put("AirTies" , AIRTIES_MODEL_LIST)
            .put("Asus", ASUS_MODEL_LIST)
            .put("Huawei", HUAWEI_MODEL_LIST)
            .put("Linksys",LINKSYS_MODEL_LIST)
            .put("TP-Link" , TP_LINK_MODEL_LIST)
            .put("Zyxel" , ZYXEL_MODEL_LIST)
            .build();

    //Message time span in miliseconds (1000 ms = 1 sec). 10*60*1000
    public static final long MESSAGE_LIFE_SPAN = 600000;
    public static final long MESSAGE_TICK_PERIOD = 10000; //Period is one minute.
}
