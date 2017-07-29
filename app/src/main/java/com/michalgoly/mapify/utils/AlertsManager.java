package com.michalgoly.mapify.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.michalgoly.mapify.R;

public class AlertsManager {
    private static final String TAG = "AlertsManager";

    /**
     * Shows an alert dialog to the user and exits the app. This method can be used when
     * the app crashes for an unexpected reason and the crash would prevent the user from
     * using it.
     *
     * @param context Context - The context necessary to construct the AlertDialog
     * @param message String - The message to display to the user
     */
    public static void alertAndExit(final Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setTitle(R.string.alert_dialog_error_title)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Activity) context).finishAffinity();
            }
        });
        Log.e(TAG, message);
        builder.show();
    }
}
