package com.wireless.ambeent.mozillaprototype.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.res.Resources;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.util.TypedValue;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Ambeent Wireless.
 */

public class ActivityHelpers {

    private static final String TAG = "ActivityHelpers";

    //This workaround method to get mac address of the device is needed for Android 6.0 and later versions.
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    //Reads the ip address from website and returns it.
    public static String getExternalIp(){

        try {
            URL checkIP = new URL("http://checkip.amazonaws.com/");
            BufferedReader in = new BufferedReader(new InputStreamReader(checkIP.openStream()));

            return in.readLine()+":8081";

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0.0.0.0";
    }


    //Formula for converting frequency to channel.
    public static int convertFrequencyToChannel(int freq) {
        if (freq >= 2412 && freq <= 2484) {
            return (freq - 2412) / 5 + 1;
        } else if (freq >= 5170 && freq <= 5825) {
            return (freq - 5170) / 5 + 34;
        } else {
            return -1;
        }
    }


    //Finds gateway address (192.168.x.x)
    public static String getGatewayAddress(WifiManager wifiManager){

        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo(); //Needed to receive gateway address.
        int binaryAddress = dhcpInfo.gateway; //Getting the address in binary format.
        return formatIpAddress(binaryAddress);

    }

    //Gets the binary format of gateway address and converts it to legit address.
    public static String formatIpAddress(int addr){
        return  ((addr & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF));
    }

    //Converts dp to px units.
    public static int convertDpToPx(Context context, float dp){
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }





    //Unregisters the receiver with error handling
    public static void unregisterReceiver(Context context, BroadcastReceiver receiver){

        try {
            context.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }

    }

    public static void disableScrollGesture(GoogleMap googleMap){
        if(googleMap != null) googleMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    //Just shortening the method...
    public static long getCurrentTimeMilis(){

        long currentTime = System.currentTimeMillis();

        return currentTime;
    }

    //Just shortening the method...
    public static long getCurrentTimeSeconds(){

        long currentTime = System.currentTimeMillis()/1000;

        return currentTime;
    }

    //Just shortening the method...
    public static long getTenMinutesAgoMilis(){

        long tenMinutesAgo = System.currentTimeMillis() - (1000 * 60 * 10);

        return tenMinutesAgo;
    }

    //Just shortening the method...
    public static long getTenMinutesAgoSeconds(){

        long tenMinutesAgo = (System.currentTimeMillis()/1000) -( 60 * 10);

        return tenMinutesAgo;
    }

    //Animates the map camera to given target location
    public static void animateCamera(GoogleMap map, LatLng target){

        CameraPosition camPos = new CameraPosition.Builder()
                .target(target)
                .zoom(map.getMaxZoomLevel())
                .build();
        CameraUpdate camUpd = CameraUpdateFactory.newCameraPosition(camPos);
        map.animateCamera(camUpd);

    }
}
