package com.fourteenelevendev.android.apps.ioexperiment.utils;

/**
 * Constants
 *
 * Created by kyleparker on 3/30/2016.
 */
public class Constants {
    private static final String PACKAGE = "com.fourteenelvendev.android.app";
    public static final String APP_FONT = "fonts/sans.ttf";

    private static final String FIREBASE_LOCATION_DEVICES = "devices";
    private static final String FIREBASE_LOCATION_USERS = "users";

    public static final String FIREBASE_PROPERTY_CONNECTED = "connected";
    public static final String FIREBASE_PROPERTY_DEVICE_NUMBER = "deviceNumber";
    public static final String FIREBASE_PROPERTY_NUMBER_DEVICES = "numberDevices";
    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";

    public static final String FIREBASE_URL = "REPLACE WITH FIREBASE URL";
    public static final String FIREBASE_URL_DEVICES = FIREBASE_URL + "/" + FIREBASE_LOCATION_DEVICES;
    public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USERS;

    // Keys to get and set the current subscription and publication tasks using SharedPreferences.
    public static final String KEY_SUBSCRIPTION_TASK = "subscription_task";
    public static final String KEY_PUBLICATION_TASK = "publication_task";

    public static final int REQUEST_RESOLVE_ERROR = 1001;

    public static final String SETTINGS_NAME = "AppSettings";

    // Tasks constants.
    public static final String TASK_SUBSCRIBE = "task_subscribe";
    public static final String TASK_UNSUBSCRIBE = "task_unsubscribe";
    public static final String TASK_PUBLISH = "task_publish";
    public static final String TASK_UNPUBLISH = "task_unpublish";
    public static final String TASK_NONE = "task_none";

    // The time-to-live when subscribing or publishing in this sample. Three minutes.
    public static final int TTL_IN_SECONDS = 3 * 60;

    public class Extra {
        private static final String PREFIX = ".extra.";

        public static final String SOURCE = PACKAGE + PREFIX + "SOURCE";
    }

    public class Source {
        private static final String PREFIX = ".source.";

        public static final String CONNECT = PACKAGE + PREFIX + "CONNECT";
        public static final String MAIN = PACKAGE + PREFIX + "MAIN";
        public static final String WELCOME = PACKAGE + PREFIX + "WELCOME";
    }
}
