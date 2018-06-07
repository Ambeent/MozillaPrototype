package com.wireless.ambeent.mozillaprototype.businesslogic;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.wireless.ambeent.mozillaprototype.hotspot.ClientScanResult;
import com.wireless.ambeent.mozillaprototype.hotspot.FinishScanListener;
import com.wireless.ambeent.mozillaprototype.hotspot.WifiApManager;
import com.wireless.ambeent.mozillaprototype.pojos.ConnectedDeviceObject;
import com.wireless.ambeent.mozillaprototype.server.ServerController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

//This class is responsible for creating and disabling hotspots, scanning and detecting the hotspots that are created by the application.
public class WifiApController {

    private static final String TAG = "WifiApController";


    private Context mContext;
    private HashSet<ConnectedDeviceObject> mConnDevObjList;

    //Hotspot controller object of external library. Its contents are in 'hotspot' package
    private WifiApManager mWifiApManager;

    //Hotspot object for Android O and above
    private WifiManager.LocalOnlyHotspotReservation mReservation;

    //WifiManager for general purposes
    private WifiManager mWifiManager;

    //A class that handles pinging of the network and finding other connected devices
    private WifiPinger mWifiPinger;

    //When connected to a network, these will be used to check other clients that are connected to network periodically
    private Handler mClientDetectorHandler;
    private Runnable mClientDetectorRunnable;

    //Contains the list of the hotspots that are created by the app
    private List<String> mScannedHotspotSsidList = new ArrayList<>();

    //The flag to keep track of client updates
    private boolean isClientsGetingUpdated = false;


    public WifiApController(Context mContext, HashSet<ConnectedDeviceObject> mConnDevObjList) {
        this.mContext = mContext;
        this.mConnDevObjList = mConnDevObjList;

        initialization();


    }

    //Initialization of the object that are used in this class
    private void initialization(){

        mWifiApManager = new WifiApManager(mContext);

        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mWifiPinger = new WifiPinger(mContext, mConnDevObjList);

        //Update client list every 10 seconds
        mClientDetectorHandler = new Handler();
        mClientDetectorRunnable = new Runnable() {
            @Override
            public void run() {
                if(mWifiApManager.isWifiApEnabled() || isConnectedToAmbeentMozillaHotspot()){
                    Toast.makeText(mContext, "Detected " + mConnDevObjList.size() + " other connected devices", Toast.LENGTH_SHORT).show();
                    updateClientList();
                    //mWifiPinger.startScanning();
                } else mConnDevObjList.clear();
                mClientDetectorHandler.postDelayed(this, 10000);
            }
        };

        startUpdatingClientList();

        checkWriteSettingsPermission();

    }

    //Starts periodical updating of the client list
    public void startUpdatingClientList(){
        if(!isClientsGetingUpdated){
            mClientDetectorHandler.postDelayed(mClientDetectorRunnable, 1000);
            isClientsGetingUpdated = true;
        }
    }

    //Stops periodical updating of the client list
    public void stopUpdatingClientList(){
        if (isClientsGetingUpdated) {
            mClientDetectorHandler.removeCallbacks(mClientDetectorRunnable);
            isClientsGetingUpdated = false;
        }
    }

    //Decides which methods to call to enable hotspot
    public void turnOnHotspot() {
        Log.i(TAG, "turnOnHotspot: ");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            turnOnHotspotAboveOreo();
        } else {
            turnOnHotspotBelowOreo();
        }

