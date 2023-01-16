package com.messenger.nik.fragment;

import android.content.Context;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.google.firebase.database.ValueEventListener;
import com.messenger.nik.R;
import com.messenger.nik.activity.FragmentLoadActivity;
import com.messenger.nik.adapter.RCSAdapter;
import com.messenger.nik.adapter.UserStatusAdapter;
import com.messenger.nik.helper.Constants;
import com.messenger.nik.customInterface.RCSUserData;
import com.messenger.nik.models.RCModel;
import com.messenger.nik.models.UserStatus;

/*
RCSFragment (Recent-chat-screen Fragment)
 */
public class RCSFragment extends Fragment implements View.OnClickListener, RCSUserData {

    //CONSTANTS
    private static final String TAG = RCSFragment.class.getSimpleName();
    private static String RCS_DB_PATH = null;
    private static String RCS_ST_DB_PATH = null;

    //ANDROID RESOURCE CLASS
    private RecyclerView.AdapterDataObserver adapterDataObserver, userStatusDataObserver;
    private LinearLayoutManager linearLayoutManager, horizontalLayout;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private Context context;

    //UI COMPONENTS
    private ImageView user_profile_button;
    private RelativeLayout no_recent_chats_found_view;
    private RecyclerView recent_chats_recycler_view, user_status_recycler_view;
    private FloatingActionButton add_message_button;

    //FIREBASE DATABASE CLASS
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private RCSAdapter rcsAdapter;
    private UserStatusAdapter usAdapter;

    @Override
    public void onResume() {
        super.onResume();

        //start listening to firebase adapter
        rcsAdapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop listening to firebase adapter
        rcsAdapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        context = null;

        //Remove the views to prevent memory leaks
        rcsAdapter.unregisterAdapterDataObserver(adapterDataObserver);
        usAdapter.unregisterAdapterDataObserver(userStatusDataObserver);
        userStatusDataObserver = null;
        adapterDataObserver = null;

        recent_chats_recycler_view.setAdapter(null);
        user_status_recycler_view.setAdapter(null);

        user_status_recycler_view = null;
        recyclerViewLayoutManager = null;

        linearLayoutManager = null;
        horizontalLayout = null;

        user_profile_button = null;
        add_message_button = null;

        no_recent_chats_found_view = null;
        recent_chats_recycler_view = null;
    }

    public RCSFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize context
        context = requireContext();
        //Initialize path to recent chats
        RCS_DB_PATH = Constants.DB_MAIN + "/" + Constants.current_user_virtual_number;
        //Initialize path to share quick status
        RCS_ST_DB_PATH = Constants.DB_US + "/" + Constants.current_user_virtual_number;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_chat_screen_ui,container,false);

        //Initialize add person button
        add_message_button = view.findViewById(R.id.add_message_button);
        add_message_button.setOnClickListener(this);

        //Initialize user profile button
        user_profile_button = view.findViewById(R.id.user_profile_button);
        user_profile_button.setOnClickListener(this);

        //Initialize no recent chats found layout
        no_recent_chats_found_view = view.findViewById(R.id.no_recent_chats_found_view);

        //Initialize recent chat recycler view here
        recent_chats_recycler_view = view.findViewById(R.id.recent_chats_recycler_view);

        //Initialize the user status recycler view here
        user_status_recycler_view = view.findViewById(R.id.user_status_recycler_view);
        recyclerViewLayoutManager = new LinearLayoutManager( context );
        user_status_recycler_view.setLayoutManager( recyclerViewLayoutManager );

        /*start of user status adapter*/
        Query userStatusQuery = FirebaseDatabase.getInstance()
                .getReference()
                .child(RCS_ST_DB_PATH);

        FirebaseRecyclerOptions<UserStatus> userStatusFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<UserStatus>()
                .setQuery(userStatusQuery, UserStatus.class).setLifecycleOwner(this).build();

        usAdapter = new UserStatusAdapter( userStatusFirebaseRecyclerOptions, requireContext() );

        userStatusDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                usAdapter.getItemCount();

                user_status_recycler_view.scrollToPosition(0);
            }
        };
        usAdapter.registerAdapterDataObserver(userStatusDataObserver);

        horizontalLayout = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        user_status_recycler_view.setLayoutManager(horizontalLayout);
        user_status_recycler_view.setAdapter(usAdapter);
        /*End of user status adapter*/

        updateRecyclerViewUI(true);

        //Initialize linear layout manager
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child(RCS_DB_PATH)
                .orderByChild("/timeStamp");

        FirebaseRecyclerOptions<RCModel> rcModelFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<RCModel>()
                .setQuery(query, RCModel.class).setLifecycleOwner(this).build();

        rcsAdapter = new RCSAdapter( rcModelFirebaseRecyclerOptions,this );

        adapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = rcsAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 || (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    recent_chats_recycler_view.scrollToPosition(positionStart);
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
        recent_chats_recycler_view.setLayoutManager(linearLayoutManager);
        recent_chats_recycler_view.setAdapter(rcsAdapter);
        rcsAdapter.startListening();

        //add vertical line between recycler view items
        recent_chats_recycler_view.addItemDecoration(new DividerItemDecoration(recent_chats_recycler_view.getContext(), DividerItemDecoration.VERTICAL));

        fetchCurrentProfileDetails();

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == add_message_button) {
            FragmentLoadActivity.get().loadFragment(new ContactsFragment(), "ContactsFragment");
        }

        if (v == user_profile_button) {
            FragmentLoadActivity.get().loadFragment(new ProfileFragment(), "ProfileFragment");
        }
    }

    private void updateRecyclerViewUI(boolean visibility) {
        if (visibility) {
            recent_chats_recycler_view.setVisibility(View.GONE);
            no_recent_chats_found_view.setVisibility(View.VISIBLE);
        } else {
            no_recent_chats_found_view.setVisibility(View.GONE);
            recent_chats_recycler_view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method to pass the data to chat fragment
     * @param user_name name of chat user
     * @param user_virtual_number number of the chat user
     */
    @Override
    public void data(String user_name, String user_avatar, String user_virtual_number, String groupVn, String crID, String Notification_key) {
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.receivedData(user_name, user_avatar, user_virtual_number, groupVn, crID, Notification_key);
    }

    private void fetchCurrentProfileDetails() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot current_details = snapshot.child( Constants.DB_VN + "/" + Constants.current_user_virtual_number);

                if ( current_details.exists() ) {
                    for (DataSnapshot detail : current_details.getChildren()) {
                        if ("name".equals( detail.getKey() ) ) {
                            Constants.current_user_name = String.valueOf( detail.getValue() );
                        }
                        if ("avatar".equals( detail.getKey() ) ) {
                            Constants.current_user_avatar = String.valueOf( detail.getValue() );
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }
}
