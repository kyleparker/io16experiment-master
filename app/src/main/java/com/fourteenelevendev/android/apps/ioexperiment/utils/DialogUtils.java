package com.fourteenelevendev.android.apps.ioexperiment.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import com.fourteenelevendev.android.apps.ioexperiment.R;

/**
 * Dialog utility builder
 *
 * Created by kyleparker on 3/31/2016.
 */
public class DialogUtils {

    public static final int DEFAULT_LAYOUT_ID = -1;
    public static final int DEFAULT_MESSAGE_ID = -1;
    public static final int DEFAULT_NEGATIVE_TEXT_ID = -1;
    public static final int DEFAULT_POSITIVE_TEXT_ID = -1;
    public static final int DEFAULT_TITLE_ID = -1;
    /**
     * Create a MaterialDesign dialog
     *
     * @param activity
     * @param titleId
     * @param messageId
     * @param layoutId
     * @param positiveTextId
     * @param negativeTextId
     * @param cancelable
     * @param positiveCallback
     * @param negativeCallback
     * @param formatArgs
     * @return
     */
    private static MaterialDialog createDialogBase(Activity activity, int titleId, int messageId, int layoutId, int positiveTextId,
                                                   int negativeTextId, boolean cancelable,
                                                   MaterialDialog.SingleButtonCallback positiveCallback,
                                                   MaterialDialog.SingleButtonCallback negativeCallback, Object... formatArgs) {
        Typeface typeface = Typeface.createFromAsset(activity.getAssets(), Constants.APP_FONT);

        if (negativeCallback == null) {
            negativeCallback = new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            };
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                .cancelable(cancelable)
                .onPositive(positiveCallback)
                .onNegative(negativeCallback)
                .typeface(typeface, typeface);

        if (titleId > DEFAULT_TITLE_ID) {
            builder.title(titleId);
        }

        if (messageId > DEFAULT_MESSAGE_ID) {
            builder.content(activity.getString(messageId, formatArgs));
        }

        if (layoutId > DEFAULT_LAYOUT_ID) {
            builder.customView(layoutId, true);
        }

        if (positiveTextId > DEFAULT_POSITIVE_TEXT_ID) {
            builder.positiveText(positiveTextId);
        }

        if (negativeTextId > DEFAULT_NEGATIVE_TEXT_ID) {
            builder.negativeText(negativeTextId);
        }

        return builder.build();
    }

    /**
     * Create a progress dialog for the loading spinner or progress indicator
     * <p/>
     * @param activity
     * @param spinner
     * @param titleId
     * @param messageId
     * @param cancelable
     * @param max
     * @param onCancelListener
     * @param typeface
     * @param formatArgs
     * @return
     */
    private static MaterialDialog createProgressDialog(final Activity activity, boolean spinner, int titleId, int messageId,
                                                       boolean cancelable, int max, DialogInterface.OnCancelListener onCancelListener,
                                                       Typeface typeface, Object... formatArgs) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                .cancelable(cancelable);

        if (titleId > DEFAULT_TITLE_ID) {
            builder.title(messageId > DEFAULT_MESSAGE_ID ? activity.getString(titleId) : activity.getString(titleId, formatArgs));
        }
        if (messageId > DEFAULT_MESSAGE_ID) {
            builder.content(activity.getString(messageId, formatArgs)).typeface(typeface, typeface);
        }
        if (onCancelListener != null) {
            builder.cancelListener(onCancelListener);
        }
        if (spinner) {
            builder.progress(true, 0);
        } else {
            builder.progress(false, max, true);
        }

        return builder.build();
    }

    /**
     * Create a MaterialDesign dialog
     *
     * @param activity
     * @param titleId
     * @param messageId
     * @param positiveTextId
     * @param negativeTextId
     * @param cancelable
     * @param positiveCallback
     * @param negativeCallback
     * @return
     */
    public static MaterialDialog createDialog(Activity activity, int titleId, int messageId, int positiveTextId, int negativeTextId,
                                              boolean cancelable, MaterialDialog.SingleButtonCallback positiveCallback,
                                              MaterialDialog.SingleButtonCallback negativeCallback) {
        return createDialogBase(activity, titleId, messageId, DEFAULT_LAYOUT_ID, positiveTextId, negativeTextId,
                cancelable, positiveCallback, negativeCallback);
    }

    /**
     * Create an item dialog
     *
     * @param activity
     * @param titleId
     * @param items
     * @param listCallback
     * @param callback
     * @return
     */
    public static MaterialDialog createItemDialog(Activity activity, int titleId, int items, MaterialDialog.ListCallback listCallback,
                                                  MaterialDialog.SingleButtonCallback callback) {
        Typeface typeface = Typeface.createFromAsset(activity.getAssets(), Constants.APP_FONT);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                .items(items)
                .itemsCallback(listCallback)
                .onNegative(callback)
                .negativeText(R.string.dialog_cancel)
                .typeface(typeface, typeface);

        if (titleId > DialogUtils.DEFAULT_TITLE_ID) {
            builder.title(titleId);
        }

        return builder.build();
    }

    /**
     * Creates a spinner progress dialog.
     *
     * @param activity
     * @param titleId
     * @param messageId
     * @param cancelable
     * @param typeface
     * @return
     */
    public static MaterialDialog createSpinnerProgressDialog(Activity activity, int titleId, int messageId, boolean cancelable,
                                                             Typeface typeface) {
        return createProgressDialog(activity, true, titleId, messageId, cancelable, 0, null, typeface);
    }
}
