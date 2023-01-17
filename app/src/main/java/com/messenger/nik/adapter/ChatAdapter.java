package com.messenger.nik.adapter;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.cardview.widget.CardView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.messenger.nik.R;
import com.messenger.nik.fragment.ChatFragment;
import com.messenger.nik.helper.Constants;
import com.messenger.nik.customInterface.ChatUserData;
import com.messenger.nik.models.ChatModel;

public class ChatAdapter extends FirebaseRecyclerAdapter<ChatModel, ChatAdapter.CustomViewHolder> {

    //CONSTANT
    private static final String TAG = ChatAdapter.class.getSimpleName();
    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_GIF_MSG = 2;
    private static final int LEFT_GIF_MSG = 3;
    private static final int RIGHT_FILE_MSG = 4;
    private static final int LEFT_FILE_MSG = 5;

    //Interface Class
    private final ChatUserData cd;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     */
    public ChatAdapter(@NonNull FirebaseRecyclerOptions<ChatModel> options, ChatUserData cd) {
        super(options);
        this.cd = cd;
    }

    @Override
    protected void onBindViewHolder(@NonNull CustomViewHolder holder, int position, @NonNull ChatModel model) {
        holder.setItem_message_card_view();
        holder.setItem_message_text_view(model.getMessage());
        //If the tag message is present and it is not equals to null
        //then show the tag message view else hide the tag message view
        if (model.getTm()!=null) {
            holder.setTag_message_view(View.VISIBLE);
            holder.setSet_tag_message_text_view(model.getTm());
        } else {
            holder.setTag_message_view(View.GONE);
            holder.setSet_tag_message_text_view(null);
        }
        //If gif message is present and it is not equals to null
        //then show the gif message
        if (model.getGm()!=null) {
            holder.setItem_message_gif_image_view(model.getGm());
        }

        if (model.getFileModel() != null) {
            holder.setItem_message_download_file_title(model.getFileModel().getName());
            holder.setItem_message_download_file_button();
        }
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == RIGHT_MSG){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right,parent,false);
        }else if (viewType == LEFT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left,parent,false);
        } else if (viewType == RIGHT_GIF_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_gif_right,parent,false);
        } else if (viewType == LEFT_GIF_MSG){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_gif_left,parent,false);
        } else if (viewType == RIGHT_FILE_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_file_right,parent,false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_file_left,parent,false);
        }
        return new CustomViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel model = getItem(position);
        if (model.getGm()!=null) {
            if (model.getVn().equals(Constants.current_user_virtual_number) ) {
                return RIGHT_GIF_MSG;
            } else {
                return LEFT_GIF_MSG;
            }
        } else if (model.getFileModel() != null) {
            if (model.getVn().equals(Constants.current_user_virtual_number)) {
                return RIGHT_FILE_MSG ;
            } else {
                return LEFT_FILE_MSG;
            }
        } else if (model.getMessage()!=null && model.getVn().equals(Constants.current_user_virtual_number)){
            return RIGHT_MSG;
        }else{
            return LEFT_MSG;
        }
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        CardView item_message_card_view;
        LinearLayout tag_message_view;
        TextView item_message_text_view,set_tag_message_text_view, item_message_download_file_title;
        ScrollView tag_message_scroll_view;
        ImageView item_message_gif_image_view, item_message_download_file_button;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            item_message_card_view = itemView.findViewById(R.id.item_message_card_view);
            item_message_text_view = itemView.findViewById(R.id.item_message_text_view);
            tag_message_view = itemView.findViewById(R.id.tag_message_view_item);
            tag_message_scroll_view = itemView.findViewById(R.id.tag_message_scroll_view);
            set_tag_message_text_view = itemView.findViewById(R.id.set_tag_message_text_view);
            item_message_gif_image_view = itemView.findViewById(R.id.item_message_gif_image_view);
            item_message_download_file_button = itemView.findViewById(R.id.item_message_download_file_button);
            item_message_download_file_title = itemView.findViewById(R.id.item_message_download_file_title);

            if (set_tag_message_text_view!=null) {
                set_tag_message_text_view.setOnTouchListener((v, event) -> {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                });

                //Enabling scrolling on TextView.
                set_tag_message_text_view.setMovementMethod(new ScrollingMovementMethod());
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            final ChatModel model = getItem(position);
            if (v == item_message_card_view) {
                cd.data(model.getMessage(),null,null,null);
                ChatFragment.get().tagMessageView(true,model.getMessage());
            }
            if (v == item_message_download_file_button) {
                cd.data(null,null,model.getFileModel().getUrl(),model.getFileModel().getType());
                ChatFragment.get().permissions();//ask permission before downloading file
            }
        }

        void setItem_message_card_view() {
            if (item_message_card_view == null) return;
            item_message_card_view.setOnClickListener(this);
        }

        void setItem_message_text_view(String message) {
            if (item_message_text_view == null) return;
            item_message_text_view.setText(message);
        }

        void setTag_message_view(int visibility) {
            if (tag_message_view == null) return;
            tag_message_view.setVisibility(visibility);
        }

        void setSet_tag_message_text_view(String message) {
            if (set_tag_message_text_view == null) return;
            if (message!=null) {
                set_tag_message_text_view.setText(message);
            }
        }

        void setItem_message_gif_image_view(String url) {
            if (item_message_gif_image_view == null) return;
            if (url!=null) {
                Glide.with(item_message_gif_image_view.getContext())
                        .load(url).into(item_message_gif_image_view);
            }
        }

        void setItem_message_download_file_title(String title) {
            if (item_message_download_file_title == null) return;
            item_message_download_file_title.setText(title);
        }

        void setItem_message_download_file_button() {
            if (item_message_download_file_button == null) return;
            item_message_download_file_button.setOnClickListener(this);
        }
    }

    /*
    private void deleteSelectedMessage(ChatModel model) {
        String path = Constants.DB_MAIN+"/"+Constants.virtual_number+"/"+Constants.user_virtual_number+"/"+"chatModel";
        databaseReference.child(path)
                .orderByChild("/user_message").equalTo(model.getUser_message())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            dataSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }
    */
}
