package com.fourteenelevendev.android.apps.ioexperiment.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fourteenelevendev.android.apps.ioexperiment.R;
import com.fourteenelevendev.android.apps.ioexperiment.model.Device;
import com.fourteenelevendev.android.apps.ioexperiment.model.DeviceMessage;
import com.fourteenelevendev.android.apps.ioexperiment.model.Letter;
import com.fourteenelevendev.android.apps.ioexperiment.utils.Adapters;
import com.fourteenelevendev.android.apps.ioexperiment.utils.Constants;
import com.fourteenelevendev.android.apps.ioexperiment.utils.DialogUtils;
import com.fourteenelevendev.android.apps.ioexperiment.utils.LogUtils;
import com.fourteenelevendev.android.apps.ioexperiment.utils.PreferencesUtils;
import com.fourteenelevendev.android.apps.ioexperiment.utils.UIUtils;

// TODO: Toggle the connect icon to a cancel icon while trying to subscribe

/**
 * Display the message to the user
 *
 * Created by kyleparker on 3/31/2016.
 */
public class MessageFragment extends Fragment {
    private static final String TAG = "MessageFragment";

    private FragmentActivity mActivity;
    private ViewGroup mRootView;
    private MaterialDialog mProgressDialog;
    private TextView mIntroConnectDevice;
    private View mContainer;
    private ImageButton mDiscover;

    private GoogleApiClient mGoogleApiClient;
    private Adapters.LetterAdapter mAdapter;

    private Typeface mTypeface;
    private int mMessageResId;
    private int mDeviceNum;
    private int mTotalDevices;
    private String mSource = "";
    private String mFirebaseUid;
    private String[] mDeviceIds;
    private boolean mStartNextPublish;
    /**
     * Sets the time in seconds for a published message or a subscription to live. Set to three
     * minutes.
     */
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(Constants.TTL_IN_SECONDS).build();
    /**
     * The {@link Message} object used to broadcast information about the device to nearby devices.
     */
    private Message mDeviceInfoMessage;
    /**
     * A {@link MessageListener} for processing messages from nearby devices.
     */
    private MessageListener mMessageListener;
    /**
     * Tracks if we are currently resolving an error related to Nearby permissions. Used to avoid
     * duplicate Nearby permission dialogs if the user initiates both subscription and publication
     * actions without having opted into Nearby.
     */
    private boolean mResolvingNearbyPermissionError = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this.getActivity();
        mDeviceNum = PreferencesUtils.getInt(mActivity, R.string.key_device_number, 1);
        mTotalDevices = PreferencesUtils.getInt(mActivity, R.string.key_total_devices, 1);
        mFirebaseUid = PreferencesUtils.getString(mActivity, R.string.key_firebase_uid, "");
        mTypeface = Typeface.createFromAsset(mActivity.getAssets(), Constants.APP_FONT);

