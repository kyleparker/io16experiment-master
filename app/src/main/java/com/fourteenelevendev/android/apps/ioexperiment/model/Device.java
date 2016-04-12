package com.fourteenelevendev.android.apps.ioexperiment.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Device
 *
 * Created by kyleparker on 4/4/2016.
 */
public class Device implements Parcelable {
    private String deviceId;
    private int deviceNumber;
    private boolean connected;

    public Device() {
    }

    /**
     * Use this constructor to create new Device.
     *
     * @param deviceId
     * @param deviceNumber
     * @param connected
     */
    public Device(String deviceId, int deviceNumber, boolean connected) {
        this.deviceId = deviceId;
        this.deviceNumber = deviceNumber;
        this.connected = connected;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getDeviceNumber() {
        return deviceNumber;
    }

    public boolean isConnected() {
        return connected;
    }

    private Device(Parcel in) {
        deviceId = in.readString();
        deviceNumber = in.readInt();
        connected = in.readByte() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceId);
        dest.writeInt(deviceNumber);
        dest.writeByte((byte) (connected ? 1 : 0));
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
}
