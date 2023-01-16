package com.messenger.nik.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.messenger.nik.R;
import com.messenger.nik.fragment.RCSFragment;

import java.lang.ref.WeakReference;

public class FragmentLoadActivity extends AppCompatActivity {

    //CONSTANT
    private static final String TAG = FragmentLoadActivity.class.getSimpleName();

    //WeakReference
    public static WeakReference<FragmentLoadActivity> weakActivity;

    @Override
    public void onBackPressed() {

        if ( getSupportFragmentManager().getBackStackEntryCount() == 1 ) {
            finish();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_load);
        //weakActivity initializing
        weakActivity = new WeakReference<>(FragmentLoadActivity.this);

        loadFragment(new RCSFragment(), "RCSFragment");
    }

    public static FragmentLoadActivity get() {
        return weakActivity.get();
    }

    public void loadFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void toast(String text, int duration) {
        Toast.makeText(FragmentLoadActivity.this, text, duration).show();
    }
}