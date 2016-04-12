package com.fourteenelevendev.android.apps.ioexperiment.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;

import com.fourteenelevendev.android.apps.ioexperiment.utils.Constants;

/**
 * User object
 * <p/>
 * Users for The Giving Challenge, including the person logged into the app and anyone that is part of their
 * affiliated communities.
 * <p/>
 * Created by kyleparker on 1/28/2016.
 */
public class User implements Parcelable {
    private String authUid;
    private int numberDevices;
    private HashMap<String, Object> timestampCreated;

    public User() {
    }

    /**
     * Use this constructor to create new User.
     *
     * @param authUid
     * @param timestampCreated
     */
    public User(String authUid, HashMap<String, Object> timestampCreated) {
        this.authUid = authUid;
        this.timestampCreated = timestampCreated;
    }

    public String getAuthUid() {
        return authUid;
    }

    public int getNumberDevices() {
        return numberDevices;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }

    @JsonIgnore
    public long getTimestampCreatedLong() {
        if (timestampCreated != null && timestampCreated.size() > 0) {
            return (long) timestampCreated.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
        }

        return -1L;
    }

    private User(Parcel in) {
        authUid = in.readString();
        numberDevices = in.readInt();

        long inCreated = in.readLong();
        HashMap<String, Object> timestampCreatedObject = new HashMap<>();
        timestampCreatedObject.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, inCreated);
        this.timestampCreated = timestampCreatedObject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authUid);
        dest.writeInt(numberDevices);
        dest.writeLong(getTimestampCreatedLong());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
