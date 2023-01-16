package com.messenger.nik.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.messenger.nik.R;
import com.messenger.nik.helper.Util;

public class VNGActivity extends AppCompatActivity {

    //CONSTANT
    //private static final String TAG = VNGActivity.class.getSimpleName();

    //ANDROID RESOURCE CLASS
    private Context context;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //set context to null
        context = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize context
        context = VNGActivity.this;

        setContentView(R.layout.activity_vng_ui);

        //Initialize Views
        TextView virtual_number_view = findViewById(R.id.virtual_number_view);

        // create the get Intent object
        Intent intent = getIntent();

        // receive the virtual number generated value by getStringExtra() method
        // and key must be same which is send by login activity
        String vn = intent.getStringExtra("virtual_number");

        // display the string into textView
        virtual_number_view.setText(vn);

        //Set the onclick listener to virtual_number_view so that
        //user can copy the virtual number on click.
        virtual_number_view.setOnClickListener(v -> {
            //Copy the virtual number to clipboard
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("virtual number", virtual_number_view.getText());
            clipboard.setPrimaryClip(clip);
            //Show the message to user
            Toast.makeText(context, "Number Copied", Toast.LENGTH_SHORT).show();
        });
        //Initialize virtual number done button
        findViewById(R.id.virtual_number_done_button)
                .setOnClickListener(v -> {

                    //put the generated virtual number in shared prefs
                    Util.storeSharedPreference(context,"virtual_number",vn);

                    //now start the another activity
                    startActivity(new Intent(VNGActivity.this, SetUpUserActivity.class));
                    finish();
                });
    }
}