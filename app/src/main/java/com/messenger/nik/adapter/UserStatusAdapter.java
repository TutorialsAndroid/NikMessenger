package com.messenger.nik.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.messenger.nik.R;
import com.messenger.nik.models.UserStatus;

public class UserStatusAdapter extends FirebaseRecyclerAdapter<UserStatus, UserStatusAdapter.CustomViewHolder> {

    //CONSTANT
    private static final String TAG = UserStatusAdapter.class.getSimpleName();

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     */
    public UserStatusAdapter(@NonNull FirebaseRecyclerOptions<UserStatus> options) {
        super(options);
    }


    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_status_item, parent,false);
        return new CustomViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull CustomViewHolder holder, int position, @NonNull UserStatus model) {
        holder.setUser_status_image();
        holder.setUser_status_name( model.getName() );
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView user_status_image;
        TextView user_status_name;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            user_status_image = itemView.findViewById(R.id.user_status_item);
            user_status_name = itemView.findViewById(R.id.user_status_item_name);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            final UserStatus model = getItem(position);

            if ( v  == user_status_image ) {
                Log.i(TAG, model.getStatus_url() );
                Log.i(TAG, model.getVn() );
            }
        }

        void setUser_status_image() {
            if (this.user_status_image == null) return;
            Glide.with(user_status_image.getContext())
                    .load(getProfilePhoto())
                    .apply(RequestOptions.circleCropTransform())
                    .into(user_status_image);

            user_status_image.setOnClickListener(this);
        }

        void setUser_status_name(String name) {
            if (this.user_status_name == null) return;
            user_status_name.setText( name );
        }

        int getProfilePhoto() {
            return user_status_image.getContext().getResources()
                    .getIdentifier("default_profile", "drawable", user_status_image.getContext().getPackageName());
        }
    }
}
