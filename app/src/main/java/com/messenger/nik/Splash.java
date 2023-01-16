package com.messenger.nik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.messenger.nik.activity.FragmentLoadActivity;
import com.messenger.nik.activity.LoginActivity;
import com.messenger.nik.helper.Constants;
import com.messenger.nik.helper.Util;

public class Splash extends AppCompatActivity {

    //CONSTANT
    private static final String TAG = Splash.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the current user virtual_number from shared preferences
        //and store it in constants virtual_number so it can access later anywhere
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SP,0);
        //if sharePreferences contains virtual_number then fetch the virtual_number
        if (sharedPreferences.contains("virtual_number")) {
            //store the virtual_number in constants
            Constants.current_user_virtual_number = Util.getValueFromSharedPreferences(Splash.this,"virtual_number");
        }

        //Initialize Firebase Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        //create a valueEvent listener so that we can check if user is new
        //to app or existing user according to that we will startActivity
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //If virtual number is not equals to null then proceed for
                //checking if user is new or not
                //close the splash activity
                if (Constants.current_user_virtual_number !=null) {
                    //create a path to database
                    String path = Constants.DB_VN + "/" + Constants.current_user_virtual_number;
                    DataSnapshot virtual_number = snapshot.child(path);

                    //if virtual number exits in database it means user is not new to the app
                    if (virtual_number.exists()) {
                        startActivity(new Intent(Splash.this, FragmentLoadActivity.class));
                    } else {
                        //virtual number doesn't exist so user is new to the app
                        startActivity(new Intent(Splash.this, LoginActivity.class));
                    }
                } else {
                    //if virtual_number is null it means user is new to the app
                    Log.e(TAG,"onError: Constants.virtual_number was null");
                    startActivity(new Intent(Splash.this, LoginActivity.class));
                }
                finish(); //close the splash activity
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }
}
