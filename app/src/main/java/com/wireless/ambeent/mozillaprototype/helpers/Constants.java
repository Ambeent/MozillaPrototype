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


}
