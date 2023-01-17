package com.messenger.nik.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.messenger.nik.R;
import com.messenger.nik.fragment.GlideApp;

public class UserStatusViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();

        setContentView(R.layout.activity_user_status_view);

        ImageView statusView = findViewById(R.id.status_view);
        GlideApp.with(statusView)
                .load(bundle.getString("status_url"))
                .into(statusView);
    }
}
