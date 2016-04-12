package com.fourteenelevendev.android.apps.ioexperiment.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

import com.fourteenelevendev.android.apps.ioexperiment.BaseApplication;
import com.fourteenelevendev.android.apps.ioexperiment.R;
import com.fourteenelevendev.android.apps.ioexperiment.model.User;
import com.fourteenelevendev.android.apps.ioexperiment.utils.Constants;
import com.fourteenelevendev.android.apps.ioexperiment.utils.LogUtils;
import com.fourteenelevendev.android.apps.ioexperiment.utils.PreferencesUtils;
import com.google.android.gms.analytics.Tracker;

// TODO: If time permits, add a special display to the watch at the end of the sequence
// TODO: Consider adding animations
// DONE: Add the images to an adapter and display as a horizontal recyclerview
// DONE: Add a start-over feature
// DONE: Add fragment to backstack for back button to work properly
// DONE: If this is device 2-x, retrieve the UID from Firebase, then display the ConnectFragment

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity implements
        WelcomeFragment.Callbacks,
        ConnectFragment.Callbacks {
    private static final int SCREEN_WELCOME = 0;
    private static final int SCREEN_CONNECT = 1;
    private static final int SCREEN_SHARE_DISCOVER = 2;

    private static final String EXTRA_SCREEN = "extras.SCREEN";

    private static final int UI_ANIMATION_DELAY = 300;

    private static AppCompatActivity mActivity;
    private static final Handler mHideHandler = new Handler();

    private static View mContentView;

    private static boolean mVisible;
    private int mScreenPosition;

    private MessageFragment mMessageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mActivity = this;
        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);

        LogUtils.LOGE("***> firebase uid", PreferencesUtils.getString(mActivity, R.string.key_firebase_uid, ""));
        LogUtils.LOGE("***> is connected", PreferencesUtils.getBoolean(mActivity, R.string.key_is_connected, false) + "");
        if (TextUtils.isEmpty(PreferencesUtils.getString(mActivity, R.string.key_firebase_uid, "")) &&
                !PreferencesUtils.getBoolean(mActivity, R.string.key_is_connected, false)) {
            generateAnonymousAccount();
        }

        if (savedInstanceState != null) {
            mScreenPosition = savedInstanceState.getInt(EXTRA_SCREEN, SCREEN_WELCOME);
        } else {
            BaseApplication application = (BaseApplication) getApplication();
            Tracker tracker = application.getDefaultTracker();
            tracker.enableAutoActivityTracking(true);

            if (PreferencesUtils.getBoolean(mActivity, R.string.key_is_connected, false)) {
                mScreenPosition = SCREEN_SHARE_DISCOVER;
                loadFragment(Constants.Source.MAIN);
            } else {
                mScreenPosition = SCREEN_WELCOME;
                loadFragment(Constants.Source.MAIN);
            }
        }

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SCREEN, mScreenPosition);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mMessageFragment != null) {
            hide();
            mMessageFragment.finishedResolvingNearbyPermissionError();
            if (requestCode == Constants.REQUEST_RESOLVE_ERROR) {
                // User was presented with the Nearby opt-in dialog and pressed "Allow".
                if (resultCode == Activity.RESULT_OK) {
                    // We track the pending subscription and publication tasks in MainFragment. Once
                    // a user gives consent to use Nearby, we execute those tasks.
                    mMessageFragment.executePendingTasks();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // User was presented with the Nearby opt-in dialog and pressed "Deny". We cannot
                    // proceed with any pending subscription and publication tasks. Reset state.
                    mMessageFragment.resetToDefaultState();
                } else {
                    Toast.makeText(mActivity, "Failed to resolve error with code " + resultCode,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed(){
        LogUtils.LOGE("***> screen position", mScreenPosition + "");
//        if (getSupportFragmentManager().getBackStackEntryCount() == 1){
        if (mScreenPosition == SCREEN_WELCOME){
//            super.onBackPressed();
            finish();
        } else {
//            super.onBackPressed();
            LogUtils.LOGE("***> backpressed", "reset device");
            resetDevice();
        }
    }

    @Override
    public void onDiscover(String source) {
        mScreenPosition = SCREEN_SHARE_DISCOVER;
        loadFragment(source);
    }

    @Override
    public void onGetStarted() {
        mScreenPosition = SCREEN_CONNECT;
        loadFragment("");
    }

    @Override
    public void onLetsGo() {
        mScreenPosition = SCREEN_SHARE_DISCOVER;
        loadFragment(Constants.Source.CONNECT);
    }

    /**
     * Converts a fragment arguments bundle into an intent.
     */
    public static Intent fragmentArgumentsToIntent(Bundle arguments) {
        Intent intent = new Intent();
        if (arguments == null) {
            return intent;
        }

        intent.putExtras(arguments);
        return intent;
    }

    public static void hide() {
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Creates a new user in Firebase from the Java POJO
     */
    private void createUserInFirebaseHelper(final String authUid) {
        final Firebase userLocation = new Firebase(Constants.FIREBASE_URL_USERS).child(authUid);

        // See if there is already a user (for example, if they already logged in with an associated Google account.
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If there is no user, make one
                LogUtils.LOGE("***> userLocation", "single value event - " + dataSnapshot.getValue());
                if (dataSnapshot.getValue() == null) {
                    // Set raw version of date to the ServerValue.TIMESTAMP value and save into dateCreatedMap
                    HashMap<String, Object> timestampJoined = new HashMap<>();
                    timestampJoined.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(authUid, timestampJoined);
                    userLocation.setValue(newUser);

                    LogUtils.LOGE("***> add new user", authUid);
                    PreferencesUtils.setInt(mActivity, R.string.key_device_number, 1);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                LogUtils.LOGE(MainActivity.class.getSimpleName(), getString(R.string.log_error_occurred) + firebaseError.getMessage());
            }
        });
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Generate an anonymous account to identify this user. The UID will be transmitted as part of the payload for all
     * connected devices.
     */
    private void generateAnonymousAccount() {
        LogUtils.LOGE("***> generate anon account", "here");
        Firebase ref = new Firebase(Constants.FIREBASE_URL);
        ref.authAnonymously(new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // we've authenticated this session with your Firebase app
                LogUtils.LOGE("***> onAuthenticated", authData.getUid());
                PreferencesUtils.setString(mActivity, R.string.key_firebase_uid, authData.getUid());
                createUserInFirebaseHelper(authData.getUid());
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // there was an error
            }
        });
    }

    /**
     * Based on the position of the intro, display the appropriate fragment
     */
    private void loadFragment(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Extra.SOURCE, source);

        switch (mScreenPosition) {
            case SCREEN_WELCOME:
                WelcomeFragment welcomeFragment = new WelcomeFragment();
                welcomeFragment.setArguments(bundle);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_content, welcomeFragment)
                        .addToBackStack(WelcomeFragment.class.getName())
                        .commit();
                break;
            case SCREEN_CONNECT:
                ConnectFragment connectFragment = new ConnectFragment();
                connectFragment.setArguments(bundle);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_content, connectFragment)
                        .addToBackStack(ConnectFragment.class.getName())
                        .commit();
                break;
            case SCREEN_SHARE_DISCOVER:
                mMessageFragment = new MessageFragment();
                mMessageFragment.setArguments(bundle);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_content, mMessageFragment)
                        .addToBackStack(MessageFragment.class.getName())
                        .commit();
                break;
        }
    }

    public static void resetDevice() {
        // Delete entries from Firebase
        String firebaseUid = PreferencesUtils.getString(mActivity, R.string.key_firebase_uid, "");

        LogUtils.LOGE("***> reset device", firebaseUid);
        Firebase deviceRef = new Firebase(Constants.FIREBASE_URL_DEVICES).child(firebaseUid);
        deviceRef.removeValue();

        Firebase userRef = new Firebase(Constants.FIREBASE_URL_USERS).child(firebaseUid);
        userRef.removeValue();

        PreferencesUtils.setString(mActivity, R.string.key_firebase_uid, "");
        PreferencesUtils.setInt(mActivity, R.string.key_device_number, 0);
        PreferencesUtils.setBoolean(mActivity, R.string.key_is_connected, false);
        PreferencesUtils.setInt(mActivity, R.string.key_total_devices, 0);
        PreferencesUtils.setBoolean(mActivity, R.string.key_message_received, false);

        hide();

        PreferencesUtils.setBoolean(mActivity, R.string.key_device_reset_done, true);

        Intent intent = new Intent(mActivity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);
        mActivity.finish();
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private static final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
}
