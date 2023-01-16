package com.messenger.nik.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.messenger.nik.R;
import com.messenger.nik.adapter.RCSAdapter;
import com.messenger.nik.helper.Constants;
import com.messenger.nik.customInterface.RCSUserData;
import com.messenger.nik.helper.Util;
import com.messenger.nik.models.RCModel;

import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

import es.dmoral.toasty.Toasty;

public class ContactsFragment extends Fragment implements View.OnClickListener, RCSUserData {

    //CONSTANTS
    private static final String TAG = RCSFragment.class.getSimpleName();
    private static String another_user_name = null;
    private static String another_user_avatar = null;
    private static String another_user_notification_key = null;
    private static String groupName = null;
    private static String avatar = null;
    private static String grpVn = null;
    private static String crID = null;
    private static String vn = null;

    //ANDROID RESOURCE CLASS
    private RecyclerView.AdapterDataObserver adapterDataObserver;
    private LinearLayoutManager linearLayoutManager;
    private Context context;
    private Activity activity;

    //UI COMPONENTS
    private RelativeLayout no_contacts_found_view;
    private RecyclerView contacts_recycler_view;
    private TextView add_person_summary;
    private EditText add_person_edit_text;
    private FloatingActionButton add_person_button,add_person_button_done,cancel_add_person_button;
    private ImageView create_group_button, join_group_button;

    //FIREBASE DATABASE CLASS
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private final DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference();
    private final DatabaseReference groupInfoReference = FirebaseDatabase.getInstance().getReference();
    private RCSAdapter rcsAdapter;

    @Override
    public void onResume() {
        super.onResume();

        //start listening to firebase adapter
        rcsAdapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();

        //set the activity to null
        activity = null;

        //Set the context to null
        context = null;

        //stop listening to firebase adapter
        rcsAdapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //Remove the views to prevent memory leaks
        rcsAdapter.unregisterAdapterDataObserver(adapterDataObserver);
        adapterDataObserver = null;
        contacts_recycler_view.setAdapter(null);
        add_person_edit_text.removeTextChangedListener(null);
        linearLayoutManager = null;
        no_contacts_found_view = null;
        contacts_recycler_view = null;
        add_person_button = null;
        add_person_button_done = null;
        cancel_add_person_button = null;
        add_person_edit_text = null;
        add_person_summary = null;
        create_group_button = null;
        join_group_button = null;
    }

    public ContactsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize context
        context = requireContext();
        //Initialize activity
        activity = requireActivity();

