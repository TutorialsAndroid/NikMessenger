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
import com.messenger.nik.activity.FragmentLoadActivity;
import com.messenger.nik.helper.Constants;

import java.lang.ref.WeakReference;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    //CONSTANTS
    //private static final String TAG = ProfileFragment.class.getSimpleName();
    public static WeakReference<ProfileFragment> weakReference;

    //UI COMPONENTS
    private ImageView current_user_profile_photo;
    private TextView current_user_name,
            current_user_virtual_number,
            share_quick_status_button,
            send_feedBack_button,
            report_problem_button,
            app_info_button;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        current_user_profile_photo = null;
        current_user_name = null;
        current_user_virtual_number = null;
        share_quick_status_button = null;
        send_feedBack_button = null;
        report_problem_button = null;
        app_info_button= null;
    }

    public ProfileFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        weakReference = new WeakReference<>(ProfileFragment.this);
    }

    public static ProfileFragment get() {
        return weakReference.get();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        //Initialize current user profile photo image view
        current_user_profile_photo = view.findViewById(R.id.current_user_profile_photo);
        //Initialize current user name text-view
        current_user_name = view.findViewById(R.id.current_user_name);
        //Initialize current user virtual number text-view
        current_user_virtual_number = view.findViewById(R.id.current_user_virtual_number);
        //Initialize share status button
        share_quick_status_button = view.findViewById(R.id.share_quick_status_button);
        share_quick_status_button.setOnClickListener(this);
        //Initialize send feedBack button
        send_feedBack_button = view.findViewById(R.id.send_feedBack_button);
        send_feedBack_button.setOnClickListener(this);
        //Initialize report problem button
        report_problem_button = view.findViewById(R.id.report_problem_button);
        report_problem_button.setOnClickListener(this);
        //Initialize app info button
        app_info_button = view.findViewById(R.id.app_info_button);
        app_info_button.setOnClickListener(this);

        //Load default user profile photo
        GlideApp.with( current_user_profile_photo.getContext() )
                .load( Constants.current_user_avatar )
                .apply(RequestOptions.circleCropTransform())
                .into( current_user_profile_photo );

        current_user_name.setText( Constants.current_user_name );
        current_user_virtual_number.setText( Constants.current_user_virtual_number );

        return view;
    }

    @Override
    public void onClick(View v) {
        if ( v == share_quick_status_button) {
            FragmentLoadActivity.get().loadFragment( new CameraFragment(), "CameraFragment" );
        }

        if ( v == send_feedBack_button ) { sendFeedBack(); }

        if ( v == report_problem_button ) { reportProblem(); }

        if ( v == app_info_button ) { appInfo(); }
    }

    private void sendFeedBack() {}

    private void reportProblem() {}

    private void appInfo() {}
}