        mDeviceIds = new String[mTotalDevices];
        mStartNextPublish = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_message, container, false);
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LogUtils.LOGE("***> onCreateView", (savedInstanceState != null) + "");
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleIncomingMessage(message);
                    }
                });
            }

            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.LOGE("***> listener", message.toString());
                    }
                });
            }
        };

        reloadFromArguments(getArguments());
        setupView();

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mGoogleApiClient == null) {
            LogUtils.LOGE("***> fragment onStart", "here");
            mActivity.getPreferences(Context.MODE_PRIVATE)
                    .registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);

            mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                    .addApi(Nearby.MESSAGES_API)
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mConnectionFailedListener)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        if (mGoogleApiClient.isConnected() && !mActivity.isChangingConfigurations()) {
            LogUtils.LOGE("***> fragment onStop", "here");
            // Using Nearby is battery intensive. To preserve battery, stop subscribing or
            // publishing when the fragment is inactive.
            unsubscribe();
            unpublish();

            updateSharedPreference(Constants.KEY_SUBSCRIPTION_TASK, Constants.TASK_NONE);
            updateSharedPreference(Constants.KEY_PUBLICATION_TASK, Constants.TASK_NONE);

            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;

            mActivity.getPreferences(Context.MODE_PRIVATE)
                    .unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
        }

        super.onStop();
    }

    /**
     * Execute any pending tasks for the pub/sub
     * This will be called when the Google Client API connects and when the activity result is handled
     */
    protected void executePendingTasks() {
        // If the message has already been sent and received, we do not need to handle the pubSub any further
        if (!PreferencesUtils.getBoolean(mActivity, R.string.key_message_received, false)) {
            executePendingSubscriptionTask();
            executePendingPublicationTask();
        }
    }

    /**
     * When permission errors are resolved, set the flag
     */
    protected void finishedResolvingNearbyPermissionError() {
        mResolvingNearbyPermissionError = false;
    }

    /**
     * Resets the state of pending subscription and publication tasks.
     */
    protected void resetToDefaultState() {
        mActivity.getPreferences(Context.MODE_PRIVATE)
                .edit()
                .putString(Constants.KEY_SUBSCRIPTION_TASK, Constants.TASK_NONE)
                .putString(Constants.KEY_PUBLICATION_TASK, Constants.TASK_NONE)
                .apply();
    }

    private static String connectionSuspendedCauseToString(int cause) {
        switch (cause) {
            case GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST:
                return "CAUSE_NETWORK_LOST";
            case GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED:
                return "CAUSE_SERVICE_DISCONNECTED";
            default:
                return "CAUSE_UNKNOWN: " + cause;
        }
    }

    /**
     * Display the message based on the device number
     */
    private void displayMessage() {
        LogUtils.LOGE("***> devices", mDeviceNum + "|" + mTotalDevices);
        if (PreferencesUtils.getBoolean(mActivity, R.string.key_is_connected, false)) {
            TextView scrollMessage = (TextView) mRootView.findViewById(R.id.scroll_message);
            List<Letter> list = UIUtils.generateLetterList(mDeviceNum, mTotalDevices);

            if (list.size() > 1) {
                scrollMessage.setVisibility(View.VISIBLE);
                scrollMessage.setText(mActivity.getString(R.string.text_scroll, mTotalDevices));
                scrollMessage.setTypeface(mTypeface);
            }

            mAdapter.addAll(list);
        }
    }

    /**
     * Invokes a pending task based on the subscription state.
     */
    private void executePendingSubscriptionTask() {
        String pendingSubscriptionTask = getPubSubTask(Constants.KEY_SUBSCRIPTION_TASK);
        if (TextUtils.equals(pendingSubscriptionTask, Constants.TASK_SUBSCRIBE)) {
            subscribe();
        } else if (TextUtils.equals(pendingSubscriptionTask, Constants.TASK_UNSUBSCRIBE)) {
            unsubscribe();
        }
    }

    /**
     * Invokes a pending task based on the publication state.
     */
    private void executePendingPublicationTask() {
        String pendingPublicationTask = getPubSubTask(Constants.KEY_PUBLICATION_TASK);
        if (TextUtils.equals(pendingPublicationTask, Constants.TASK_PUBLISH)) {
            publish();
        } else if (TextUtils.equals(pendingPublicationTask, Constants.TASK_UNPUBLISH)) {
            unpublish();
        }
    }

    /**
     * Retrieve the next device from Firebase and generate the appropriate message
     */
    private void getNextDeviceFirebase() {
        // Retrieve next deviceId from Firebase
        Firebase nextDeviceRef = new Firebase(Constants.FIREBASE_URL_DEVICES).child(mFirebaseUid);
        Query queryRef = nextDeviceRef.orderByChild(Constants.FIREBASE_PROPERTY_DEVICE_NUMBER).equalTo(mDeviceNum + 1);
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    Device d = item.getValue(Device.class);
                    if (d != null) {
                        LogUtils.LOGE("***> child updated", "device connected: " + d.isConnected());
                        // If the device has connected to the parent uid, handle the message display based on whether
                        // the message has been received by the child device
                        if (d.isConnected()) {
                            if (!PreferencesUtils.getBoolean(mActivity, R.string.key_message_received, false)){
                                PreferencesUtils.setBoolean(mActivity, R.string.key_message_received, true);

                                // Once the message has been received by the child device, unpublish the message so the
                                // next device can begin broadcasting
                                unpublish();
                                mDiscover.setVisibility(View.GONE);
                                mIntroConnectDevice.setText(mActivity.getString(R.string.text_message_received));
                            }
                        } else {
                            // The device has not been connected yet, so generate the message to be sent and prepare
                            // to publish
                            LogUtils.LOGE("***> child updated", "device: " + d.getDeviceId());

                            // Update message
                            String message = mActivity.getString(R.string.message_body, mFirebaseUid, mDeviceNum + 1,
                                    mTotalDevices, d.getDeviceId());
                            LogUtils.LOGE("***> child message", message);
                            mDeviceInfoMessage = DeviceMessage.newNearbyMessage(
                                    InstanceID.getInstance(mActivity.getApplicationContext()).getId(), message);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /**
     * Based on values stored in SharedPreferences, determines the subscription or publication task
     * that should be performed.
     */
    private String getPubSubTask(String taskKey) {
        return mActivity.getPreferences(Context.MODE_PRIVATE).getString(taskKey, Constants.TASK_NONE);
    }

    /**
     * Handle the incoming message from the nearby device. Parse the message to retrieve the appropriate data.
     */
    private void handleIncomingMessage(Message message) {
        LogUtils.LOGE("***> message", DeviceMessage.fromNearbyMessage(message).getMessageBody());

        String[] messageParts = DeviceMessage.fromNearbyMessage(message).getMessageBody().split(Pattern.quote("|"));

        if (messageParts.length == 0) {
            LogUtils.LOGE("***> error", "error parsing message");
            return;
        }

        mFirebaseUid = messageParts[0];
        mDeviceNum = Integer.valueOf(messageParts[1]);
        mTotalDevices = Integer.valueOf(messageParts[2]);
        String deviceId = messageParts[3];

        if (!PreferencesUtils.getBoolean(mActivity, R.string.key_is_connected, false)) {
            // Update Firebase and remove the old FirebaseUid
            String currentFirebaseUid = PreferencesUtils.getString(mActivity, R.string.key_firebase_uid, "");
            LogUtils.LOGE("***> handleIncomingMessage", "remove firebase uid:" + currentFirebaseUid);
            Firebase userChallengeRef = new Firebase(Constants.FIREBASE_URL_USERS).child(currentFirebaseUid);
            userChallengeRef.removeValue();
        }

        // Update the shared preferences
        PreferencesUtils.setString(mActivity, R.string.key_firebase_uid, mFirebaseUid);
        PreferencesUtils.setInt(mActivity, R.string.key_total_devices, mTotalDevices);
        PreferencesUtils.setInt(mActivity, R.string.key_device_number, mDeviceNum);
        PreferencesUtils.setBoolean(mActivity, R.string.key_is_connected, true);

        // Display the message
        displayMessage();

        // Update the background color
        setInitialValues();

        // Generate next message, based on Firebase
        getNextDeviceFirebase();

        // Update connected status on Firebase
        Map<String, Object> device = new HashMap<>();
        device.put(Constants.FIREBASE_PROPERTY_CONNECTED, true);

        Firebase deviceRef = new Firebase(Constants.FIREBASE_URL_DEVICES).child(mFirebaseUid).child(deviceId);
        deviceRef.updateChildren(device);
        deviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Device d = dataSnapshot.getValue(Device.class);
                LogUtils.LOGE("***> deviceRef", "here");
                if (d == null) {
                    LogUtils.LOGE("***> setupFirebase", "reset device");
                    MainActivity.resetDevice();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        if (mDeviceNum < mTotalDevices) {
            // Change the display text
            mIntroConnectDevice.setText(mActivity.getString(R.string.text_connect_device, mDeviceNum + 1));

            // Change text/button action
            mDiscover.setOnClickListener(mConnectedClickListener);
        } else {
            mIntroConnectDevice.setVisibility(View.GONE);
            mDiscover.setVisibility(View.GONE);
        }

        unsubscribe();
    }

    /**
     * Handle the publish request
     */
    private void handlePublish() {
        String publicationTask = getPubSubTask(Constants.KEY_PUBLICATION_TASK);
        if (TextUtils.equals(publicationTask, Constants.TASK_NONE) ||
                TextUtils.equals(publicationTask, Constants.TASK_UNPUBLISH)) {
            updateSharedPreference(Constants.KEY_PUBLICATION_TASK, Constants.TASK_PUBLISH);
        } else {
            updateSharedPreference(Constants.KEY_PUBLICATION_TASK, Constants.TASK_UNPUBLISH);
        }
    }

    /**
     * Handle the subscription request
     */
    private void handleSubscribe() {
        String subscriptionTask = getPubSubTask(Constants.KEY_SUBSCRIPTION_TASK);
        if (TextUtils.equals(subscriptionTask, Constants.TASK_NONE) ||
                TextUtils.equals(subscriptionTask, Constants.TASK_UNSUBSCRIBE)) {
            updateSharedPreference(Constants.KEY_SUBSCRIPTION_TASK, Constants.TASK_SUBSCRIBE);
        } else {
            updateSharedPreference(Constants.KEY_SUBSCRIPTION_TASK, Constants.TASK_UNSUBSCRIBE);
        }
    }

    /**
     * Handles errors generated when performing a subscription or publication action. Uses
     * {@link Status#startResolutionForResult} to display an opt-in dialog to handle the case
     * where a device is not opted into using Nearby.
     */
    private void handleUnsuccessfulNearbyResult(Status status) {
        LogUtils.LOGE(TAG, "processing error, status = " + status);
        if (status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN) {
            if (!mResolvingNearbyPermissionError) {
                try {
                    mResolvingNearbyPermissionError = true;
                    status.startResolutionForResult(mActivity, Constants.REQUEST_RESOLVE_ERROR);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (status.getStatusCode() == ConnectionResult.NETWORK_ERROR) {
                Toast.makeText(mActivity.getApplicationContext(),
                        "No connectivity, cannot proceed. Fix in 'Settings' and try again.",
                        Toast.LENGTH_LONG).show();
                resetToDefaultState();
            } else {
                // To keep things simple, pop a toast for all other error messages.
                Toast.makeText(mActivity.getApplicationContext(), "Unsuccessful: " +
                        status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }

    /**
     * Publishes device information to nearby devices. If not successful, attempts to resolve any
     * error related to Nearby permissions by displaying an opt-in dialog. Registers a callback
     * that updates the UI when the publication expires.
     */
    private void publish() {
        LogUtils.LOGE(TAG, "trying to publish");
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            PublishOptions options = new PublishOptions.Builder()
                    .setStrategy(PUB_SUB_STRATEGY)
                    .setCallback(new PublishCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                            LogUtils.LOGE(TAG, "no longer publishing");
                            updateSharedPreference(Constants.KEY_PUBLICATION_TASK, Constants.TASK_NONE);
                        }
                    }).build();

            if (mDeviceInfoMessage != null) {
                Nearby.Messages.publish(mGoogleApiClient, mDeviceInfoMessage, options)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }

                                if (status.isSuccess()) {
                                    publishMessage();
                                } else {
                                    LogUtils.LOGE(TAG, "could not publish");
                                    handleUnsuccessfulNearbyResult(status);
                                }
                            }
                        });
            }
        }
    }

    /**
     * Publish the message after the user connects their device
     */
    private void publishMessage() {
        // key_is_connected indicates the user has either connected their device to a parent device or in this case,
        // the device is the parent device.
        PreferencesUtils.setBoolean(mActivity, R.string.key_is_connected, true);
        displayMessage();

        mIntroConnectDevice.setText(mActivity.getString(R.string.text_waiting_for_device, mDeviceNum + 1));
        mDiscover.setVisibility(View.GONE);

        Toast.makeText(mActivity, mActivity.getString(R.string.toast_success, mDeviceNum + 1,
                mDeviceNum + 1), Toast.LENGTH_LONG).show();
        LogUtils.LOGE(TAG, "Parent published message for next device");
    }

    /**
     * Convert the fragment arguments to intents for use during the activity lifecycle
     */
    private void reloadFromArguments(Bundle savedInstanceState) {
        final Intent intent = MainActivity.fragmentArgumentsToIntent(savedInstanceState);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            mSource = extras.getString(Constants.Extra.SOURCE);
        }
    }

    /**
     * Based on the device number, set the background color
     */
    private void setInitialValues() {
        int randomInt = UIUtils.generateRandomInt(1, 11);

        // Create a unique "loading" message based on the device number
        mMessageResId = R.string.dialog_message_ready;
        int color = R.color.message_background_1;

        switch (randomInt) {
            case 1:
                color = R.color.message_background_1;
                mMessageResId = R.string.dialog_message_ready;
                break;
            case 2:
                color = R.color.message_background_2;
                mMessageResId = R.string.dialog_message_almost_there;
                break;
            case 3:
                color = R.color.message_background_3;
                mMessageResId = R.string.dialog_message_epic;
                break;
            case 4:
                color = R.color.message_background_4;
                mMessageResId = R.string.dialog_message_incoming;
                break;
            case 5:
                color = R.color.message_background_5;
                mMessageResId = R.string.dialog_message_patience;
                break;
            case 6:
                color = R.color.message_background_6;
                mMessageResId = R.string.dialog_message_ready;
                break;
            case 7:
                color = R.color.message_background_7;
                mMessageResId = R.string.dialog_message_stay_target;
                break;
            case 8:
                color = R.color.message_background_8;
                mMessageResId = R.string.dialog_message_wait;
                break;
            case 9:
                color = R.color.message_background_9;
                mMessageResId = R.string.dialog_message_epic;
                break;
            case 10:
                color = R.color.message_background_10;
                mMessageResId = R.string.dialog_message_incoming;
                break;
            case 11:
                color = R.color.message_background_11;
                mMessageResId = R.string.dialog_message_almost_there;
                break;
        }
        mContainer.setBackgroundColor(ContextCompat.getColor(mActivity, color));
    }

    /**
     * Setup Firebase for the user and connected devices
     *
     * Update the existing user data with the number of devices selected on the previous screen
     * Add the devices based on the selection
     * Generate the message for the next device
     */
    private void setupFirebase() {
        // Update Firebase to include the total number of devices
        if (!TextUtils.isEmpty(mFirebaseUid)) {
            Map<String, Object> profile = new HashMap<>();
            profile.put(Constants.FIREBASE_PROPERTY_NUMBER_DEVICES, mTotalDevices);

            Firebase userRef = new Firebase(Constants.FIREBASE_URL_USERS).child(mFirebaseUid);
            userRef.updateChildren(profile);
            LogUtils.LOGE("***> setupFirebase", "update firebase uid:" + mFirebaseUid);

            // Add device ids to Firebase
            for (int i = 0; i < PreferencesUtils.getInt(mActivity, R.string.key_total_devices, 1); i++) {
                // If the challengeCode is empty, then push a new value to the database
                Firebase deviceRef = new Firebase(Constants.FIREBASE_URL_DEVICES).child(mFirebaseUid);
                final Firebase newDeviceRef = deviceRef.push();

                mDeviceIds[i] = newDeviceRef.getKey();
                final int currentDeviceNumber = i + 1;

                LogUtils.LOGE("***> new device #" + currentDeviceNumber, mDeviceIds[i]);

                Device device = new Device(mDeviceIds[i], i + 1, mDeviceNum == currentDeviceNumber);
                newDeviceRef.setValue(device);
                newDeviceRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Device d = dataSnapshot.getValue(Device.class);
                        if (d != null) {
                            LogUtils.LOGE("***> child updated", "id: " + d.getDeviceId());
                            LogUtils.LOGE("***> child updated", "number: " + d.getDeviceNumber());
                            LogUtils.LOGE("***> child updated", "connected: " + d.isConnected());
                            LogUtils.LOGE("***> child updated", "next: " + currentDeviceNumber);

                            // Hide the button and show the message for the first device
                            if (d.isConnected() && d.getDeviceNumber() > 1) {
                                PreferencesUtils.setBoolean(mActivity, R.string.key_message_received, true);

                                LogUtils.LOGE("***> child updated", "unpublished: " + currentDeviceNumber);
                                unpublish();
                                mDiscover.setVisibility(View.GONE);
                                mIntroConnectDevice.setText(mActivity.getString(R.string.text_message_received));
                            }
                        } else {
                            if (!PreferencesUtils.getBoolean(mActivity, R.string.key_device_reset_done, false)) {
                                LogUtils.LOGE("***> setupFirebase", "reset device");
                                MainActivity.resetDevice();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

                // Create the message for the next device
                if (i == mDeviceNum) {
                    LogUtils.LOGE("***> device", "message created");
                    String message = mActivity.getString(R.string.message_body, mFirebaseUid, mDeviceNum + 1,
                            mTotalDevices, newDeviceRef.getKey());
                    mDeviceInfoMessage = DeviceMessage.newNearbyMessage(
                            InstanceID.getInstance(mActivity.getApplicationContext()).getId(), message);
                }
            }
        }
    }

    /**
     * Set up the various views within the fragment.
     * Set typeface, content, images, set event listeners, and generate the appropriate messages based on connection status
     */
    private void setupView() {
        mIntroConnectDevice = (TextView)mRootView.findViewById(R.id.intro_connect_device);
        mIntroConnectDevice.setTypeface(mTypeface);

        mDiscover = (ImageButton) mRootView.findViewById(R.id.button_share_discover);
        mDiscover.setOnClickListener(mClickListener);

        mIntroConnectDevice.setText(mSource.equals(Constants.Source.WELCOME) ?
                mActivity.getString(R.string.text_listen_device) :
                mActivity.getString(R.string.text_connect_device, mDeviceNum + 1));

        RecyclerView recyclerView = (RecyclerView) mRootView.findViewById(R.id.letter_list);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        // Define the gridlayout for the RecyclerView - column count will change based on rotation and device type
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity,
                mActivity.getResources().getInteger(R.integer.grid_items_per_row));
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));

        mAdapter = new Adapters.LetterAdapter(mActivity);
        recyclerView.setAdapter(mAdapter);

        ArrayList<Letter> list = new ArrayList<>();
        Letter item = new Letter();
        item.setImageResId(mSource.equals(Constants.Source.WELCOME) ? R.drawable.img_listen : R.drawable.img_publish);
        item.setContentDescription(R.string.button_get_started);
        list.add(0, item);

        mAdapter.addAll(list);

        // If connected from a previous session, simply display the letter, generate the message and set the button
        // to publish mode.
        if (PreferencesUtils.getBoolean(mActivity, R.string.key_is_connected, false)) {
            displayMessage();

            if (mTotalDevices == 1) {
                mIntroConnectDevice.setVisibility(View.GONE);
                mDiscover.setVisibility(View.GONE);
            } else {
                if (mDeviceNum == mTotalDevices) {
                    mDiscover.setVisibility(View.GONE);
                    mIntroConnectDevice.setVisibility(View.GONE);
                }

                // Retrieve the next devices from Firebase
                getNextDeviceFirebase();
            }
        } else {
            if (mSource.equals(Constants.Source.CONNECT)) {
                setupFirebase();
            }
        }

        mContainer = mRootView.findViewById(R.id.container_message);

        setInitialValues();

        if (PreferencesUtils.getBoolean(mActivity, R.string.key_message_received, false)) {
            mDiscover.setVisibility(View.GONE);
            mIntroConnectDevice.setText(mActivity.getString(R.string.text_message_received));
        }
    }

    /**
     * Subscribes to messages from nearby devices. If not successful, attempts to resolve any error
     * related to Nearby permissions by displaying an opt-in dialog. Registers a callback which
     * updates state when the subscription expires.
     */
    private void subscribe() {
        LogUtils.LOGE(TAG, "trying to subscribe");
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy(PUB_SUB_STRATEGY)
                    .setCallback(new SubscribeCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                            LogUtils.LOGE(TAG, "no longer subscribing");
                            updateSharedPreference(Constants.KEY_SUBSCRIPTION_TASK, Constants.TASK_NONE);
                        }
                    }).build();

            Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (status.isSuccess()) {
                                LogUtils.LOGE(TAG, "subscribed successfully");
                            } else {
                                LogUtils.LOGE(TAG, "could not subscribe");
                                handleUnsuccessfulNearbyResult(status);
                            }
                        }
                    });
        }
    }

    /**
     * Stops publishing device information to nearby devices. If successful, resets state. If not
     * successful, attempts to resolve any error related to Nearby permissions by displaying an
     * opt-in dialog.
     */
    private void unpublish() {
        LogUtils.LOGE(TAG, "trying to unpublish");
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending task in onConnected().
        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            if (mDeviceInfoMessage != null) {
                Nearby.Messages.unpublish(mGoogleApiClient, mDeviceInfoMessage)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }

                                if (status.isSuccess()) {
                                    LogUtils.LOGE(TAG, "unpublished successfully");
                                    updateSharedPreference(Constants.KEY_PUBLICATION_TASK, Constants.TASK_NONE);
                                } else {
                                    LogUtils.LOGE(TAG, "could not unpublish");
                                    handleUnsuccessfulNearbyResult(status);
                                }
                            }
                        });
            }
        }
    }

    /**
     * Ends the subscription to messages from nearby devices. If successful, resets state. If not
     * successful, attempts to resolve any error related to Nearby permissions by
     * displaying an opt-in dialog.
     */
    private void unsubscribe() {
        LogUtils.LOGE(TAG, "trying to unsubscribe");
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener)
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(@NonNull Status status) {
                            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (status.isSuccess()) {
                                LogUtils.LOGE(TAG, "unsubscribed successfully");
                                updateSharedPreference(Constants.KEY_SUBSCRIPTION_TASK, Constants.TASK_NONE);
                            } else {
                                LogUtils.LOGE(TAG, "could not unsubscribe");
                                handleUnsuccessfulNearbyResult(status);
                            }
                        }
                    });
        }
    }

    /**
     * Helper for editing entries in SharedPreferences.
     */
    private void updateSharedPreference(String key, String value) {
        mActivity.getPreferences(Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
    }

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            LogUtils.LOGE(TAG, "GoogleApiClient connected");
            Nearby.Messages.getPermissionStatus(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        executePendingTasks();
                    } else {
                        handleUnsuccessfulNearbyResult(status);
                    }
                }
            });
        }

        @Override
        public void onConnectionSuspended(int cause) {
            LogUtils.LOGE(TAG, "GoogleApiClient connection suspended: " + connectionSuspendedCauseToString(cause));
        }
    };

    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            LogUtils.LOGE(TAG, "connection to GoogleApiClient failed");
        }
    };

    /**
     * Handle the button click listener if the user has already connected and displaying a letter
     */
    private View.OnClickListener mConnectedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MainActivity.hide();

            mProgressDialog = DialogUtils.createSpinnerProgressDialog(mActivity, DialogUtils.DEFAULT_TITLE_ID,
                    mMessageResId, true, mTypeface);
            mProgressDialog.show();

            handlePublish();
        }
    };

    /**
     * Handle the button click listener based on the referring screen
     */
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MainActivity.hide();

            mProgressDialog = DialogUtils.createSpinnerProgressDialog(mActivity, DialogUtils.DEFAULT_TITLE_ID,
                    mMessageResId, true, mTypeface);
            mProgressDialog.show();

            if (mSource.equals(Constants.Source.WELCOME)) {
                handleSubscribe();
            } else {
                handlePublish();
            }
        }
    };

    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, final String key) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (TextUtils.equals(key, Constants.KEY_SUBSCRIPTION_TASK)) {
                                executePendingSubscriptionTask();
                            } else if (TextUtils.equals(key, Constants.KEY_PUBLICATION_TASK)) {
                                executePendingPublicationTask();
                            }
                        }
                    });
                }
            };
}