        startUpdatingClientList();
    }

    //Decides which methods to call to disable hotspot
    public void turnOffHotspot() {
        Log.i(TAG, "turnOffHotspot: ");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            turnOffHotspotAboveOreo();
        } else {
            turnOffHotspotBelowOreo();
        }

        stopUpdatingClientList();
    }

    //Checks the connected wifi network and determines whether it is an app-created network
    public boolean isConnectedToAmbeentMozillaHotspot(){

        //TODO: Assume that the app has activated the hotspot, therefore it is our hotspot but this is not true. For some reason, ssid cannot be obtained with WifiInfo if hotspot is active, so cheat by returning true here.
        if(isHotspotActivated()) {
            startUpdatingClientList();
            return true;
        }

        //Wifi is not even activated or the ssid is shorter than the prefix
        if(mWifiManager.getConnectionInfo().getSSID() == null || mWifiManager.getConnectionInfo().getSSID().length() < 15) return false;

    //    Log.i(TAG, "isConnectedToAmbeentMozillaHotspot: " +wifiManager.getConnectionInfo().getSSID());

        String ssidPrefix = mWifiManager.getConnectionInfo().getSSID().substring(1,15 ); //Mind the quote marks...


        if(ssidPrefix.equalsIgnoreCase("AmbeentMozilla")) {
     //       Log.i(TAG, "isConnectedToAmbeentMozillaHotspot: true");
            startUpdatingClientList();
            return true;
        } else {
            stopUpdatingClientList();
     //       Log.i(TAG, "isConnectedToAmbeentMozillaHotspot: false");
            return  false;
        }

    }


    //Checks the WRITE_SETTINGS permission, sends user to permission page is needed
    private void checkWriteSettingsPermission() {
        mWifiApManager.showWritePermissionSettings(false);
    }




    //Turning on hotspot for the versions below O
    private void turnOnHotspotBelowOreo() {
        WifiConfiguration wifiConfiguration = initHotspotConfig();
        mWifiApManager.setWifiApEnabled(wifiConfiguration, true);
    }

    //Turning off hotspot for  the versions below O
    private void turnOffHotspotBelowOreo() {
        //Stop acting like a server
        ServerController.getInstance().stopServer();

        mWifiManager.setWifiEnabled(true);
    }

    //Returns the state of hotspot
    public boolean isHotspotActivated(){
        return mWifiApManager.isWifiApEnabled();
    }

    //Turning on hotspot for Android O and above
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspotAboveOreo() {
        Log.i(TAG, "turnOnHotspotAboveOreo: ");

        WifiManager manager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);



        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d(TAG, "Wifi Hotspot is on now");
                mReservation = reservation;
            //    mReservation.getWifiConfiguration() = initHotspotConfig();

            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d(TAG, "onStopped: ");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.d(TAG, "onFailed: ");
            }
        }, new Handler());
    }

    private void turnOffHotspotAboveOreo() {
        Log.i(TAG, "turnOffHotspotAboveOreo: ");
        if (mReservation != null) {
            mReservation.close();
        }
    }

    private void pingTheNetwork(){

    }

    //Pings the network and sets the Connected Device list accordingly
    private void updateClientList() {

        mWifiApManager.getClientList(false, new FinishScanListener() {


            @Override
            public void onFinishScan(final ArrayList<ClientScanResult> clients) {

                //Dummy list to keep main list away from work as much as possible
                ArrayList<ConnectedDeviceObject> connectedDeviceObjects = new ArrayList<>();

                StringBuilder a = new StringBuilder();
                a.append("WifiApState: " + mWifiApManager.getWifiApState() + "\n");
        //        a.append("Clients: \n");
                for (ClientScanResult clientScanResult : clients) {
         //           a.append("####################\n");
                    a.append("IpAddr: " + clientScanResult.getIpAddr() + "  Mac: " + clientScanResult.getHWAddr()+"\n");
           //         a.append("Device: " + clientScanResult.getDevice() + "\n");
          //          a.append("HWAddr: " + clientScanResult.getHWAddr() + "\n");
          //          a.append("isReachable: " + clientScanResult.isReachable() + "\n");

                    ConnectedDeviceObject connectedDeviceObject = new ConnectedDeviceObject(clientScanResult.getHWAddr(), clientScanResult.getIpAddr());
                    connectedDeviceObjects.add(connectedDeviceObject);
                }

                //Add detected devices to main list.

        //        mConnDevObjList.clear();
                mConnDevObjList.addAll(connectedDeviceObjects);
                Log.i(TAG, "onFinishScan: " + a);
            }
        });

    }

    //Creates a WifiConfiguration for hotspot initialization and returns it
    private WifiConfiguration initHotspotConfig() {

        WifiConfiguration wifiConfig = new WifiConfiguration();

        //Generating a random suffix to differentiate hotspots
        Random random = new Random();
        int randomSuffix = random.nextInt(9000) +1000;

        wifiConfig.SSID = "AmbeentMozilla-" + String.valueOf(randomSuffix);

        // must be 8 length
        wifiConfig.preSharedKey = "abcd1234";

        //wifiConfig.hiddenSSID = true;

        wifiConfig.status = WifiConfiguration.Status.ENABLED;
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);


        return wifiConfig;
    }

    public List<String> getmScannedHotspotSsidList() {
        return mScannedHotspotSsidList;
    }
}
