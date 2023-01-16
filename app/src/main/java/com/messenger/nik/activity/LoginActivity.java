package com.messenger.nik.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.messenger.nik.R;
import com.messenger.nik.helper.Constants;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    //CONSTANT
    private static final String TAG = LoginActivity.class.getSimpleName();

    //UI COMPONENTS
    private TextView agree_and_continue_button_title;
    private ProgressBar agree_and_continue_progress_bar;

    //ANDROID RESOURCE CLASS
    private Context context;

    //FIREBASE DATABASE CLASS
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    public void onPause() {
        super.onPause();

        //update the status of login to false
        updateLoginButton(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Set the context to null
        context = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize context
        context = LoginActivity.this;

        setContentView(R.layout.activity_login_ui);

        //Agree and continue button initialize
        findViewById(R.id.agree_and_continue_button).setOnClickListener(v -> alertDialog());
        //Agree and continue button title view
        agree_and_continue_button_title = findViewById(R.id.agree_and_continue_button_title);
        //Agree and continue progress bar initialize
        agree_and_continue_progress_bar = findViewById(R.id.agree_and_continue_progress_bar);
    }

    private void alertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.accept_terms_and_privacy_title);
        builder.setMessage(R.string.accept_terms_and_privacy_msg);
        builder.setPositiveButton(android.R.string.ok,(dialog, which) -> {
            //Dismiss the dialog
            dialog.dismiss();
            //Update the login button
            updateLoginButton(true);
            //generate and store the virtual number in database
            storeVirtualNumberInDatabase();
        });
        builder.setNegativeButton(android.R.string.no,(dialog, which) -> {
            //TODO cancel the dialog and view T&C and privacy policy of the app
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void updateLoginButton(boolean update) {
        if (update) {
            agree_and_continue_button_title.setVisibility(View.GONE);
            agree_and_continue_progress_bar.setVisibility(View.VISIBLE);
        } else {
            agree_and_continue_progress_bar.setVisibility(View.GONE);
            agree_and_continue_button_title.setVisibility(View.VISIBLE);
        }
    }

    private String generateRandomString() {
        String s = "1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        Random rnd = new Random();
        while (stringBuilder.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * s.length());
            stringBuilder.append(s.charAt(index));
        }
        return stringBuilder.toString();
    }

    private void storeVirtualNumberInDatabase() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Path to check if virtual_number exits or not
                String path = Constants.DB_VN + "/" + generateRandomString();
                DataSnapshot registered_virtual_numbers = snapshot.child(path);

                //dataSnapshot of registering virtual number in database
                DataSnapshot register_virtual_number = snapshot.child(Constants.DB_VN);

                if (registered_virtual_numbers.exists()) {
                    Log.e(TAG, "onError:virtual number exists");
                    storeVirtualNumberInDatabase();
                } else {
                    Log.i(TAG, "virtual number doesn't exists");
                    String virtual_number = generateRandomString();
                    register_virtual_number.getRef().child(virtual_number).setValue("r")
                            .addOnSuccessListener(unused -> {
                                updateLoginButton(false);
                                //Pass the virtual number to next fragment (VNGFragment)
                                //Constants.current_user_virtual_number = virtual_number;
                                //Load the VNGFragment
                                //startActivity(new Intent(LoginActivity.this, VNGActivity.class));
                                //finish();

                                // get the generated virtual number
                                String vn = generateRandomString();

                                // Create the Intent object of this class Context() to Second_activity class
                                Intent intent = new Intent(LoginActivity.this, VNGActivity.class);

                                // now by putExtra method put the value in key, value pair
                                // key is message_key by this key we will receive the value, and put the string
                                intent.putExtra("virtual_number", vn);

                                // start the Intent
                                startActivity(intent);
                                finish();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG,error.getDetails());
                updateLoginButton(false);
            }
        };
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }
}
