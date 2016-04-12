package com.fourteenelevendev.android.apps.ioexperiment;

import android.app.Application;

import com.firebase.client.Firebase;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Base application
 *
 * Created by kyleparker on 3/30/2016.
 */
public class BaseApplication extends Application {
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();

        /* Initialize Firebase */
        Firebase.setAndroidContext(this);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setLocalDispatchPeriod(60);
            analytics.setDryRun(BuildConfig.DEBUG);

            mTracker = analytics.newTracker(R.xml.app_tracker);
            mTracker.enableAdvertisingIdCollection(true);
            mTracker.enableAutoActivityTracking(true);
            mTracker.enableExceptionReporting(true);
        }

        return mTracker;
    }
}
