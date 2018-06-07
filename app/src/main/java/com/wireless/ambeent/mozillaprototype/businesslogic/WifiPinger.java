package com.wireless.ambeent.mozillaprototype.businesslogic;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;


import com.wireless.ambeent.mozillaprototype.helpers.ActivityHelpers;
import com.wireless.ambeent.mozillaprototype.pojos.ConnectedDeviceObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Atakan on 6.12.2017.
 */

public class WifiPinger {

    private static final String TAG = "WifiPinger";
    private static final int NB_THREADS = 256;

    private Context mContext;


    private String usersIpAddress = "";
    private StringBuilder stringBuilder;

    private HashSet<ConnectedDeviceObject> mConnDevObjList;

    public WifiPinger(Context mContext, HashSet<ConnectedDeviceObject> mConnDevObjList){
        this.mContext = mContext;
        this.mConnDevObjList = mConnDevObjList;



    }

    public void startScanning(){

        stringBuilder = new StringBuilder();

        //Sets users local ip address
        setUsersConnDevice();

        new ScanOperation().execute();

    }

    private class ScanOperation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
       //     Log.i(TAG, "onPreExecute: ScanOperation ");
        //    mConnDevObjList.clear();


        }

        @Override
        protected Void doInBackground(Void... params) {

      //      Log.i(TAG, "Starting scan: ");

            ExecutorService executor = Executors.newFixedThreadPool(NB_THREADS);
            String subnet = usersIpAddress.substring(0, usersIpAddress.lastIndexOf("."));


            for (int dest = 0; dest < 255; dest++) {
                String host = subnet + "." + dest;
                executor.execute(pingRunnable(host));
            }

       //     Log.i(TAG, "Waiting for executor to terminate...");
            executor.shutdown();
            try {
                executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG, "doInBackground: " + e.toString() );
            }

            Log.i(TAG, "Scan finished");
            Log.i(TAG, "Scan results: " + stringBuilder.toString());

            for (ConnectedDeviceObject connectedDeviceObject : mConnDevObjList){

                Log.i(TAG, "ipAddress:  " +connectedDeviceObject.getIpAddress());

            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {

       //     Log.i(TAG, "onPostExecute: ScanOperation");
        }
    }


    private void setConnDeviceTextview(){

        int numberOfDevices = mConnDevObjList.size();
      //  mConnDeviceTextView.setText(mContext.getResources().getString(R.string.router_connected_device_number, numberOfDevices));

    }





    //Returns a runnable to be executed by executor. This runnable sends a ping to given IP address. If an answer is received,
    //it reads arp document to gets the mac address of the device at that IP.
    private Runnable pingRunnable(final String host) {
        return new Runnable() {
            public void run() {
       //         Log.d(TAG, "Pinging " + host + "...");

      //          ArrayList<ConnectedDeviceObject> connectedDeviceObjects = new ArrayList<>();

                try {
                    InetAddress inet = InetAddress.getByName(host);
                    boolean reachable = inet.isReachable(2000);
                    if (reachable) {
            //            Log.d(TAG, "=> Result: reachable " + inet.getHostName() + " " + inet.getCanonicalHostName());

                        BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            String[] splitted = line.split(" +");
                            if (splitted != null && splitted.length >= 4) {
                                // Basic sanity check
                                String neighborMac = splitted[3];
                                if (neighborMac.matches("..:..:..:..:..:..") && !neighborMac.equals("00:00:00:00:00:00" ) && !stringBuilder.toString().contains(splitted[0])) {
                                    //We have found a device at this address. Add it to our list.

                                    //Is this the users own ip address?
                                    if(splitted[0].equalsIgnoreCase(usersIpAddress))
                                        continue;


                                    ConnectedDeviceObject connectedDeviceObject = new ConnectedDeviceObject(neighborMac.toUpperCase(), splitted[0]);

                                    Log.i(TAG, "run: Found " + connectedDeviceObject.toString());

                                    mConnDevObjList.add(connectedDeviceObject);

                                    stringBuilder.append(" IP " +splitted[0] + " MAC: " + neighborMac +"\n");
                                }
                            }
                        }
                        bufferedReader.close();

                    }



             //       Log.d(TAG, "=> Result: not reachable ");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Not found", e);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "IO Error", e);
                }
            }
        };
    }


    //Pinging self does not return arp which results not counting the users own device. So we add the users device to list exclusively.
    public void setUsersConnDevice() {
        WifiManager wm = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
        usersIpAddress = ActivityHelpers.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        String usersMacAddress = ActivityHelpers.getMacAddr();

    //    mConnDevObjList.add(new ConnectedDeviceObject(usersMacAddress, usersIpAddress, true));

    //    Log.i(TAG, "setUsersConnDevice: " + usersIpAddress);
    //    ipAdressTextView.setText(usersIpAddress);
    }


}
