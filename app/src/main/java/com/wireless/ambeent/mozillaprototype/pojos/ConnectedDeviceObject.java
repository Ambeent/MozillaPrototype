package com.wireless.ambeent.mozillaprototype.pojos;

import java.util.Objects;

/**
 * Created by Atakan on 7.12.2017.
 */

public class ConnectedDeviceObject {

    private String macAddress;

    private String ipAddress;

    private boolean isThisTheUser; //Is that device the users device?

    //This timestamp marks the time when the data sync is made last time
    private long lastUpdateTimestamp;

    public ConnectedDeviceObject(String macAddress, String ipAddress, boolean isThisTheUser) {
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.isThisTheUser = isThisTheUser;
    }

    public ConnectedDeviceObject(String macAddress, String ipAddress) {
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isThisTheUser() {
        return isThisTheUser;
    }

    public void setThisTheUser(boolean thisTheUser) {
        isThisTheUser = thisTheUser;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectedDeviceObject that = (ConnectedDeviceObject) o;
        return Objects.equals(macAddress.toLowerCase(), that.macAddress.toLowerCase());
    }

    @Override
    public int hashCode() {

        return Objects.hash(macAddress);
    }

    @Override
    public String toString() {
        return "ConnectedDeviceObject{" +
                "macAddress='" + macAddress + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
