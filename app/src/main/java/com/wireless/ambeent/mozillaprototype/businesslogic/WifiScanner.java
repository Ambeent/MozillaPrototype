package com.wireless.ambeent.mozillaprototype.businesslogic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.wireless.ambeent.mozillaprototype.R;

import java.util.ArrayList;
import java.util.List;

public class WifiScanner extends BroadcastReceiver {

    private static final String TAG = "WifiScanner";

    private Context mContext;
    private List<ScanResult> mScanResults;

    private WifiManager mWifiManager;

    //Needed for more consistent scan periods
    private Handler mHandler;
    private Runnable mScanRunnable;

    public WifiScanner(Context mContext, List<ScanResult> mScanResults) {
        this.mContext = mContext;
        this.mScanResults = mScanResults;

        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mHandler = new Handler();
        mScanRunnable = new Runnable() {
            @Override
            public void run() {
                mWifiManager.startScan();
                mHandler.postDelayed(this, 9000);
            }
        };
    }

    //Registers the receiver and starts scanning nearby wifis
    public void startScanning(Activity activity){
        try {
            activity.unregisterReceiver(this);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        Log.i(TAG, "startScanning... ");
        activity.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mHandler.post(mScanRunnable);
    }

    //Scans again for quick refreshment
    public void scanNow(){
        mWifiManager.startScan();
    }

    //Stops the scan
    public void stopScanning(Activity activity){
        try {
            activity.unregisterReceiver(this);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        mHandler.removeCallbacks(mScanRunnable);
    }

    //Checks whether the wifi is enabled and returns boolean
    public boolean isWifiEnabled(){
        return mWifiManager.isWifiEnabled();
    }

    //Connect to clicked hotspot
    public void connectToHotspot(String ssid){

        //Already connected to a hotspot. Do nothing
        if(mWifiManager.getConnectionInfo().getSSID()!=null && mWifiManager.getConnectionInfo().getSSID().equals("\"" +ssid +"\"")) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_already_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        String password = "abcd1234";

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + ssid + "\"";   // Please note the quotes. String should contain ssid in quotes
        conf.preSharedKey = "\"" + password + "\"";;

        //remember id
        int netId = mWifiManager.addNetwork(conf);
        mWifiManager.disconnect();
        mWifiManager.enableNetwork(netId, true);
        mWifiManager.reconnect();

        Log.i(TAG, "connectToHotspot: ");
    }

    //This receiver filters the the access points according to their SSIDs and prepares a list of scanned app-created hotspots
    //If a SSID starts with "AmbeentMozilla", then it is created by our app.
    @Override
    public void onReceive(Context context, Intent intent) {

        List<ScanResult> scanResults = new ArrayList<>();

        for(ScanResult scanResult : mWifiManager.getScanResults()){

            if(scanResult.SSID.length() <14) continue; //Short ssid, do not bother checking

            String ssidPrefix = scanResult.SSID.substring(0,14 );

       //     Log.i(TAG, "onReceive: " + ssidPrefix);

            //Check the substring to see whether the ssid containts 'AmbeentMozilla'
            if(ssidPrefix.equalsIgnoreCase("AmbeentMozilla")) scanResults.add(scanResult);

        }

        //Add hotspots to list
        mScanResults.clear();
        mScanResults.addAll(scanResults);

      //  Log.i(TAG, "onReceive: " + mScanResults.toString());

    }


}
