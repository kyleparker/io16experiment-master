package com.fourteenelevendev.android.apps.ioexperiment.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import com.fourteenelevendev.android.apps.ioexperiment.R;
import com.fourteenelevendev.android.apps.ioexperiment.utils.Constants;
import com.fourteenelevendev.android.apps.ioexperiment.utils.DialogUtils;

/**
 * Display the welcome message to visitors.
 *
 * Created by kyleparker on 3/30/2016.
 */
public class WelcomeFragment extends Fragment {
    private FragmentActivity mActivity;
    private ViewGroup mRootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_welcome, container, false);
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
            throw new ClassCastException(context.toString() + " must implement " + WelcomeFragment.class.getSimpleName());
        }

        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    private void setupView() {
        Typeface typeface = Typeface.createFromAsset(mActivity.getAssets(), Constants.APP_FONT);
        TextView introAppChallenge = (TextView)mRootView.findViewById(R.id.intro_app_challenge);
        introAppChallenge.setTypeface(typeface);

        TextView introCreatedBy = (TextView)mRootView.findViewById(R.id.intro_created_by);
        introCreatedBy.setTypeface(typeface);

        Button getStarted = (Button) mRootView.findViewById(R.id.button_get_started);
        getStarted.setOnClickListener(mClickListener);
    }

    /**
     * Handle the click listener for the text/button
     */
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MainActivity.hide();

            MaterialDialog.ListCallback listCallback = new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog dialog, View view, int position, CharSequence text) {
                    switch (position) {
                        case 0:
                            mCallbacks.onGetStarted();
                            break;
                        case 1:
                            mCallbacks.onDiscover(Constants.Source.WELCOME);
                            break;
                    }
                }
            };

            DialogUtils.createItemDialog(mActivity, DialogUtils.DEFAULT_TITLE_ID, R.array.get_started_options,
                    listCallback, null).show();
        }
    };

    public interface Callbacks {
        void onGetStarted();
        void onDiscover(String source);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onGetStarted() {  }
        @Override
        public void onDiscover(String source) {  }
    };

    private Callbacks mCallbacks = sDummyCallbacks;
}