        Constants.CONTACTS_DB_PATH = Constants.CONTACTS_DB +"/" + Constants.current_user_virtual_number;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_ui,container,false);

        create_group_button = view.findViewById(R.id.create_group_button);
        create_group_button.setOnClickListener(this);

        join_group_button = view.findViewById(R.id.join_group_button);
        join_group_button.setOnClickListener(this);

        //Initialize add person button
        add_person_button = view.findViewById(R.id.add_person_button);
        add_person_button.setOnClickListener(this);

        //Initialize add person button done
        add_person_button_done = view.findViewById(R.id.add_person_button_done);
        add_person_button_done.setOnClickListener(this);

        //Initialize cancel add person button
        cancel_add_person_button = view.findViewById(R.id.cancel_add_person_button);
        cancel_add_person_button.setOnClickListener(this);

        //Initialize the add person summary
        add_person_summary = view.findViewById(R.id.enter_virtual_number_summary);

        //Initialize add person edit text
        add_person_edit_text = view.findViewById(R.id.add_person_edit_text);

        //Initialize edit-text listener
        //Edit Text Change Listener
        add_person_edit_text.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) { }

            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                //When screen is rotated addTextChangedListener is triggered
                //and it cause to show fab button like closeComment button and
                //edit text etc.. So we have used hasFocus method in if condition
                //so this method will only triggered when user manually focus on the
                //view.
                if (add_person_edit_text.hasFocus()) {
                    // Do whatever
                    if (charSequence.toString().trim().length() == 0) {
                        cancel_add_person_button.setVisibility(View.VISIBLE);
                        add_person_button.setVisibility(View.GONE);
                    } else {
                        add_person_button.setVisibility(View.GONE);
                        cancel_add_person_button.setVisibility(View.GONE);
                        add_person_button_done.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        //Initialize no recent chats found layout
        no_contacts_found_view = view.findViewById(R.id.no_contacts_found_view);

        //Initialize recent chat recycler view here
        contacts_recycler_view = view.findViewById(R.id.contacts_recycler_view);

        updateRecyclerViewUI(true);

        //Initialize linear layout manager
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);

        Query query = FirebaseDatabase.getInstance().getReference()
                .child( Constants.CONTACTS_DB +"/" + Constants.current_user_virtual_number );

        FirebaseRecyclerOptions<RCModel> options = new FirebaseRecyclerOptions.Builder<RCModel>()
                .setQuery(query, RCModel.class).setLifecycleOwner(this).build();

        rcsAdapter = new RCSAdapter( options,this );

        adapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = rcsAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 || (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    contacts_recycler_view.scrollToPosition(positionStart);
                }
                updateRecyclerViewUI(friendlyMessageCount == 0);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                int friendlyMessageCount = rcsAdapter.getItemCount();
                updateRecyclerViewUI(friendlyMessageCount == 0);
            }
        };
        rcsAdapter.registerAdapterDataObserver(adapterDataObserver);

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        contacts_recycler_view.setLayoutManager(linearLayoutManager);
        contacts_recycler_view.setAdapter(rcsAdapter);
        rcsAdapter.startListening();

        //add vertical line between recycler view items
        contacts_recycler_view.addItemDecoration(new DividerItemDecoration(contacts_recycler_view.getContext(), DividerItemDecoration.VERTICAL));
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == create_group_button) { createGroup(); }

        if (v == join_group_button) { joinGroup(); }

        if (v == add_person_button) { updateAddPersonEditText(true); }

        if (v == cancel_add_person_button) { updateAddPersonEditText(false); }

        if (v == add_person_button_done) { fetchUserDetails(); }
    }

    private void createGroup() {
        View customAlertDialogView = LayoutInflater.from( context ).inflate( R.layout.create_group_dialog, null, false );

        TextInputLayout groupNameField = customAlertDialogView.findViewById( R.id.group_name_field );

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder( context );
        dialogBuilder.setView( customAlertDialogView );

        dialogBuilder.setTitle( R.string.create_group );
        dialogBuilder.setIcon(R.drawable.ic_twotone_groups_icon);
        dialogBuilder.setPositiveButton( R.string.create, (dialog, which) -> {

            String groupName = String.valueOf( Objects.requireNonNull( groupNameField.getEditText() ).getText() );

            if ( !groupName.isEmpty() ) {
                String avatar = "https://api.multiavatar.com/"+groupName+".png";
                String vn = generateGroupVirtualNumber();
                String Notification_key = vn + generateRandomString();

                RCModel rcModel = new RCModel(
                        groupName,
                        avatar,
                        Constants.current_user_virtual_number,
                        vn,
                        Calendar.getInstance().getTime().getTime()+"",
                        generateRandomString(),
                        Notification_key,
                        null
                );

                databaseReference.child(Constants.DB_MAIN)
                        .child(Constants.current_user_virtual_number)
                        .child(vn)
                        .setValue(rcModel);

                databaseReference.child(Constants.DB_GRP)
                        .child(vn)
                        .setValue(rcModel);

                storeGroupMemberInfo( vn );

                //Subscribe to group virtual number to receive notifications
                FirebaseMessaging.getInstance().subscribeToTopic(vn);
            } else {
                Toasty.error(context, getString(R.string.enter_group_name_error), Toast.LENGTH_SHORT,true).show();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
        dialogBuilder.show();
    }

    private String Notification_key = null;
    private void joinGroup() {
        View customAlertDialogView = LayoutInflater.from( context ).inflate( R.layout.join_group_dialog, null, false );

        TextInputLayout groupNameField = customAlertDialogView.findViewById( R.id.group_vn_field );

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder( context );
        dialogBuilder.setView( customAlertDialogView );

        dialogBuilder.setTitle( R.string.join_group_dialog );
        dialogBuilder.setMessage( R.string.join_group_message_dialog );
        dialogBuilder.setIcon(R.drawable.ic_twotone_groups_icon);
        dialogBuilder.setPositiveButton( R.string.create, (dialog, which) -> {

            String groupVN = String.valueOf( Objects.requireNonNull( groupNameField.getEditText() ).getText() );

            if ( !groupVN.isEmpty() ) {

                Util.showLoading( context );

                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DataSnapshot dataSnapshot = snapshot.child( Constants.DB_GRP + "/" + groupVN );

                        if ( dataSnapshot.exists() ) {
                            for ( DataSnapshot data : dataSnapshot.getChildren() ) {
                                if ( "name".equals( data.getKey() ) ) {
                                    groupName = String.valueOf( data.getValue() );
                                }
                                if ( "avatar".equals( data.getKey() ) ) {
                                    avatar = String.valueOf( data.getValue() );
                                }
                                if ( "groupVn".equals( data.getKey() ) ) {
                                    grpVn = String.valueOf( data.getValue() );
                                }
                                if ( "crID".equals( data.getKey() ) ) {
                                    crID = String.valueOf( data.getValue() );
                                }
                                if ( "vn".equals( data.getKey() ) ) {
                                    vn = String.valueOf( data.getValue() );
                                }
                                if ("notification_key".equals( data.getKey() )) {
                                    Notification_key = String.valueOf( data.getValue() );
                                }
                            }
                        } else {
                            Toasty.error(context, getString(R.string.group_does_not_exits_error), Toast.LENGTH_SHORT, true).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                };
                groupReference.addListenerForSingleValueEvent( valueEventListener );

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if ( groupName != null && avatar != null && grpVn != null && crID != null && vn != null && Notification_key != null) {
                        RCModel rcModel = new RCModel(
                                groupName,
                                avatar,
                                vn,
                                grpVn,
                                Calendar.getInstance().getTime().getTime()+"",
                                crID,
                                Notification_key,
                                null
                        );

                        databaseReference.child(Constants.DB_MAIN)
                                .child(Constants.current_user_virtual_number)
                                .child( groupVN )
                                .setValue(rcModel).addOnSuccessListener(tResult -> {
                                    //Subscribe to group virtual number to receive notifications
                                    FirebaseMessaging.getInstance().subscribeToTopic(grpVn);
                                    handler.removeCallbacksAndMessages(null);

                                    storeGroupMemberInfo( groupVN );

                                    Util.hideLoading();
                                });
                    } else {
                        Toasty.error(context, getString(R.string.something_went_wrong_error), Toast.LENGTH_SHORT, true).show();
                    }
                },10000); //after 10 sec join the group

            } else {
                Toasty.error(context, getString(R.string.enter_group_vn_error), Toast.LENGTH_SHORT,true).show();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
        dialogBuilder.show();
    }

    private void storeGroupMemberInfo(String groupVN) {
        groupInfoReference.child( Constants.GRP_INFO ).child( groupVN )
                .child( Constants.current_user_virtual_number ).setValue("{}");
    }

    private String generateGroupVirtualNumber() {
        String s = "1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        Random rnd = new Random();
        while (stringBuilder.length() < 12) { // length of the random string.
            int index = (int) (rnd.nextFloat() * s.length());
            stringBuilder.append(s.charAt(index));
        }
        return stringBuilder.toString();
    }

    private void updateAddPersonEditText(boolean visibility) {
        if (visibility) {
            //hide the add person button
            add_person_button.setVisibility(View.GONE);
            //show the cancel add person button
            cancel_add_person_button.setVisibility(View.VISIBLE);
            //show the add person summary
            add_person_summary.setVisibility(View.VISIBLE);
            //show the edit text for entering number
            add_person_edit_text.setVisibility(View.VISIBLE);
        } else {
            add_person_summary.setVisibility(View.GONE);
            add_person_edit_text.setText(null);
            add_person_edit_text.setVisibility(View.GONE);
            cancel_add_person_button.setVisibility(View.GONE);
            add_person_button_done.setVisibility(View.GONE);
            add_person_button.setVisibility(View.VISIBLE);

            hideSoftKeyboard(); //hide the soft-keyboard if present on screen
        }
    }

    private void updateRecyclerViewUI(boolean visibility) {
        if (visibility) {
            contacts_recycler_view.setVisibility(View.GONE);
            no_contacts_found_view.setVisibility(View.VISIBLE);
        } else {
            no_contacts_found_view.setVisibility(View.GONE);
            contacts_recycler_view.setVisibility(View.VISIBLE);
        }
    }

    //Method to hide softKeyboard from the screen
    public void hideSoftKeyboard() {
        if (context!=null) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService
                    (Context.INPUT_METHOD_SERVICE);

            assert imm != null;
            imm.hideSoftInputFromWindow(add_person_edit_text.getWindowToken(), 0);
        }
    }

    private void fetchUserDetails() {
        //Get the virtual number from edit text and store it in string format
        String getVirtualNumber = add_person_edit_text.getText().toString();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot userDetails = snapshot.child( Constants.DB_VN + "/" + getVirtualNumber );
                if (userDetails.exists()) {
                    for (DataSnapshot details : userDetails.getChildren()) {
                        if ( "name".equals( details.getKey() ) ) {
                            another_user_name = String.valueOf( details.getValue() );
                        }
                        if ( "avatar".equals( details.getKey() ) ) {
                            another_user_avatar = String.valueOf( details.getValue() );
                        }
                        if ( another_user_name != null && another_user_avatar != null ) {
                            saveUserToDatabase( another_user_name, another_user_avatar );
                        } else {
                            fetchUserDetails();
                        }
                    }
                } else {
                    Toasty.error(context, getString(R.string.user_not_exit_error),Toast.LENGTH_SHORT,true).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }

    private void saveUserToDatabase(String name, String avatar) {
        if ( name != null && avatar != null ) {
            //Get the virtual number from edit text and store it in string format
            String getVirtualNumber = add_person_edit_text.getText().toString();
            String crID = generateRandomString();
            String NotificationKey = getVirtualNumber + crID.trim();

            RCModel rcModel = new RCModel(
                    name,
                    avatar,
                    getVirtualNumber,
                    null,
                    Calendar.getInstance().getTime().getTime()+"",
                    crID,
                    NotificationKey,
                    null
            );

            databaseReference.child(Constants.CONTACTS_DB_PATH).child(getVirtualNumber).setValue(rcModel)
                    .addOnSuccessListener(unused -> updateAddPersonEditText(false));

            //Subscribe to chat room id to receive notifications
            FirebaseMessaging.getInstance().subscribeToTopic(crID);
        } else {
            fetchUserDetails();
        }
    }

    private String generateRandomString() {
        String s = "1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        Random rnd = new Random();
        while (stringBuilder.length() < 20) { // length of the random string.
            int index = (int) (rnd.nextFloat() * s.length());
            stringBuilder.append(s.charAt(index));
        }
        return stringBuilder.toString();
    }

    /**
     * Method to pass the data to chat fragment
     * @param user_name name of chat user
     * @param user_virtual_number number of the chat user
     */
    @Override
    public void data(String user_name, String user_avatar, String user_virtual_number, String group_vn, String crID, String Notification_key) {
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.receivedData(user_name, user_avatar, user_virtual_number, group_vn, crID, Notification_key);
    }
}
