package com.messenger.nik.adapter;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.messenger.nik.R;
import com.messenger.nik.activity.FragmentLoadActivity;
import com.messenger.nik.fragment.ChatFragment;
import com.messenger.nik.fragment.GlideApp;
import com.messenger.nik.helper.Constants;
import com.messenger.nik.customInterface.RCSUserData;
import com.messenger.nik.models.RCModel;

public class RCSAdapter extends FirebaseRecyclerAdapter<RCModel, RCSAdapter.CustomViewHolder> {

    //CONSTANT
    private static final String TAG = RCSAdapter.class.getSimpleName();

    //INTERFACE CLASS
    private final RCSUserData RCSUserData;

    //FIREBASE DATABASE CLASS
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     */
    public RCSAdapter(@NonNull FirebaseRecyclerOptions<RCModel> options, RCSUserData RCSUserData ) {
        super(options);
        this.RCSUserData = RCSUserData;
    }

    @Override
    protected void onBindViewHolder(@NonNull CustomViewHolder holder, int position, @NonNull RCModel model) {
        holder.setRecent_chat_view();
        holder.setRecent_chat_user_name(model.getName());
        holder.setRecent_chat_virtual_number(model.getVn());
        holder.setRecent_chat_user_profile_photo(model.getAvatar());
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_chat,parent,false);
        return new CustomViewHolder(view);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnCreateContextMenuListener {

        LinearLayout recent_chat_view;
        TextView recent_chat_user_name;
        ImageView recent_chat_user_profile_photo;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            recent_chat_view = itemView.findViewById(R.id.recent_chat_view);
            recent_chat_user_name = itemView.findViewById(R.id.recent_chat_user_name);
            recent_chat_user_profile_photo = itemView.findViewById(R.id.recent_chat_user_profile_photo);

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            final RCModel model = getItem(position);

            if (v == recent_chat_view) {
                if (model.getNotification_key() != null) {
                    Log.d(TAG, model.getNotification_key());
                } else {
                    Log.d(TAG, "notification key was null");
                }
                //pass the user data to interface so we can use it later on chat fragment
                RCSUserData.data(
                        model.getName(),
                        model.getAvatar(),
                        model.getVn(),
                        model.getGroupVn(),
                        model.getCrID(),
                        model.getNotification_key()
                );
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = getAdapterPosition();
            final RCModel model = getItem(position);

            menu.setHeaderTitle("Select The Action");
            menu.add(0, 1, 0, "Delete Chat")
                    .setOnMenuItemClickListener(item -> {
                        deleteChat(model);
                        return false;
                    });
            menu.add(0, 2, 0, "Report")
                    .setOnMenuItemClickListener(item -> {
                        //TODO add report user method
                        return false;
                    });

        }

        void setRecent_chat_view(){
            if (this.recent_chat_view == null) return;
            recent_chat_view.setOnClickListener(this);
        }

        void setRecent_chat_user_name(String user_name) {
            if (this.recent_chat_user_name == null) return;
            recent_chat_user_name.setText(user_name);
        }

        void setRecent_chat_virtual_number(String virtual_number) {
            Log.i(TAG,virtual_number);
        }

        void setRecent_chat_user_profile_photo( String avatar )
        {
            //If imageView is null then don't do anything
            if (this.recent_chat_user_profile_photo == null) return;

            GlideApp.with( recent_chat_user_profile_photo.getContext() )
                    .load( avatar )
                    .apply(RequestOptions.circleCropTransform())
                    .into( recent_chat_user_profile_photo );
        }
    }

    private void deleteChat(RCModel rcModel) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //delete rc_model from another user
                snapshot.child( Constants.DB_MAIN + "/" + rcModel.getVn() + "/" +
                        Constants.current_user_virtual_number )
                        .getRef().removeValue();
                //delete rc_model from current user
                snapshot.child( Constants.DB_MAIN + "/" +
                        Constants.current_user_virtual_number + "/" +
                        rcModel.getVn() ).getRef().removeValue();
                //delete chat room
                snapshot.child( Constants.DB_CR + "/" + rcModel.getCrID() )
                        .getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }
}