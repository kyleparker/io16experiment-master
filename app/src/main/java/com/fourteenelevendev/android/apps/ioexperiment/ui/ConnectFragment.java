package com.fourteenelevendev.android.apps.ioexperiment.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import com.fourteenelevendev.android.apps.ioexperiment.R;
import com.fourteenelevendev.android.apps.ioexperiment.utils.Constants;
import com.fourteenelevendev.android.apps.ioexperiment.utils.DialogUtils;
import com.fourteenelevendev.android.apps.ioexperiment.utils.PreferencesUtils;

/**
 * Connect the devices
 *
 * Created by kyleparker on 3/30/2016.
 */
public class ConnectFragment extends Fragment {
    private FragmentActivity mActivity;
    private ViewGroup mRootView;
    private Spinner mSpinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_connect, container, false);
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof Callbacks)) {
            throw new ClassCastException(context.toString() + " must implement " + ConnectFragment.class.getSimpleName());
        }

        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    /**
     * Set up the various views within the fragment.
     * Set typeface, content, images, set event listeners, and generate adapter for the quantity dropdown
     */
    private void setupView() {
        Typeface typeface = Typeface.createFromAsset(mActivity.getAssets(), Constants.APP_FONT);
        TextView introConnect = (TextView)mRootView.findViewById(R.id.intro_connect);
        introConnect.setTypeface(typeface);

        TextView introDevices = (TextView)mRootView.findViewById(R.id.intro_number_devices);
        introDevices.setTypeface(typeface);

        CharSequence[] items = new CharSequence[11];
        for (int i = 0; i < 11; i++) {
            items[i] = Integer.toString(i + 1);
        }

        mSpinner = (Spinner) mRootView.findViewById(R.id.spinner_devices);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(mActivity, R.layout.spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PreferencesUtils.setInt(mActivity, R.string.key_total_devices, position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MainActivity.hide();
                return false;
            }
        });

        Button letsGo = (Button) mRootView.findViewById(R.id.button_lets_go);
        letsGo.setOnClickListener(mClickListener);
    }

    /**
     * Handle the click listener for the text/button
     */
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MainActivity.hide();

            if (mSpinner.getSelectedItemPosition() == 0) {
                MaterialDialog.SingleButtonCallback negativeCallback = new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PreferencesUtils.setBoolean(mActivity, R.string.key_is_connected, true);
                        mCallbacks.onLetsGo();
                    }
                };

                DialogUtils.createDialog(mActivity, DialogUtils.DEFAULT_TITLE_ID, R.string.dialog_single_device,
                        R.string.dialog_i_found_one, R.string.dialog_nope_just_one, true, null, negativeCallback).show();
            } else {
                mCallbacks.onLetsGo();
            }
        }
    };

    /**
     * Callbacks
     */
    public interface Callbacks {
        void onLetsGo();
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onLetsGo() {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;
}
