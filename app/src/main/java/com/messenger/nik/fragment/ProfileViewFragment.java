package com.messenger.nik.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.request.RequestOptions;
import com.messenger.nik.R;

import java.lang.ref.WeakReference;

public class ProfileViewFragment extends Fragment {

    //CONSTANTS
    private String photo_url = null;
    private String virtual_number = null;
    private String name = null;

    //UI COMPONENTS
    private ImageView profile_photo;
    private TextView user_virtual_number, user_name;

    //WeakReference
    public static WeakReference<ProfileViewFragment> weakActivity;

    public ProfileViewFragment() {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //Clear constants values
        photo_url = null;
        virtual_number = null;
        name = null;

        //Remove the view
        profile_photo = null;
        user_virtual_number = null;
        user_name = null;

        //set the weak activity to null
        weakActivity = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //weakActivity initializing
        weakActivity = new WeakReference<>(ProfileViewFragment.this);
    }

    public static ProfileViewFragment get() {
        return weakActivity.get();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile_info_ui,container,false);

        profile_photo = view.findViewById(R.id.user_profile_photo);
        user_virtual_number = view.findViewById(R.id.user_virtual_number);
        user_name = view.findViewById(R.id.user_name);

        user_virtual_number.setText( virtual_number );
        user_name.setText( name );

        GlideApp.with( requireContext() )
                .load(photo_url)
                .apply(RequestOptions.circleCropTransform())
                .into( profile_photo );

        return view;
    }

    protected void receivedData( String photo_url, String virtual_number, String name ) {
        this.photo_url = photo_url;
        this.virtual_number = virtual_number;
        this.name = name;
    }
}
