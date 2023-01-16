package com.messenger.nik.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.messenger.nik.R;
import com.messenger.nik.helper.Constants;
import com.messenger.nik.helper.Util;
import com.messenger.nik.models.RegisteredUserModel;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class SetUpUserActivity extends AppCompatActivity {

    //CONSTANT
    private static final String TAG = SetUpUserActivity.class.getSimpleName();
    private static boolean isFirstTime = true;

    //ANDROID RESOURCE CLASS
    private Context context;

    //UI COMPONENTS
    private EditText enter_user_name_edit_text;

    //FIREBASE DATABASE CLASS
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    public void onResume() {
        super.onResume();

        //If is first time is true then don't initialize the context
        if (!isFirstTime) {
            //If context is null then initialize it again
            if (context == null) {
                context = SetUpUserActivity.this;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //Set the isFirstTime to false
        isFirstTime = false;

        //Set the context to null
        context = null;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize Context
        context = SetUpUserActivity.this;

        setContentView(R.layout.activity_set_up_profile);

        //Initialize enter user name edit text
        enter_user_name_edit_text = findViewById(R.id.enter_user_name_edit_text);
        //done adding user name button initialize
        ImageView done_adding_name_button = findViewById(R.id.done_adding_name_button);
        //initialize done adding user name button click listener
        done_adding_name_button.setOnClickListener(v -> {
            if (!enter_user_name_edit_text.getText().toString().isEmpty()) {
                //check if user-name exits or not
                String un = String.valueOf( enter_user_name_edit_text.getText() );
                // Call the replaceAll() method to remove white space from string
                un = un.replaceAll("\\s", "");
                //call the check user name exits method
                checkUserNameExits( un );
            } else {
                //user didn't enter the text
                Toasty.error(context, "Please enter your user-name", Toast.LENGTH_SHORT, true).show();
            }
        });
    }

    /**
     * this method will check if user typed user-name exits or not
     * if user-name doesn't exits then it will save user-name to database
     * @param userName typed by user in edit-text
     */
    private void checkUserNameExits(String userName) {
        Query query = databaseReference.child(Constants.DB_VN)
                .orderByChild("/name").equalTo(userName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0){
                    //user-name exits so show user an error
                    Toasty.error(context, "User-Name Exits...", Toast.LENGTH_SHORT, true).show();

                    Log.e(TAG,"onError: user-name exits");
                    enter_user_name_edit_text.setText(null);
                } else {
                    //user-name doesn't exits so save user to database
                    saveUserNameToDB(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    /**
     * method to save user-information to database
     * @param userName of the user
     */
    private void saveUserNameToDB(String userName) {
        //path registered_users --> current_user_virtual_number --> user_name + ip + date_joined
        /* example how data structure will look
            12345678
                |--user_name : akshay
                |--ip: 192.168.15.1
                |--date_joined: 18-07-21
         */

        String ip = getIPAddress();
        String date_joined = Calendar.getInstance().getTime().getTime()+"";
        String avatar = "https://api.multiavatar.com/"+userName+".png";

        String notification_key = databaseReference.push().getKey();

        //Get the value from shared preference and store it in string
        Constants.current_user_virtual_number = Util.getValueFromSharedPreferences( context, "virtual_number" );

        RegisteredUserModel rm = new RegisteredUserModel(
                userName,
                avatar,
                ip,
                date_joined
        );
        databaseReference.child(Constants.DB_VN)
                .child( Constants.current_user_virtual_number ).setValue(rm)
        .addOnSuccessListener(unused -> {
            startActivity(new Intent(SetUpUserActivity.this,FragmentLoadActivity.class));
            finish();
        });
    }


    private static String getIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : inetAddresses) {
                    if (!address.isLoopbackAddress()) {
                        String hostAddress = address.getHostAddress();
                        boolean isIPv4 = hostAddress.indexOf(':')<0;

                        if (isIPv4)
                            return hostAddress;
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }
}
