package com.messenger.nik.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.messenger.nik.R;

public class Util {

    private static AlertDialog progressDialog;

    /**
     * Global method to store values in shared-preferences
     * @param context pass the context of the specific-class
     * @param key shared-preferences key value in string
     * @param value shared-preferences value in string
     */
    public static void storeSharedPreference(Context context,String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SP, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key,value);
        editor.apply();
    }

    /**
     * Global method to get value in shared-preferences
     * @param context pass the context of the specific-class
     * @param key shared-preferences key value in string
     * @return the shared-preference value
     */
    public static String getValueFromSharedPreferences(Context context, String key) {
        String prefValueNew = "null";
        SharedPreferences preferences = context.getSharedPreferences(Constants.SP, 0);
        String prefValue = preferences.getString(key, "");
        if (!prefValue.equalsIgnoreCase("")) {
            /* Edit the value here*/
            prefValueNew = prefValue;
        }
        return prefValueNew;
    }

    public static void showLoading(Context context) {
        MaterialAlertDialogBuilder progress = new MaterialAlertDialogBuilder(context);

        progress.setView( R.layout.progress_dialog );
        progress.setCancelable( false );
        progressDialog = progress.create();
        progressDialog.show();
    }

    public static void hideLoading() {
        progressDialog.dismiss();
    }
}